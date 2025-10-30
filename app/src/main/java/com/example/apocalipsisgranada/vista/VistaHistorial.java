package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Controlador;
import com.example.apocalipsisgranada.modelo.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VistaHistorial extends AppCompatActivity {

    private SharedPreferences prefs;
    private RecyclerView recyclerHistorial;
    private List<Mensaje> listaAlertas = new ArrayList<>();

    // ============================================================
    // CICLO DE VIDA
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));

        // Configuración general
        Controlador.configurarModoDesarrolladorComun(this);
        ManejadorVistas.configurarElementosComunes(this);

        // Cargar alertas
        cargarAlertas();
    }

    // ============================================================
    // CARGAR ALERTAS
    // ============================================================
    public void cargarAlertas() {
        listaAlertas.clear();

        try {
            InputStream is = getAssets().open("alertas.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            int diaActual = prefs.getInt("diaActual", 1);

            // Mensaje inicial del sistema
            listaAlertas.add(new Mensaje(
                    0,
                    Controlador.obtenerFechaSimulada(prefs, 0),
                    "Registro del Sistema de Alertas — Gobierno de España",
                    "false",
                    "alerta"
            ));

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int dia = obj.getInt("dia");
                String texto = obj.getString("mensaje");
                String sonido = obj.optString("sonido", "false");

                // Solo mostrar alertas hasta el día actual
                if (dia <= diaActual) {
                    listaAlertas.add(new Mensaje(
                            dia,
                            Controlador.obtenerFechaSimulada(prefs, dia),
                            texto,
                            sonido,
                            "alerta"
                    ));
                }
            }

            // Ordenar de más recientes a más antiguas
            Collections.reverse(listaAlertas);

            recyclerHistorial.setAdapter(new AdaptadorMensajes(listaAlertas, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // ACTUALIZAR HISTORIAL (AL AVANZAR DÍA)
    // ============================================================
    public void actualizarHistorial() {
        cargarAlertas();
    }
}


