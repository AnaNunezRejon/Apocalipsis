package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

public class VistaPrincipal extends BaseActivity {

    private RecyclerView recyclerMensajes;
    private SharedPreferences preferencias;
    private Button botonAvanzar, botonReiniciar;
    private ImageView escudo;
    private TextView textoFecha;

    private final List<Mensaje> alertas = new ArrayList<>();
    private final List<Mensaje> guias = new ArrayList<>();
    private final List<Mensaje> mostrados = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);

        boolean primeraVez = preferencias.getBoolean("primer_arranque", true);
        if (primeraVez) {
            SharedPreferences.Editor editor = preferencias.edit();

            // ⚠️ NO hacemos clear() para no perder nombreUsuario
            editor.putBoolean("primer_arranque", false);
            editor.putLong("fechaInicio", System.currentTimeMillis());
            editor.putInt("diaActual", 1);
            editor.putInt("indiceMensajeDia", 0);
            editor.apply();
        }

        recyclerMensajes = findViewById(R.id.recyclerPrincipal);
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));

        escudo = findViewById(R.id.escudo);
        botonAvanzar = findViewById(R.id.botonAvanzar);
        botonReiniciar = findViewById(R.id.botonReiniciar);
        textoFecha = findViewById(R.id.textoFecha);

        configurarModoDesarrolladorComun();
        configurarMenuInferior();
        mostrarSaludoUsuario();

        cargarArchivo("alertas.json", "alerta", alertas);
        cargarArchivo("guias.json", "guia", guias);

        botonAvanzar.setOnClickListener(v -> avanzarDia());
        botonReiniciar.setOnClickListener(v -> reiniciarSimulacionComun());

        actualizarTextoModoDesarrollador();
        mostrarMensajesIniciales();
    }

    // ============================================================
    // 📅 Avanzar día o mensaje (alerta + guía)
    // ============================================================
    private void avanzarDia() {
        int diaActual = preferencias.getInt("diaActual", 1);
        int indice = preferencias.getInt("indiceMensajeDia", 0);

        // 🔹 Obtener todos los pares alerta+guía del día actual
        List<List<Mensaje>> paresDia = obtenerParesDelDia(diaActual);

        if (indice < paresDia.size() - 1) {
            // Hay más pares dentro del mismo día
            indice++;
            preferencias.edit().putInt("indiceMensajeDia", indice).apply();
        } else {
            // Pasamos al siguiente día
            diaActual++;
            indice = 0;
            preferencias.edit()
                    .putInt("diaActual", diaActual)
                    .putInt("indiceMensajeDia", 0)
                    .apply();
        }

        Toast.makeText(this, "Avanzaste al día " + diaActual, Toast.LENGTH_SHORT).show();
        mostrarMensajesIniciales();
    }

    // ============================================================
    // 🟢 Mostrar mensajes acumulados hasta el día actual
    // ============================================================
    protected void mostrarMensajesIniciales() {
        mostrados.clear();

        // Mensaje inicial de ejemplo
        mostrados.add(new Mensaje(
                0,
                "23/09/2025",
                "🌧️ Alerta por lluvias en la provincia de Granada.",
                "false",
                "alerta"
        ));

        int diaActual = preferencias.getInt("diaActual", 1);
        int indice = preferencias.getInt("indiceMensajeDia", 0);

        // 🔹 Mostrar todos los días anteriores completos
        for (int d = 1; d < diaActual; d++) {
            List<List<Mensaje>> paresDia = obtenerParesDelDia(d);
            for (List<Mensaje> par : paresDia) {
                mostrados.addAll(par);
            }
        }

        // 🔹 Mostrar los pares del día actual hasta el índice actual
        List<List<Mensaje>> paresHoy = obtenerParesDelDia(diaActual);
        for (int i = 0; i <= indice && i < paresHoy.size(); i++) {
            mostrados.addAll(paresHoy.get(i));
        }

        // 🔹 Ordenar por día (descendente)
        Collections.sort(mostrados, (m1, m2) -> Integer.compare(m2.getDia(), m1.getDia()));

        recyclerMensajes.setAdapter(new AdaptadorMensajes(mostrados, this));

        textoFecha.setText("Hoy es " + obtenerFechaSimulada(diaActual));

        // 🔔 Notificar solo el último mensaje nuevo (la alerta del par actual)
        if (!paresHoy.isEmpty() && indice < paresHoy.size()) {
            Mensaje nuevaAlerta = paresHoy.get(indice).get(0); // el primer mensaje del par (alerta)
            if ("alerta".equals(nuevaAlerta.getTipo()) && !yaNotificada(diaActual * 10 + indice)) {
                reproducirSonido(nuevaAlerta);
                mostrarNotificacion(nuevaAlerta);
                marcarComoNotificada(diaActual * 10 + indice);
            }
        }
    }

    // ============================================================
    // 🧩 Obtener pares de mensajes (alerta + guía) por día
    // ============================================================
    private List<List<Mensaje>> obtenerParesDelDia(int dia) {
        List<Mensaje> alertasDia = new ArrayList<>();
        List<Mensaje> guiasDia = new ArrayList<>();

        for (Mensaje alerta : alertas) if (alerta.getDia() == dia) alertasDia.add(alerta);
        for (Mensaje guia : guias) if (guia.getDia() == dia) guiasDia.add(guia);

        List<List<Mensaje>> pares = new ArrayList<>();
        int total = Math.max(alertasDia.size(), guiasDia.size());

        for (int i = 0; i < total; i++) {
            List<Mensaje> par = new ArrayList<>();
            if (i < alertasDia.size()) par.add(alertasDia.get(i));
            if (i < guiasDia.size()) par.add(guiasDia.get(i));
            pares.add(par);
        }

        return pares;
    }

    // ============================================================
    // 📂 Cargar alertas o guías desde JSON
    // ============================================================
    private void cargarArchivo(String archivo, String tipo, List<Mensaje> destino) {
        try {
            InputStream is = getAssets().open(archivo);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Mensaje mensaje = new Mensaje(
                        obj.getInt("dia"),
                        obtenerFechaSimulada(obj.getInt("dia")),
                        obj.getString("mensaje"),
                        obj.optString("sonido", "false"),
                        tipo
                );
                destino.add(mensaje);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
