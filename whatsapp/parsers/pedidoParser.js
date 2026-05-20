function extraerCampo(texto, campo) {

    const regex = new RegExp(`${campo}:\\s*(.*)`, "i");

    const match = texto.match(regex);

    return match ? match[1].trim() : "";
}

// ==========================================
// extraer productos múltiples
// ==========================================

function extraerProductos(texto) {

    const match = texto.match(/PRODUCTO:\s*([\s\S]*)/i);

    if (!match) return "";

    return match[1]
        .split(/\r?\n/)
        .map(item => item.trim())
        .filter(item => item.length > 0)
        .join("|");
}

export function parsePedido(texto) {

    return {

        cliente: extraerCampo(texto, "CLIENTE"),

        dni: extraerCampo(texto, "DNI"),

        telefono: extraerCampo(texto, "TELEFONO"),

        direccion: extraerCampo(texto, "DIRECCION"),

        ciudad: extraerCampo(texto, "CIUDAD"),

        producto: extraerProductos(texto)
    };
}