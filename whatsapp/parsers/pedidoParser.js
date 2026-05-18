function extraerCampo(texto, campo) {

    const regex = new RegExp(`${campo}:\\s*(.*)`, "i");

    const match = texto.match(regex);

    return match ? match[1].trim() : "";
}

export function parsePedido(texto) {

    return {
        cliente: extraerCampo(texto, "CLIENTE"),
        dni: extraerCampo(texto, "DNI"),
        telefono: extraerCampo(texto, "TELEFONO"),
        direccion: extraerCampo(texto, "DIRECCION"),
        producto: extraerCampo(texto, "PRODUCTO")
    };
}