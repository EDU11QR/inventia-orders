// =========================================================
// AREA SPLINE CHART SVG (sin librerías externas)
// =========================================================
// curva suavizada (catmull-rom -> bezier), área con
// degradado azul, grilla sutil, animación de trazado
// al montar/cambiar periodo y marcadores discretos
//
// props:
//   datos        [{ etiqueta: "08:00", total: 4 }, ...]
//                ya viene completo (huecos rellenos con ceros)
//   pasoEtiqueta opcional: cada cuántos puntos se muestra
//                etiqueta en el eje X (si no, se calcula solo)
//   clave        valor que reinicia la animación al cambiar
//                (ej. el periodo activo del selector)
// =========================================================

import { useId } from "react";

const ANCHO = 640;
const ALTO = 320;
const MARGEN_IZQ = 40;
const MARGEN_DER = 16;
const MARGEN_ARRIBA = 18;
const MARGEN_ABAJO = 34;

const COLOR_LINEA = "#2563eb";      // blue-600
const COLOR_MARCADOR_BORDE = "#2563eb";
const COLOR_GRILLA = "#eef2f7";     // slate casi imperceptible
const COLOR_EJE_TEXTO = "#94a3b8";  // slate-400

// =========================================================
// convierte los puntos en un path bezier suave
// (interpolación catmull-rom con tensión moderada;
// nunca se sale del rango vertical real de los datos)
// =========================================================
function construirPathCurva(puntos) {

    if (puntos.length < 2) {
        return "";
    }

    let d = `M ${puntos[0].x.toFixed(2)},${puntos[0].y.toFixed(2)}`;

    for (let i = 0; i < puntos.length - 1; i++) {

        const p0 = puntos[i - 1] ?? puntos[i];
        const p1 = puntos[i];
        const p2 = puntos[i + 1];
        const p3 = puntos[i + 2] ?? p2;

        const tension = 0.18;

        const c1x = p1.x + (p2.x - p0.x) * tension;
        const c1y = p1.y + (p2.y - p0.y) * tension;
        const c2x = p2.x - (p3.x - p1.x) * tension;
        const c2y = p2.y - (p3.y - p1.y) * tension;

        d += ` C ${c1x.toFixed(2)},${c1y.toFixed(2)} ${c2x.toFixed(2)},${c2y.toFixed(2)} ${p2.x.toFixed(2)},${p2.y.toFixed(2)}`;
    }

    return d;
}

function GraficoLineas({ datos, pasoEtiqueta, clave }) {

    const idGradiente = useId();

    const totalPuntos = datos.length;

    if (totalPuntos === 0) {
        return null;
    }

    const maximo = Math.max(1, ...datos.map((p) => p.total));

    const anchoUtil = ANCHO - MARGEN_IZQ - MARGEN_DER;
    const altoUtil = ALTO - MARGEN_ARRIBA - MARGEN_ABAJO;
    const baseY = MARGEN_ARRIBA + altoUtil;

    // coordenadas de cada punto
    const puntos = datos.map((punto, indice) => ({

        ...punto,

        x:
            MARGEN_IZQ +
            (totalPuntos === 1
                ? anchoUtil / 2
                : (indice * anchoUtil) / (totalPuntos - 1)),

        y: baseY - (punto.total * altoUtil) / maximo
    }));

    // curva principal y su área (mismo recorrido + cierre al base)
    const pathCurva = construirPathCurva(puntos);
    const pathArea =
        totalPuntos > 1
            ? `${pathCurva} L ${puntos[puntos.length - 1].x.toFixed(2)},${baseY} L ${puntos[0].x.toFixed(2)},${baseY} Z`
            : "";

    // guías horizontales muy sutiles (4 divisiones)
    const divisiones = 4;
    const guias = Array.from({ length: divisiones + 1 }, (_, i) => ({
        y: MARGEN_ARRIBA + (altoUtil * i) / divisiones,
        valor: Math.round((maximo * (divisiones - i)) / divisiones)
    }));

    // etiquetas del eje X: fijo si lo dan, si no ~8 máximo
    const paso =
        pasoEtiqueta ??
        Math.max(1, Math.ceil(totalPuntos / 8));

    return (

        <svg
            viewBox={`0 0 ${ANCHO} ${ALTO}`}
            className="w-full select-none"
            role="img"
            aria-label="Gráfico de pedidos por periodo"
        >

            <defs>

                <linearGradient
                    id={idGradiente}
                    x1="0"
                    y1="0"
                    x2="0"
                    y2="1"
                >
                    <stop offset="0%" stopColor={COLOR_LINEA} stopOpacity="0.28" />
                    <stop offset="55%" stopColor={COLOR_LINEA} stopOpacity="0.10" />
                    <stop offset="100%" stopColor={COLOR_LINEA} stopOpacity="0" />
                </linearGradient>

            </defs>

            {/* grilla horizontal ultra sutil + escala Y */}
            {guias.map((guia, indice) => (
                <g key={indice}>
                    <line
                        x1={MARGEN_IZQ}
                        y1={guia.y}
                        x2={ANCHO - MARGEN_DER}
                        y2={guia.y}
                        stroke={COLOR_GRILLA}
                        strokeWidth="0.5"
                    />
                    <text
                        x={MARGEN_IZQ - 8}
                        y={guia.y + 3}
                        textAnchor="end"
                        fontSize="10"
                        fill={COLOR_EJE_TEXTO}
                    >
                        {guia.valor}
                    </text>
                </g>
            ))}

            {/* key por clave (periodo): reinicia la animación al filtrar */}
            <g key={clave ?? totalPuntos} className="grafico-contenido">

                {/* área con degradado */}
                {pathArea && (
                    <path d={pathArea} fill={`url(#${idGradiente})`} className="grafico-area" />
                )}

                {/* línea spline */}
                {pathCurva && (
                    <path
                        d={pathCurva}
                        fill="none"
                        stroke={COLOR_LINEA}
                        strokeWidth="2.5"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        pathLength="1"
                        className="grafico-linea"
                    />
                )}

                {/* marcadores discretos con tooltip nativo */}
                {puntos.map((p, indice) => (
                    <circle
                        key={indice}
                        cx={p.x}
                        cy={p.y}
                        r="3"
                        fill="#ffffff"
                        stroke={COLOR_MARCADOR_BORDE}
                        strokeWidth="2"
                        className="grafico-punto"
                        style={{
                            animationDelay: `${Math.min(indice * 18, 500)}ms`
                        }}
                    >
                        <title>{`${p.etiqueta}: ${p.total} pedidos`}</title>
                    </circle>
                ))}

            </g>

            {/* etiquetas del eje X */}
            {puntos.map((p, indice) => (
                indice % paso === 0 && (
                    <text
                        key={`x-${indice}`}
                        x={p.x}
                        y={ALTO - 12}
                        textAnchor="middle"
                        fontSize="10"
                        fill={COLOR_EJE_TEXTO}
                    >
                        {p.etiqueta}
                    </text>
                )
            ))}

        </svg>
    );
}

export default GraficoLineas;
