import axios from "axios";
import express from "express";
import QRCode from "qrcode";
import qrcodeTerminal from "qrcode-terminal";
import crypto from "crypto";
import fs from "node:fs";

import {
    makeWASocket,
    useMultiFileAuthState,
    DisconnectReason,
    fetchLatestWaWebVersion
} from "@whiskeysockets/baileys";

import { parsePedido } from "./parsers/pedidoParser.js";

const LISTENER_HOST = "127.0.0.1";
const LISTENER_PORT = Number(process.env.WHATSAPP_LISTENER_PORT || 3001);
const AUTH_DIR = "auth";

let activeSocket = null;
let startPromise = null;
let reconnectTimer = null;
let keepAliveInterval = null;
let socketGeneration = 0;
let logoutEnProgreso = false;
let logoutPromise = null;
let reconexionManual = false;
let reconnectPromise = null;

const whatsappState = {
    status: "STARTING",
    qrDataUrl: null,
    qrGeneratedAt: null,
    connectedAt: null,
    lastDisconnectAt: null,
    lastError: null,
    retryCount: 0
};

function updateWhatsappState(changes) {
    Object.assign(whatsappState, changes);
    console.log("[WhatsApp] Estado:", whatsappState.status);
}

function clearQr() {
    whatsappState.qrDataUrl = null;
    whatsappState.qrGeneratedAt = null;
}

function clearKeepAlive() {
    if (keepAliveInterval) {
        clearInterval(keepAliveInterval);
        keepAliveInterval = null;
    }
}

function cancelarReconexion() {
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
        console.log("[WhatsApp] Reconexión pendiente cancelada.");
    }
}

function eliminarCredenciales() {
    try {
        fs.rmSync(AUTH_DIR, { recursive: true, force: true });
        console.log("[WhatsApp] Carpeta de credenciales auth/ eliminada.");
    } catch (error) {
        console.error("[WhatsApp] No se pudo eliminar la carpeta auth/:", error.message);
    }
}

function reiniciarEstadoWhatsApp() {
    updateWhatsappState({
        status: "STARTING",
        qrDataUrl: null,
        qrGeneratedAt: null,
        connectedAt: null,
        lastDisconnectAt: null,
        lastError: null,
        retryCount: 0
    });
}

function scheduleReconnect() {
    if (reconnectTimer) {
        console.log("[WhatsApp] Reconexión ya programada.");
        return;
    }

    updateWhatsappState({ status: "RECONNECTING" });
    console.log("[WhatsApp] Reconectando en 5 segundos...");

    reconnectTimer = setTimeout(() => {
        reconnectTimer = null;
        iniciarBotSeguro("reconexión automática");
    }, 5000);
}

function getStatusResponse() {
    return {
        status: whatsappState.status,
        connectedAt: whatsappState.connectedAt,
        lastDisconnectAt: whatsappState.lastDisconnectAt,
        lastError: whatsappState.lastError,
        retryCount: whatsappState.retryCount,
        qrAvailable: Boolean(whatsappState.qrDataUrl)
    };
}

const app = express();

app.get("/api/whatsapp/status", (req, res) => {
    res.json(getStatusResponse());
});

app.get("/api/whatsapp/qr", (req, res) => {
    if (!whatsappState.qrDataUrl) {
        return res.status(409).json({
            status: whatsappState.status,
            message: "No hay un código QR disponible."
        });
    }

    res.json({
        status: whatsappState.status,
        qr: whatsappState.qrDataUrl,
        generatedAt: whatsappState.qrGeneratedAt
    });
});

app.post("/api/whatsapp/logout", (req, res) => {

    if (logoutPromise) {
        return res.status(409).json({
            status: whatsappState.status,
            message: "Ya hay una desvinculación en curso."
        });
    }

    logoutPromise = desvincularWhatsApp()
        .then((respuesta) => {
            res.json(respuesta);
        })
        .catch((error) => {

            console.error(
                "[WhatsApp] Error al desvincular:",
                error?.message || error
            );

            updateWhatsappState({
                status: "ERROR",
                lastError: error?.message || "Fallo al desvincular WhatsApp."
            });

            if (!res.headersSent) {
                res.status(500).json({
                    status: whatsappState.status,
                    message: "No se pudo desvincular WhatsApp."
                });
            }
        })
        .finally(() => {
            logoutPromise = null;
        });
});

