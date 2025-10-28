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

    private final List<Mensaje> listaDeMensajes;
    private final Context contexto;

    public AdaptadorMensajes(List<Mensaje> listaDeMensajes, Context contexto) {
        this.listaDeMensajes = listaDeMensajes;
        this.contexto = contexto;
    }

    @NonNull
    @Override
    public VistaMensaje onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista) {
        View vista = LayoutInflater.from(contexto).inflate(R.layout.item_mensajes, padre, false);
        return new VistaMensaje(vista);
    }

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

    @Override
    public int getItemCount() {
        return listaDeMensajes.size();
    }

    // 📦 Clase interna para vincular vistas
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
