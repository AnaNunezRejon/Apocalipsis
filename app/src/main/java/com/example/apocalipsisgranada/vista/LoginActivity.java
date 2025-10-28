package com.example.apocalipsisgranada.vista;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.ControladorLogin;
import com.example.apocalipsisgranada.controlador.Preferencias;
import com.example.apocalipsisgranada.modelo.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText campoNombre;
    private EditText campoContrasena;
    private Button botonAcceder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoNombre = findViewById(R.id.campoNombre);
        campoContrasena = findViewById(R.id.campoContrasena);
        botonAcceder = findViewById(R.id.botonAcceder);

        SharedPreferences prefs = getSharedPreferences("configuracion", MODE_PRIVATE);
        if (Preferencias.hayUsuario(this)) {
            startActivity(new Intent(this, VistaPrincipal.class));
            finish();
            return;
        }
        // 🟢 Si ya hay usuario guardado, saltar directo al menú principal
        String nombreGuardado = prefs.getString("nombreUsuario", null);
        if (nombreGuardado != null && !nombreGuardado.isEmpty()) {
            Intent intent = new Intent(this, VistaPrincipal.class);
            startActivity(intent);
            finish();
            return;
        }

        // 🟢 Si no hay usuario guardado, pedir login normal
        botonAcceder.setOnClickListener(v -> {
            String nombre = campoNombre.getText().toString().trim();
            String contrasena = campoContrasena.getText().toString().trim();

            ControladorLogin controlador = new ControladorLogin();

            if (controlador.validarNombre(nombre) && controlador.validarContrasena(contrasena)) {

                // 🔁 1️⃣ Reiniciar simulación desde cero
                Preferencias.reiniciarSimulacion(this, false);

                // 💾 2️⃣ Guardar el nombre de usuario después del reinicio
                Preferencias.guardarNombreUsuario(this, nombre);

                // 🟢 3️⃣ Marcar que es el primer arranque
                getSharedPreferences("configuracion", MODE_PRIVATE)
                        .edit()
                        .putBoolean("primer_arranque", true)
                        .commit();

                // 🚀 4️⃣ Ir a la pantalla principal
                Intent intent = new Intent(LoginActivity.this, VistaPrincipal.class);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(LoginActivity.this, "Datos no válidos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
