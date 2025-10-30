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
 * 💬 Resumen rápido del flujo — AdaptadorMensajes.java
 *
 * Esta clase controla **cómo se muestran los mensajes (alertas o guías)**
 * dentro del RecyclerView.
 * El RecyclerView es la lista que se ve en pantalla, pero necesita un "puente"
 * para saber **qué dibujar** y **cómo hacerlo**.
 * Ese puente es el AdaptadorMensajes.
 *
 * 🧠 En resumen:
 * RecyclerView = la caja donde se muestran los mensajes 📦
 * AdaptadorMensajes = el repartidor que decide qué mensaje va en cada posición 🧍‍♂️
 * VistaMensaje = la plantilla de una sola carta o fila 🧾
 *
 * ============================================================
 * 🧩 Estructura general
 * ============================================================
 * AdaptadorMensajes extiende de RecyclerView.Adapter
 * ├─ Eso significa que "adapta" los datos de una lista (List<Mensaje>)
 * │  para que puedan mostrarse en la interfaz.
 * └─ Usa una clase interna llamada VistaMensaje (un ViewHolder)
 *    que representa una sola tarjeta de mensaje.
 *
 * ============================================================
 * 🟩 Constructor AdaptadorMensajes(List<Mensaje>, Context)
 * ============================================================
 * ├─ Recibe la lista de mensajes (alertas y guías)
 * └─ Recibe el contexto (para poder acceder a colores, recursos, etc.)
 *
 * ============================================================
 * 🧱 onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista)
 * ============================================================
 * ├─ Se ejecuta cuando el RecyclerView necesita crear un nuevo “item”.
 * ├─ Infla (crea) la vista de cada mensaje usando el XML item_mensajes.xml.
 * └─ Devuelve un nuevo objeto VistaMensaje que contendrá esa vista.
 *
 * ============================================================
 * 🖋️ onBindViewHolder(@NonNull VistaMensaje vista, int posicion)
 * ============================================================
 * ├─ Se ejecuta cada vez que hay que mostrar un mensaje en pantalla.
 * ├─ Obtiene el mensaje correspondiente a la posición de la lista.
 * ├─ Rellena los campos:
 * │     textoMensaje → el contenido del mensaje (alerta o guía)
 * │     textoFecha   → la fecha simulada
 * ├─ Cambia los colores según el tipo:
 * │     🟦 “guia” → fondo azul claro, texto blanco
 * │     ⚪ “alerta” → fondo blanco, texto oscuro
 * └─ Así cada tipo de mensaje tiene su estilo visual.
 *
 * ============================================================
 * 📏 getItemCount()
 * ============================================================
 * ├─ Devuelve cuántos mensajes hay en total en la lista.
 * └─ El RecyclerView lo usa para saber cuántas filas dibujar.
 *
 * ============================================================
 * 🧾 Clase interna estática VistaMensaje (extends RecyclerView.ViewHolder)
 * ============================================================
 * ├─ Representa una “fila” del RecyclerView.
 * ├─ Contiene las vistas que forman un mensaje:
 * │     - TextView textoMensaje
 * │     - TextView textoFecha
 * │     - LinearLayout contenedorMensaje
 * ├─ En su constructor, busca los elementos con findViewById().
 * └─ Sirve como “molde reutilizable” para que el RecyclerView no cree vistas nuevas
 *    cada vez, sino que las recicle (de ahí su nombre: RecyclerView ♻️).
 *
 * ============================================================
 * 🧩 En resumen:
 *  AdaptadorMensajes:
 *   - Crea la plantilla de cada mensaje.
 *   - La rellena con los datos correctos.
 *   - Le da estilo (color, fondo, texto).
 *   - Indica cuántos mensajes hay que mostrar.
 *
 * 🔁 Relación con otras clases:
 *  ├─ VistaPrincipal → crea AdaptadorMensajes(mostrados, this)
 *  ├─ VistaGuia → crea AdaptadorMensajes(listaGuias, this)
 *  └─ VistaHistorial → crea AdaptadorMensajes(listaAlertas, this)
 *
 * 💡 Conceptos clave:
 *  - RecyclerView.Adapter: clase que conecta los datos con la interfaz.
 *  - ViewHolder: patrón que optimiza la memoria reciclando vistas.
 *  - Context: permite acceder a recursos, colores, layouts, etc.
 *  - @NonNull: indica que un parámetro o retorno no puede ser null.
 *
 * En definitiva, este archivo se encarga de **mostrar correctamente cada mensaje**
 * en pantalla con su color, texto y fecha, de forma optimizada y ordenada.
 */

