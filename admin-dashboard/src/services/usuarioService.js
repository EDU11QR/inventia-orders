import api from "./api";

export async function listarUsuarios() {
    const { data } = await api.get("/api/usuarios");
    return data;
}

export async function crearUsuario(usuario) {
    const { data } = await api.post("/api/usuarios", usuario);
    return data;
}

export async function actualizarUsuario(id, usuario) {
    const { data } = await api.put(`/api/usuarios/${id}`, usuario);
    return data;
}

export async function cambiarPassword(id, password) {
    const { data } = await api.put(`/api/usuarios/${id}/password`, { password });
    return data;
}

export async function activarUsuario(id) {
    const { data } = await api.put(`/api/usuarios/${id}/activar`);
    return data;
}

export async function desactivarUsuario(id) {
    const { data } = await api.put(`/api/usuarios/${id}/desactivar`);
    return data;
}
