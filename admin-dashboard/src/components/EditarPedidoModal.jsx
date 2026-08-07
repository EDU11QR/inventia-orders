import { useState } from "react";

function EditarPedidoModal({

    pedido,
    onClose,
    onGuardar

}) {

    const [form, setForm] = useState({

        cliente: pedido.cliente,
        dni: pedido.dni,
        telefono: pedido.telefono,
        direccion: pedido.direccion,
        ciudad: pedido.ciudad,
        producto: pedido.producto
    });

    const handleChange = (e) => {

        setForm({

            ...form,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = () => {

        onGuardar(form);
    };

    return (

        <div
            className="
                fixed
                inset-0
                bg-black/30
                flex
                items-center
                justify-center
                z-50
            "
        >

            <div
                className="
                    w-full
                    max-w-xl
                    rounded-xl
                    border
                    border-slate-200
                    bg-white
                    p-6
                    shadow-xl
                "
            >

                <h2 className="mb-1 text-lg font-semibold text-slate-900">
                    Editar Pedido
                </h2>

                <p className="mb-5 text-sm text-slate-500">
                    Actualice los datos del pedido
                </p>

                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">

                    <input
                        type="text"
                        name="cliente"
                        value={form.cliente}
                        onChange={handleChange}
                        placeholder="Cliente"
                        className="w-full min-w-0 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 md:col-span-2"
                    />

                    <input
                        type="text"
                        name="dni"
                        value={form.dni}
                        onChange={handleChange}
                        placeholder="DNI"
                        className="w-full min-w-0 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />

                    <input
                        type="text"
                        name="telefono"
                        value={form.telefono}
                        onChange={handleChange}
                        placeholder="Teléfono"
                        className="w-full min-w-0 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />

                    <input
                        type="text"
                        name="direccion"
                        value={form.direccion}
                        onChange={handleChange}
                        placeholder="Dirección"
                        className="w-full min-w-0 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 md:col-span-2"
                    />

                    <input
                        type="text"
                        name="ciudad"
                        value={form.ciudad}
                        onChange={handleChange}
                        placeholder="Ciudad"
                        className="w-full min-w-0 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 md:col-span-2"
                    />

                    <input
                        type="text"
                        name="producto"
                        value={form.producto}
                        onChange={handleChange}
                        placeholder="Producto"
                        className="w-full min-w-0 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 shadow-sm transition focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 md:col-span-2"
                    />

                </div>

                <div className="mt-6 flex justify-end gap-3">

                    <button
                        onClick={onClose}
                        className="
                            inline-flex
                            items-center
                            justify-center
                            rounded-lg
                            border
                            border-slate-300
                            bg-white
                            px-4
                            py-2.5
                            text-sm
                            font-medium
                            text-slate-700
                            shadow-sm
                            transition-colors
                            hover:bg-slate-50
                            focus:outline-none
                            focus:ring-2
                            focus:ring-blue-500/20
                        "
                    >
                        Cancelar
                    </button>

                    <button
                        onClick={handleSubmit}
                        className="
                            inline-flex
                            items-center
                            justify-center
                            rounded-lg
                            bg-blue-600
                            px-4
                            py-2.5
                            text-sm
                            font-semibold
                            text-white
                            shadow-sm
                            transition-colors
                            hover:bg-blue-700
                            focus:outline-none
                            focus:ring-2
                            focus:ring-blue-500
                            focus:ring-offset-2
                        "
                    >
                        Guardar
                    </button>

                </div>

            </div>

        </div>
    );
}

export default EditarPedidoModal;