app.post("/api/whatsapp/reconnect", (req, res) => {

    if (logoutEnProgreso || logoutPromise) {
        return res.status(409).json({
            status: whatsappState.status,
            message: "Hay una desvinculación en curso; no se puede reconectar."
        });
    }

    if (reconnectPromise || whatsappState.status === "STARTING") {
        return res.status(409).json({
            status: "RECONNECTING",
            message: "Ya existe una reconexión en curso."
        });
    }

    reconnectPromise = reconectarWhatsApp()
        .then((respuesta) => {
            res.json(respuesta);
        })
        .catch((error) => {

            console.error(
                "[WhatsApp] Error al reconectar:",
                error?.message || error
            );

            updateWhatsappState({
                status: "ERROR",
                lastError: error?.message || "Fallo al reconectar WhatsApp."
            });

            if (!res.headersSent) {
                res.status(500).json({
                    status: whatsappState.status,
                    message: "No se pudo iniciar la reconexión."
                });
            }
        })
        .finally(() => {
            reconnectPromise = null;
        });
});

app.listen(LISTENER_PORT, LISTENER_HOST, () => {
    console.log(`[WhatsApp API] Escuchando en http://${LISTENER_HOST}:${LISTENER_PORT}`);
});

async function startBot() {

    if (startPromise) {
        console.log("[WhatsApp] Inicio ya en curso; se reutiliza la conexión pendiente.");
        return startPromise;
    }

    if (activeSocket) {
        console.log("[WhatsApp] Ya existe un socket activo; no se crea otro.");
        return;
    }

    startPromise = startBotInternal();

    try {
        await startPromise;
    } finally {
        startPromise = null;
    }
}

function iniciarBotSeguro(origen) {

    startBot().catch((error) => {

        console.error(
            `[WhatsApp] Error al iniciar el bot (${origen}):`,
            error?.message || error
        );

        updateWhatsappState({
            status: "ERROR",
            lastError: error?.message || "Fallo al iniciar la conexión de WhatsApp."
        });
    });
}

async function desvincularWhatsApp() {

    logoutEnProgreso = true;

    try {
        cancelarReconexion();
        clearKeepAlive();
        console.log("[WhatsApp] Desvinculación solicitada desde la API.");

        const sock = activeSocket;

        if (sock) {

            try {

                await sock.logout();
                console.log("[WhatsApp] Sesión cerrada en el servidor de WhatsApp.");

            } catch (error) {

                console.log(
                    "[WhatsApp] logout() no se completó; se fuerza el cierre del socket:",
                    error?.message || error
                );

                try {
                    sock.end(undefined);
                } catch { }
            }

            // esperar a que el evento close libere el socket (máximo 3 segundos)
            const inicio = Date.now();

            while (activeSocket === sock && Date.now() - inicio < 3000) {
                await new Promise((resolve) => setTimeout(resolve, 100));
            }

            if (activeSocket === sock) {
                activeSocket = null;
            }

        } else {
            console.log("[WhatsApp] No hay socket activo; se eliminan las credenciales directamente.");
        }

        eliminarCredenciales();

        reiniciarEstadoWhatsApp();

        iniciarBotSeguro("desvinculación");

        return {
            status: whatsappState.status,
            message: "Desvinculación iniciada; se generará un nuevo código QR."
        };

    } finally {
        logoutEnProgreso = false;
    }
}

async function reconectarWhatsApp() {

    reconexionManual = true;

    try {
        cancelarReconexion();
        clearKeepAlive();
        console.log("[WhatsApp] Reconexión manual solicitada desde la API.");

        const sock = activeSocket;

        if (sock) {

            try {

                sock.end(undefined);
                console.log("[WhatsApp] Socket activo cerrado para reconexión; credenciales auth/ intactas.");

            } catch (error) {

                console.log(
                    "[WhatsApp] No se pudo cerrar el socket activo:",
                    error?.message || error
                );
            }

            // esperar a que el evento close libere el socket (máximo 3 segundos)
            const inicio = Date.now();

            while (activeSocket === sock && Date.now() - inicio < 3000) {
                await new Promise((resolve) => setTimeout(resolve, 100));
            }

            if (activeSocket === sock) {
                activeSocket = null;
            }

        } else {
            console.log("[WhatsApp] No hay socket activo; se inicia una conexión nueva.");
        }

        clearQr();
        updateWhatsappState({
            status: "RECONNECTING",
            lastError: null
        });

        // liberar el flag antes de arrancar para no filtrar los eventos del socket nuevo
        reconexionManual = false;

        iniciarBotSeguro("reconexión manual");

        return {
            status: "RECONNECTING",
            message: "Reconexión iniciada."
        };

    } finally {
        reconexionManual = false;
    }
}

