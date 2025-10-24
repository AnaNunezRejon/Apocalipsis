package com.example.apocalipsisgranada.vista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.widget.Toast;

import com.example.apocalipsisgranada.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PrincipalActivity extends AppCompatActivity {

    private TextView textoSaludo, textoFecha;
    private LinearLayout contenedorAlertas;
    private LinearLayout botonInicio, botonServicios, botonGuia, botonHistorial; // 👈 ahora son LinearLayout
    private ArrayList<String> listaAlertas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Referencias a los elementos del XML
        textoSaludo = findViewById(R.id.textoSaludo);
        textoFecha = findViewById(R.id.textoFecha);
        contenedorAlertas = findViewById(R.id.contenedorAlertas);

        botonInicio = findViewById(R.id.botonInicio);
        botonServicios = findViewById(R.id.botonServicios);
        botonGuia = findViewById(R.id.botonGuia);
        botonHistorial = findViewById(R.id.botonHistorial);

        // Obtener el nombre del usuario del login
        String nombreUsuario = getIntent().getStringExtra("nombreUsuario");
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            nombreUsuario = "Ciudadano";
        }

        // Mostrar saludo
        textoSaludo.setText("Hola, " + nombreUsuario);

        // Mostrar la fecha actual
        Date fechaActual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String fechaTexto = formato.format(fechaActual);
        textoFecha.setText("Hoy es " + fechaTexto);

        // Crear alertas de ejemplo
        listaAlertas.add("⚠️ Se han reportado fallos eléctricos en varios distritos de Granada.");
        listaAlertas.add("🚨 Las autoridades recomiendan evitar usar aparatos eléctricos por el momento.");

        mostrarAlertas();

        // Funcionalidad de los botones del menú inferior
        botonInicio.setOnClickListener(v -> {
            Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show();
        });

        botonServicios.setOnClickListener(v -> {
            Intent intent = new Intent(this, VistaServicios.class);
            startActivity(intent);
        });

        botonGuia.setOnClickListener(v -> {
            Intent intent = new Intent(this, VistaGuia.class);
            startActivity(intent);
        });

        botonHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(this, VistaHistorial.class);
            startActivity(intent);
        });
    }

    /**
     * Método para mostrar las alertas en la pantalla
     */
    private void mostrarAlertas() {
        contenedorAlertas.removeAllViews();

        for (int i = 0; i < listaAlertas.size(); i++) {
            TextView alerta = new TextView(this);
            alerta.setText(listaAlertas.get(i));
            alerta.setTextColor(Color.parseColor("#001B70")); // azulGobierno
            alerta.setBackgroundColor(Color.parseColor("#FFFFFF"));
            alerta.setPadding(20, 20, 20, 20);
            alerta.setTextSize(15f);

            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            parametros.setMargins(0, 0, 0, 20);
            alerta.setLayoutParams(parametros);
            alerta.setGravity(Gravity.START);

            contenedorAlertas.addView(alerta);
        }
    }
}
