import axios from "axios";
import qrcode from "qrcode-terminal";
import crypto from "crypto";

import {
    makeWASocket,
    useMultiFileAuthState,
    DisconnectReason
} from "@whiskeysockets/baileys";

import { parsePedido } from "./parsers/pedidoParser.js";

async function startBot() {

    // ==========================================
    // sesión persistente whatsapp
    // ==========================================

    const { state, saveCreds } =

        await useMultiFileAuthState("auth");

    // ==========================================
    // socket whatsapp
    // ==========================================

    const sock = makeWASocket({

        auth: state,

        syncFullHistory: false,

        fireInitQueries: false,

        markOnlineOnConnect: false
    });

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

        ({ connection, qr, lastDisconnect }) => {

            // ======================================
            // qr consola
            // ======================================

            if (qr) {

                qrcode.generate(
                    qr,
                    { small: true }
                );
            }

            // ======================================
            // conexión exitosa
            // ======================================

            if (connection === "open") {

                console.log(
                    "✅ WhatsApp conectado"
                );
            }

            // ======================================
            // reconexión automática
            // ======================================

            if (connection === "close") {

                const shouldReconnect =

                    lastDisconnect?.error?.output
                        ?.statusCode !==
                    DisconnectReason.loggedOut;

                console.log(
                    "⚠️ WhatsApp desconectado"
                );

                if (shouldReconnect) {

                    console.log(
                        "🔄 Reconectando..."
                    );

                    startBot();
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

            try {

                // ======================================
                // ignorar append históricos
                // ======================================

                if (type === "append") {

                    console.log(
                        "📦 Recuperando mensajes offline..."
                    );
                }

                // ======================================
                // obtener mensaje
                // ======================================

                const msg = messages[0];

                // ======================================
                // ignorar mensajes internos whatsapp
                // ======================================

                if (
                    msg.message?.protocolMessage
                ) {
                    return;
                }

                // ======================================
                // validar contenido
                // ======================================

                if (!msg.message) {

                    console.log(
                        "⛔ Mensaje vacío"
                    );

                    return;
                }

                // ======================================
                // extraer texto compatible MD
                // ======================================

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

                // ======================================
                // ignorar sin texto
                // ======================================

                if (!text) {

                    console.log(
                        "⛔ No se encontró texto"
                    );

                    return;
                }

                // ======================================
                // detectar pedido
                // ======================================

                const isPedido =

                    text.includes("CLIENTE:") &&
                    text.includes("DNI:") &&
                    text.includes("TELEFONO:") &&
                    text.includes("DIRECCION:");

                if (!isPedido) {

                    console.log(
                        "⛔ Mensaje ignorado"
                    );

                    return;
                }

                // ======================================
                // metadata
                // ======================================

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

                console.log("\n========================");

                console.log(
                    "📦 NUEVO PEDIDO DETECTADO"
                );

                console.log(
                    "📨 MessageID:",
                    messageId
                );

                console.log(
                    "📱 Chat:",
                    remoteJid
                );

                console.log("========================");

                // ======================================
                // parsear pedido
                // ======================================

                const pedido =
                    parsePedido(text);

                pedido.messageId =
                    messageId;

                pedido.remoteJid =
                    remoteJid;

                pedido.fechaMensaje =
                    fechaMensaje;

                console.log({

                    cliente: pedido.cliente,

                    telefono: pedido.telefono,

                    producto: pedido.producto,

                    messageId
                });

                // ======================================
                // enviar backend
                // ======================================

                const response =

                    await axios.post(

                        "http://localhost:8081/api/pedidos",

                        pedido
                    );

                if (!response.data) {

                    console.log(
                        "⚠️ Pedido duplicado"
                    );

                    return;
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
    );
}

startBot();