async function startBotInternal() {

    const generation = ++socketGeneration;
    updateWhatsappState({
        status: "STARTING",
        lastError: null
    });
    console.log(`[WhatsApp] Iniciando socket (generación ${generation}).`);

    // ==========================================
    // sesión persistente whatsapp
    // ==========================================

    const { state, saveCreds } =

        await useMultiFileAuthState(AUTH_DIR);

    // ==========================================
    // obtener última versión de WhatsApp Web
    // ==========================================

    let version = [2, 3000, 1015901307];
    try {
        const { version: latestVersion, isLatest } = await fetchLatestWaWebVersion();
        version = latestVersion;
        console.log(`📡 Usando versión de WhatsApp Web: ${version.join(".")}, es la última: ${isLatest}`);
    } catch (err) {
        console.log("⚠️ No se pudo obtener la última versión de WhatsApp Web, usando versión fallback.");
    }

    // ==========================================
    // socket whatsapp
    // ==========================================

    const sock = makeWASocket({

        version,

        auth: state,

        syncFullHistory: false,

        fireInitQueries: false,

        markOnlineOnConnect: false
    });

    activeSocket = sock;

    // ==========================================
    // guardar credenciales
    // ==========================================

    sock.ev.on(
        "creds.update",
        saveCreds
    );

    // ==========================================
    // eventos conexión
    // ==========================================

    sock.ev.on(

        "connection.update",

        async ({ connection, qr, lastDisconnect }) => {

            if (generation !== socketGeneration || activeSocket !== sock) {
                console.log(`[WhatsApp] Evento ignorado de socket obsoleto (generación ${generation}).`);
                return;
            }

            // ======================================
            // qr consola y API local
            // ======================================

            if (qr) {

                qrcodeTerminal.generate(
                    qr,
                    { small: true }
                );

                try {
                    const qrDataUrl = await QRCode.toDataURL(qr);

                    if (generation !== socketGeneration || activeSocket !== sock || logoutEnProgreso || reconexionManual || whatsappState.status === "CONNECTED") {
                        console.log(`[WhatsApp] QR descartado de socket obsoleto o ya conectado (generación ${generation}).`);
                        return;
                    }

                    updateWhatsappState({
                        status: "QR_READY",
                        qrDataUrl,
                        qrGeneratedAt: new Date().toISOString(),
                        lastError: null
                    });
                    console.log("[WhatsApp] QR actualizado y disponible en GET /api/whatsapp/qr.");
                } catch (error) {
                    clearQr();
                    updateWhatsappState({
                        status: "ERROR",
                        lastError: "No se pudo generar la imagen del código QR."
                    });
                    console.error("[WhatsApp] Error generando QR:", error.message);
                }
            }

            if (connection === "connecting" && !qr) {
                updateWhatsappState({ status: "CONNECTING" });
            }

            // ======================================
            // conexión exitosa
            // ======================================

            if (connection === "open") {

                console.log(
                    "✅ WhatsApp conectado"
                );

                clearQr();
                updateWhatsappState({
                    status: "CONNECTED",
                    connectedAt: new Date().toISOString(),
                    lastError: null,
                    retryCount: 0
                });
                console.log("[WhatsApp] Conexión establecida; QR temporal eliminado.");

                clearKeepAlive();

                keepAliveInterval = setInterval(async () => {

                    try {

                        await sock.sendPresenceUpdate(
                            "available"
                        );

                        console.log(
                            "Keep alive enviado"
                        );

                    } catch (err) {

                        console.log(
                            "⚠️ Error keep alive"
                        );
                    }

                }, 60000);
            }

            // ======================================
            // reconexión automática
            // ======================================

            if (connection === "close") {

                clearKeepAlive();
                clearQr();
                activeSocket = null;

                if (logoutEnProgreso) {
                    console.log("[WhatsApp] Cierre por desvinculación manual; reconexión automática omitida.");
                    return;
                }

                if (reconexionManual) {
                    console.log("[WhatsApp] Cierre por reconexión manual; reconexión automática omitida.");
                    return;
                }

                const shouldReconnect =

                    lastDisconnect?.error?.output
                        ?.statusCode !==
                    DisconnectReason.loggedOut;

                console.log(
                    "⚠️ WhatsApp desconectado. Código de estado:",
                    lastDisconnect?.error?.output?.statusCode,
                    lastDisconnect?.error?.message || ""
                );

                const lastError = lastDisconnect?.error?.message || null;
                updateWhatsappState({
                    status: shouldReconnect ? "RECONNECTING" : "LOGGED_OUT",
                    lastDisconnectAt: new Date().toISOString(),
                    lastError,
                    retryCount: shouldReconnect ? whatsappState.retryCount + 1 : whatsappState.retryCount
                });

                if (shouldReconnect) {

                    scheduleReconnect();
                } else {
                    console.log("[WhatsApp] Sesión cerrada; no se intentará reconectar.");
                }
            }
        }
    );

    // ==========================================
    // escuchar mensajes
    // ==========================================

    sock.ev.on(

        "messages.upsert",

        async ({ messages, type }) => {

            console.log(
                "TIPO EVENTO:",
                type
            );

            // ======================================
            // ignorar eventos históricos (append)
            // ======================================

            if (type === "append") {

                console.log(
                    "📦 Evento histórico ignorado; no se reprocesan pedidos."
                );

                return;
            }

            // ======================================
            // proteger contra sockets obsoletos
            // ======================================

            if (generation !== socketGeneration || activeSocket !== sock) {

                console.log(
                    `[WhatsApp] Lote de mensajes ignorado de socket obsoleto (generación ${generation}).`
                );

                return;
            }

            // ======================================
            // procesar todos los mensajes del lote
            // ======================================

            for (const msg of messages) {

                try {

                    // ==================================
                    // ignorar mensajes internos whatsapp
                    // ==================================

                    if (msg.message?.protocolMessage) {
                        continue;
                    }

                    // ==================================
                    // validar contenido
                    // ==================================

                    if (!msg.message) {

                        console.log(
                            "⛔ Mensaje vacío"
                        );

                        continue;
                    }

                    // ==================================
                    // extraer texto compatible MD
                    // ==================================

                    const text =

                        msg.message?.conversation ||

                        msg.message?.extendedTextMessage?.text ||

                        msg.message?.ephemeralMessage
                            ?.message?.extendedTextMessage?.text ||

                        msg.message?.ephemeralMessage
                            ?.message?.conversation ||

                        msg.message?.viewOnceMessage
                            ?.message?.extendedTextMessage?.text ||

                        msg.message?.viewOnceMessageV2
                            ?.message?.extendedTextMessage?.text;

                    // ==================================
                    // ignorar sin texto
                    // ==================================

                    if (!text) {

                        console.log(
                            "⛔ No se encontró pedido"
                        );

                        continue;
                    }

                    // ==================================
                    // detectar pedido
                    // ==================================

                    const isPedido =

                        text.includes("CLIENTE:") &&
                        text.includes("DNI:") &&
                        text.includes("TELEFONO:") &&
                        text.includes("DIRECCION:");

                    if (!isPedido) {

                        console.log(
                            "⛔ Mensaje ignorado"
                        );

                        continue;
                    }

                    // ==================================
                    // metadata 
                    // ==================================

                    const messageId =

                        msg.key?.id ||
                        crypto.randomUUID();

                    const remoteJid =
                        msg.key.remoteJid;

                    const fechaMensaje =

                        new Date(
                            Number(
                                msg.messageTimestamp
                            ) * 1000
                        );

                    // ==================================
                    // datos del remitente del pedido
                    // nombre: pushName del mensaje
                    // número: participant sin dominio
                    // ==================================

                    // ==================================
                    // datos del remitente del pedido
                    // ==================================

                    const vendedorNombre =
                        msg.pushName || "DESCONOCIDO";

                    const numeroVendedor =
                        String(
                            msg.key?.participant || remoteJid || ""
                        ).split("@")[0];

                    console.log("\n=================================================");

                    console.log(
                        "📦 NUEVO PEDIDO DETECTADO"
                    );

                    console.log(
                        "👤 Vendedor:",
                        vendedorNombre
                    );

                    console.log(
                        "📱 Número:",
                        numeroVendedor
                    );

                    console.log(
                        "🆔 MessageID:",
                        messageId
                    );

                    console.log(
                        "💬 Chat:",
                        remoteJid
                    );

                    console.log(
                        "================================================="
                    );

                    // ==================================
                    // parsear pedido
                    // ==================================

                    const pedido =
                        parsePedido(text);

                    pedido.messageId =
                        messageId;

                    pedido.remoteJid =
                        remoteJid;

                    pedido.fechaMensaje =
                        fechaMensaje;

                    pedido.vendedorNombre =
                        vendedorNombre;

                    console.log({

                        cliente: pedido.cliente,

                        telefono: pedido.telefono,

                        producto: pedido.producto,

                        messageId
                    });

                    // ==================================
                    // enviar backend
                    // (X-API-Key configurada en .env,
                    // ver .env.example)
                    // ==================================

                    const response =

                        await axios.post(

                            "http://localhost:8081/api/pedidos",

                            pedido,

                            {
                                headers: {
                                    "X-API-Key":
                                        process.env.WHATSAPP_API_KEY
                                }
                            }
                        );

                    if (!response.data) {

                        console.log(
                            "⚠️ Pedido duplicado"
                        );

                        continue;
                    }

                    console.log(
                        "✅ Pedido enviado backend"
                    );

                } catch (error) {

                    console.log(
                        "❌ Error procesando mensaje"
                    );

                    console.log(
                        error.message
                    );
                }
            }
        }
    );
}

setTimeout(() => {

    iniciarBotSeguro("arranque inicial");

}, 5000);

