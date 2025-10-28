package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.AdaptadorMensajes;
import com.example.apocalipsisgranada.modelo.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VistaGuia extends BaseActivity {

    private RecyclerView recyclerView;
    private final List<Mensaje> listaGuias = new ArrayList<>();
    private SharedPreferences prefs;
    private TextView textoModo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guia);

        // 🟡 Cargar metodos comunes
        configurarModoDesarrolladorComun();
        configurarMenuInferior();
        actualizarCabecera();
        mostrarSaludoUsuario();
        actualizarColoresModoDesarrollador();
        mostrarTextoModoDesarrollador();

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);

        // 🔹 Recycler con las guías
        recyclerView = findViewById(R.id.recyclerGuia);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarGuias();
    }

    // ============================================================
    // 📘 CARGAR LAS GUÍAS DEL JSON (solo las de días pasados)
    // ============================================================
    private void cargarGuias() {
        try {
            InputStream is = getAssets().open("guias.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
            int diaActual;

            if (modoDev) {
                diaActual = prefs.getInt("diaActual", 1);
            } else {
                // 🟢 Calcular días transcurridos desde la primera apertura real
                long fechaInicio = prefs.getLong("fechaInicio", 0);
                if (fechaInicio == 0) {
                    fechaInicio = System.currentTimeMillis();
                    prefs.edit().putLong("fechaInicio", fechaInicio).apply();
                }
                long diasPasados = (System.currentTimeMillis() - fechaInicio) / (1000 * 60 * 60 * 24);
                diaActual = (int) diasPasados + 1;
            }

            listaGuias.clear();

            // 🔹 Guía inicial “antigua” (día 0)
            listaGuias.add(new Mensaje(
                    0,
                    "23/09/2025",
                    "🧥 Lleva abrigo y paraguas ante la alerta por lluvias.",
                    "false",
                    "guia"
            ));

            // 🔹 Añadir solo guías hasta el día actual
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int dia = obj.getInt("dia");
                if (dia <= diaActual) {

                    // 🟢 Calcular fecha simulada para esta guía
                    String fecha = obtenerFechaSimulada(dia);

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

            // 🔽 ORDENAR DE MÁS RECIENTE A MÁS ANTIGUO
            listaGuias.sort((m1, m2) -> {
                if (m1.getDia() == m2.getDia()) {
                    return m2.getHora().compareTo(m1.getHora());
                }
                return Integer.compare(m2.getDia(), m1.getDia());
            });

            // 🔽 ACTUALIZAR ADAPTADOR
            recyclerView.setAdapter(new AdaptadorMensajes(listaGuias, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarGuias() {
        cargarGuias();
    }


}
