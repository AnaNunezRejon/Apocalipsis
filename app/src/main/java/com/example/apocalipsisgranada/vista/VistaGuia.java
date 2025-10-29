package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.AdaptadorMensajes;
import com.example.apocalipsisgranada.modelo.Mensaje;
import com.example.apocalipsisgranada.controlador.Controlador;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Resumen rápido del flujo — VistaGuia
 *
 * Esta pantalla muestra las guías (consejos) que corresponden
 * al día actual y a los días anteriores.
 *
 * La app abre esta pantalla → entra en onCreate()
 * Dentro de onCreate() se configuran los ajustes comunes (modo desarrollador, menú, cabecera...)
 * Se abre el archivo "guias.json" con todas las guías del juego
 * Se calculan los días transcurridos o el día actual según el modo
 * Se filtran las guías para mostrar solo las de los días que ya han pasado
 * Se crea una lista con esas guías
 * Se muestran ordenadas en el RecyclerView (la lista de la pantalla)

 */


public class VistaGuia extends AppCompatActivity {

    private RecyclerView recyclerView;
    private final List<Mensaje> listaGuias = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guia);

        // 🟡 Cargar metodos comunes
        Controlador.configurarModoDesarrolladorComun(this);
        Controlador.configurarMenuInferior(this);
        Controlador.actualizarCabecera(this);
        Controlador.mostrarSaludoUsuario(this);
        Controlador.actualizarColoresModoDesarrollador(this);
        Controlador.mostrarTextoModoDesarrollador(this);

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);
        //Porque la pantalla de Guías necesita saber en qué día está el jugador para mostrar solo las guías que correspondan hasta ese día.

        // Recycler con las guías
        recyclerView = findViewById(R.id.recyclerGuia);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarGuias();
    }

    // ============================================================
    // CARGAR LAS GUÍAS DEL JSON (solo las de días pasados)
    // ============================================================
    public void cargarGuias() {
        try {
            InputStream is = getAssets().open("guias.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            boolean modoDev = prefs.getBoolean("modoDesarrollador", false);//saber si está en modo desarrollador
            int diaActual;

            if (modoDev) {
                diaActual = prefs.getInt("diaActual", 1);//i el modo desarrollador está activado, el día actual se saca directamente del número guardado
            } else {
                //Calcular días transcurridos desde la primera apertura real
                //Si no está en modo desarrollador, se calcula automáticamente el número de días que han pasado desde que se guardó
                long fechaInicio = prefs.getLong("fechaInicio", 0);
                if (fechaInicio == 0) {
                    fechaInicio = System.currentTimeMillis();
                    prefs.edit().putLong("fechaInicio", fechaInicio).apply();//saber cuándo empezó la simulación
                }
                long diasPasados = (System.currentTimeMillis() - fechaInicio) / (1000 * 60 * 60 * 24);//Convertimos milisegundos a días
                //System.currentTimeMillis() - Devuelve la fecha y hora actual,
                diaActual = (int) diasPasados + 1;
            }

            listaGuias.clear();

            // Guía inicial “antigua” (día 0)
            listaGuias.add(new Mensaje(
                    0,
                    "23/09/2025",
                    "🧥 Lleva abrigo y paraguas ante la alerta por lluvias.",
                    "false",
                    "guia"
            ));

            SharedPreferences preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);

            // Añadir solo guías hasta el día actual
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int dia = obj.getInt("dia");
                if (dia <= diaActual) {

                    // Calcular fecha simulada para esta guía
                    String fecha = Controlador.obtenerFechaSimulada(preferencias, dia);

                    Mensaje mensaje = new Mensaje(
                            dia,
                            fecha,
                            obj.getString("mensaje"),
                            obj.optString("sonido", "false"),
                            "guia"
                    );
                    listaGuias.add(mensaje);
                }
            }

            // ORDENAR DE MÁS RECIENTE A MÁS ANTIGUO
            listaGuias.sort((m1, m2) -> {
                if (m1.getDia() == m2.getDia()) {
                    return m2.getHora().compareTo(m1.getHora());
                }
                return Integer.compare(m2.getDia(), m1.getDia());
            });

            // ACTUALIZAR ADAPTADOR
            recyclerView.setAdapter(new AdaptadorMensajes(listaGuias, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarGuias() {
        cargarGuias();
    }

}

/**
 * 🧭 Resumen rápido del flujo — VistaGuia
 *
 * Esta pantalla muestra las guías (consejos) que corresponden
 * al día actual y a los días anteriores.
 *
 * La app abre esta pantalla → entra en onCreate()
 * Dentro de onCreate() se configuran los ajustes comunes (modo desarrollador, menú, cabecera...)
 * Se abre el archivo "guias.json" con todas las guías del juego
 * Se calculan los días transcurridos o el día actual según el modo
 * Se filtran las guías para mostrar solo las de los días que ya han pasado
 * Se crea una lista con esas guías
 * Se muestran ordenadas en el RecyclerView (la lista de la pantalla)
 *
 * 🟩 onCreate()
 *  ├─ Controlador.configurarModoDesarrolladorComun(this)
 *  ├─ Controlador.configurarMenuInferior(this)
 *  ├─ Controlador.actualizarCabecera(this)
 *  ├─ Controlador.mostrarSaludoUsuario(this)
 *  ├─ Controlador.actualizarColoresModoDesarrollador(this)
 *  ├─ Controlador.mostrarTextoModoDesarrollador(this)
 *  ├─ prefs = getSharedPreferences("configuracion", MODE_PRIVATE)
 *  ├─ recyclerView.setLayoutManager(new LinearLayoutManager(this))
 *  └─ cargarGuias()
 *
 * 🟨 cargarGuias()
 *  ├─ Abre el archivo "guias.json" desde /assets
 *  ├─ Convierte su contenido a un JSONArray
 *  ├─ Comprueba si el usuario está en modo desarrollador
 *  │     ├─ Si está en modo dev → usa el día actual guardado en prefs
 *  │     └─ Si NO está en modo dev → calcula los días reales desde fechaInicio
 *  ├─ Limpia la lista anterior (listaGuias.clear())
 *  ├─ Añade una guía inicial fija (día 0, la del abrigo)
 *  ├─ Recorre el JSON con un bucle for:
 *  │     ├─ Lee cada guía
 *  │     ├─ Comprueba si su día ≤ día actual
 *  │     ├─ Calcula la fecha simulada con Controlador.obtenerFechaSimulada()
 *  │     └─ Añade el mensaje a la listaGuias
 *  ├─ Ordena las guías por día (de más reciente a más antiguo)
 *  └─ recyclerView.setAdapter(new AdaptadorMensajes(listaGuias, this))
 *
 * 🟪 actualizarGuias()
 *  └─ Llama a cargarGuias() para refrescar la lista si cambian los días
 */

