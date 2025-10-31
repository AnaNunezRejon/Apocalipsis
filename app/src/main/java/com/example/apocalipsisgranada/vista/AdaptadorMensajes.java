package com.example.apocalipsisgranada.vista;

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

/**
 * Cuando quieres mostrar una lista de elementos (como los mensajes o alertas), se usa un RecyclerView.
 * Pero el RecyclerView no sabe cómo dibujar los datos por sí solo.
 * Necesita un adaptador que le diga:
 *      cuántos elementos hay,
 *      qué diseño XML usar para cada elemento (item_mensajes.xml),
 *      y cómo rellenar cada campo con los datos (texto, hora, color…).
 * Ese adaptador es la clase AdaptadorMensajes.
 *
 * Si tuvieras una caja con muchas cartas (los mensajes),
 * el RecyclerView es la caja 📦,
 * el AdaptadorMensajes es quien reparte y coloca las cartas 🧍‍♀️,
 * y el ViewHolder (VistaMensaje) es la plantilla de una carta 🧾.
 */

public class AdaptadorMensajes extends RecyclerView.Adapter<AdaptadorMensajes.VistaMensaje> {
    /**
     * Este adaptador (AdaptadorMensajes) sirve para un RecyclerView y va a usar una vista personalizada llamada VistaMensaje omo plantilla para cada elemento de la list
     *
     * RecyclerView	Es el componente de Android que muestra listas o rejillas de elementos (como mensajes, contactos, fotos, etc.).
     * .Adapter	Es la clase “ayudante” que se encarga de crear y rellenar cada elemento de la lista.
     * <AdaptadorMensajes.VistaMensaje>	Es el tipo de vista (ViewHolder) que este adaptador usará para mostrar cada elemento.
     */


    private final List<Mensaje> listaDeMensajes;
    private final Context contexto;

    // 🔹 Constructor
    public AdaptadorMensajes(List<Mensaje> listaDeMensajes, Context contexto) {
        this.listaDeMensajes = listaDeMensajes;
        this.contexto = contexto;
    }

    // 🔹 1. Crear la vista - Devuelve un objeto de tipo VistaMensaje
    @NonNull //Este parámetro o valor nunca será null, así que puedes usarlo sin comprobarlo.
    @Override
    public VistaMensaje onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista) { //El contenedor (la vista padre donde se colocan los ítems) nunca será null.
        View vista = LayoutInflater.from(contexto).inflate(R.layout.item_mensajes, padre, false);
        return new VistaMensaje(vista);
    }
    // 🔹 2. Rellena la vista con datos - Recibe un objeto VistaMensaje
    @Override
    public void onBindViewHolder(@NonNull VistaMensaje vista, int posicion) { //El objeto “molde” de cada mensaje (ViewHolder) siempre existe.
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

        public VistaMensaje(@NonNull View itemView) { //La vista XML que representa el mensaje nunca será null cuando se crea.
            super(itemView);
            textoMensaje = itemView.findViewById(R.id.textoMensaje);
            textoFecha = itemView.findViewById(R.id.textoFecha);
            contenedorMensaje = itemView.findViewById(R.id.contenedorMensaje);
        }
    }
}

/**
 * ============================================================
 * 💬 Clase: AdaptadorMensajes.java
 * ============================================================
 *
 * Adaptador personalizado para enlazar los datos del modelo `Mensaje`
 * con el `RecyclerView` utilizado en las vistas principales
 * (VistaPrincipal, VistaHistorial, VistaGuia).
 *
 * Es responsable de **inflar el diseño de cada tarjeta de mensaje**
 * y aplicar los estilos visuales según el tipo de contenido.
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales
 * ------------------------------------------------------------
 *
 * 1️⃣ **onCreateViewHolder()**
 *     - Infla el layout de tarjeta (`item_mensaje.xml`).
 *
 * 2️⃣ **onBindViewHolder()**
 *     - Asigna los datos del mensaje (texto, fecha, tipo).
 *     - Cambia el color y estilo según el tipo de mensaje:
 *         🟡 Alerta → fondo amarillo / texto oscuro.
 *         🔵 Guía → fondo azul / texto blanco.
 *
 * 3️⃣ **getItemCount()**
 *     - Devuelve el número total de mensajes a mostrar.
 *
 * ------------------------------------------------------------
 * 🗂️ Elementos visuales manejados
 * ------------------------------------------------------------
 *
 * - `@id/textoMensaje` → cuerpo del mensaje.
 * - `@id/textoFecha` → fecha de emisión.
 * - `@id/contenedorMensaje` → tarjeta visual de fondo.
 *
 * ------------------------------------------------------------
 * 🔁 Flujo de funcionamiento
 * ------------------------------------------------------------
 *
 * 1️⃣ La vista crea el adaptador pasando la lista de mensajes.
 * 2️⃣ Cada elemento se muestra en el RecyclerView.
 * 3️⃣ Si el día cambia, la lista se actualiza con los nuevos mensajes.
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `AdaptadorMensajes.java` traduce los datos del modelo
 * en una presentación visual uniforme, estética y funcional.
 *
 * Es la pieza clave para conectar la narrativa (los mensajes)
 * con la experiencia visual del usuario.
 *
 * ============================================================
 */
