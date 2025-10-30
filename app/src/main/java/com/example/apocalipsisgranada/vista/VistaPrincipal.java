package com.example.apocalipsisgranada.vista;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class VistaPrincipal extends AppCompatActivity {

    private SharedPreferences preferencias;
    private RecyclerView recyclerMensajes;
    private List<Mensaje> alertas = new ArrayList<>();
    private List<Mensaje> guias = new ArrayList<>();
    private List<Mensaje> mostrados = new ArrayList<>();

    private static final int PERMISO_NOTIFICACION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);
        recyclerMensajes = findViewById(R.id.recyclerPrincipal);
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));

        configurarPrimerArranque();
        comprobarPermisoNotificaciones();

        // Configuración común
        Controlador.configurarModoDesarrolladorComun(this);
        ManejadorVistas.configurarElementosComunes(this);

        // Carga inicial de archivos
        cargarArchivo("alertas.json", "alerta", alertas);
        cargarArchivo("guias.json", "guia", guias);

        // Mostrar los mensajes del día actual
        mostrarMensajesIniciales();
    }

    // ============================================================
    // CONFIGURACIÓN INICIAL
    // ============================================================
    private void configurarPrimerArranque() {
        boolean primeraVez = preferencias.getBoolean("primer_arranque", true);
        if (primeraVez) {
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putBoolean("primer_arranque", false);
            editor.putLong("fechaInicio", System.currentTimeMillis());
            editor.putInt("diaActual", 1);
            editor.putInt("indiceMensajeDia", 0);
            editor.apply();
        }
    }

    private void comprobarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISO_NOTIFICACION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_NOTIFICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ============================================================
    // MOSTRAR MENSAJES
    // ============================================================
    public void mostrarMensajesIniciales() {
        mostrados.clear();

        int diaActual = preferencias.getInt("diaActual", 1);
        int indice = preferencias.getInt("indiceMensajeDia", 0);

        // Mensaje inicial fijo (día 0)
        mostrados.add(new Mensaje(0,
                Controlador.obtenerFechaSimulada(preferencias, 0),
                "Sistema de Alertas del Gobierno de España — Modo activo",
                "false",
                "alerta"));

        // Cargar todos los días anteriores
        for (int d = 1; d < diaActual; d++) {
            List<List<Mensaje>> paresAnteriores = obtenerAmbosMensajesDelDia(d);
            for (int i = 0; i < paresAnteriores.size(); i++) {
                List<Mensaje> par = paresAnteriores.get(i);
                mostrados.addAll(par);
            }

        }

        // Cargar mensajes del día actual (según índice)
        List<List<Mensaje>> paresHoy = obtenerAmbosMensajesDelDia(diaActual);
        for (int i = 0; i <= indice && i < paresHoy.size(); i++) {
            mostrados.addAll(paresHoy.get(i));
        }

        // Ordenar de más nuevo a más antiguo
        Collections.reverse(mostrados);

        recyclerMensajes.setAdapter(new AdaptadorMensajes(mostrados, this));
        ManejadorVistas.actualizarCabecera(this, Controlador.obtenerFechaSimulada(preferencias, diaActual));
    }

    // ============================================================
    // OBTENER PARES ALERTA + GUÍA DE UN DÍA
    // ============================================================
    private List<List<Mensaje>> obtenerAmbosMensajesDelDia(int dia) {
        List<Mensaje> alertasDia = new ArrayList<>();
        List<Mensaje> guiasDia = new ArrayList<>();
        List<List<Mensaje>> pares = new ArrayList<>();

        // Buscar alertas del día
        for (int i = 0; i < alertas.size(); i++) {
            Mensaje a = alertas.get(i);
            if (a.getDia() == dia) alertasDia.add(a);
        }

        // Buscar guías del día
        for (int i = 0; i < guias.size(); i++) {
            Mensaje g = guias.get(i);
            if (g.getDia() == dia) guiasDia.add(g);
        }

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
    // CARGAR ARCHIVOS JSON
    // ============================================================
    private void cargarArchivo(String archivo, String tipo, List<Mensaje> destino) {
        try {
            InputStream is = getAssets().open(archivo);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            destino.clear();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int dia = obj.getInt("dia");
                String texto = obj.getString("mensaje");
                String sonido = obj.optString("sonido", "false");
                String fecha = Controlador.obtenerFechaSimulada(preferencias, dia);
                destino.add(new Mensaje(dia, fecha, texto, sonido, tipo));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

