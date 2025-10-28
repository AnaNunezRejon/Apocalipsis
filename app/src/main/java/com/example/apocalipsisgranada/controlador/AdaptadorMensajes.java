package com.example.apocalipsisgranada.controlador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.modelo.Mensaje;

import java.util.List;

public class AdaptadorMensajes extends RecyclerView.Adapter<AdaptadorMensajes.VistaMensaje> {
    /**
     * cuando quieres mostrar una lista de elementos (como tus mensajes o alertas), usas un RecyclerView.
     * Pero el RecyclerView no sabe cómo dibujar los datos por sí solo.
     * Necesita un adaptador que le diga:
     *      cuántos elementos hay,
     *      qué diseño XML usar para cada elemento (item_mensajes.xml),
     *      y cómo rellenar cada campo con los datos (texto, hora, color…).
     * Ese adaptador es tu clase AdaptadorMensajes.
     *
     * Si tuvieras una caja con muchas cartas (los mensajes),
     * el RecyclerView es la caja 📦,
     * el AdaptadorMensajes es quien reparte y coloca las cartas 🧍‍♀️,
     * y el ViewHolder (VistaMensaje) es la plantilla de una carta 🧾.
     */

    private final List<Mensaje> listaDeMensajes;
    private final Context contexto;

    // 🔹 Constructor
    public AdaptadorMensajes(List<Mensaje> listaDeMensajes, Context contexto) {
        this.listaDeMensajes = listaDeMensajes;
        this.contexto = contexto;
    }

    // 🔹 1. Crear la vista (infla el XML de cada item)
    @NonNull
    @Override
    public VistaMensaje onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista) {
        View vista = LayoutInflater.from(contexto).inflate(R.layout.item_mensajes, padre, false);
        return new VistaMensaje(vista);
    }
    // 🔹 2. Rellenar la vista con los datos del mensaje actual
    @Override
    public void onBindViewHolder(@NonNull VistaMensaje vista, int posicion) {
        Mensaje mensaje = listaDeMensajes.get(posicion);

        vista.textoMensaje.setText(mensaje.getTexto());
        vista.textoFecha.setText(mensaje.getHora()); // aquí guardamos la fecha simulada

        switch (mensaje.getTipo()) {
            case "guia":
                vista.contenedorMensaje.setBackgroundResource(R.drawable.tarjeta_azul_claro);
                vista.textoMensaje.setTextColor(contexto.getColor(R.color.white));
                vista.textoFecha.setTextColor(contexto.getColor(R.color.textoAzul)); // 🩵 para que se vea
                break;
            case "alerta":
            default:
                vista.contenedorMensaje.setBackgroundResource(R.drawable.tarjeta_blanca);
                vista.textoMensaje.setTextColor(contexto.getColor(R.color.textoOscuro));
                vista.textoFecha.setTextColor(contexto.getColor(R.color.textoGris));
                break;
        }
    }
    // 🔹 3. Indicar cuántos elementos hay en la lista
    @Override
    public int getItemCount() {
        return listaDeMensajes.size();
    }

    // 🔹 Clase interna que representa una fila (el item)

    /**
     * VistaMensaje es tu clase interna estática, la que define cómo es cada "fila" de tu lista:
     * qué TextView, ImageView o LinearLayout contiene,
     * y cómo los vinculas con los datos (findViewById()).
     */
    public static class VistaMensaje extends RecyclerView.ViewHolder {
        TextView textoMensaje, textoFecha;
        LinearLayout contenedorMensaje;

        public VistaMensaje(@NonNull View itemView) {
            super(itemView);
            textoMensaje = itemView.findViewById(R.id.textoMensaje);
            textoFecha = itemView.findViewById(R.id.textoFecha);
            contenedorMensaje = itemView.findViewById(R.id.contenedorMensaje);
        }
    }
}
