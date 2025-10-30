package com.example.apocalipsisgranada.vista;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Preferencias;

/**
 * Resumen rápido del flujo — LoginActivity
 *
 * Esta pantalla sirve para iniciar sesión en la app.
 * El usuario escribe su nombre y una contraseña, y si son válidos,
 * se guarda el usuario en las preferencias y se abre la pantalla principal.
 *
 * Si el usuario ya se ha identificado antes, no se le vuelve a pedir login:
 * la app lo detecta automáticamente y lo lleva directo a VistaPrincipal.
 */
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

        // Si ya hay usuario guardado, saltar directo al menú principal
        if (Preferencias.hayUsuario(this)) {
            startActivity(new Intent(this, VistaPrincipal.class));
            finish();
            return;
        }

        // Si no hay usuario guardado, pedir login normal
        botonAcceder.setOnClickListener(v -> {
            String nombre = campoNombre.getText().toString().trim();
            String contrasena = campoContrasena.getText().toString().trim();

            if (validarNombre(nombre) && validarContrasena(contrasena)) {

                // 1️⃣ Reiniciar simulación desde cero
                Preferencias.reiniciarSimulacion(this, false);

                // 2️⃣ Guardar el nombre de usuario después del reinicio
                Preferencias.guardarNombreUsuario(this, nombre);

                // 3️⃣ Marcar que es el primer arranque
                prefs.edit()
                        .putBoolean("primer_arranque", true)
                        .apply();

                // 4️⃣ Ir a la pantalla principal
                startActivity(new Intent(LoginActivity.this, VistaPrincipal.class));
                finish();

            } else {
                Toast.makeText(LoginActivity.this, "Datos no válidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================================================
    // Validadores
    // ============================================================

    private boolean validarNombre(String texto) {
        return texto != null && texto.length() > 0 &&
                texto.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+");
    }

    private boolean validarContrasena(String texto) {
        return texto != null && texto.length() >= 4 &&
                !texto.matches(".*[:;?.,!@#].*");
    }
}

/**
 * 🔐 Resumen rápido del flujo — LoginActivity
 *
 * Esta pantalla sirve para iniciar sesión en la app.
 * El usuario escribe su nombre y una contraseña, y si son válidos,
 * se guarda el usuario en las preferencias y se abre la pantalla principal.
 *
 * Si el usuario ya se ha identificado antes, no se le vuelve a pedir login:
 * la app lo detecta automáticamente y lo lleva directo a VistaPrincipal.
 *
 * 🧭 Flujo general:
 * Al abrir la app → entra en onCreate()
 * Se cargan los campos de texto (nombre y contraseña) y el botón "Acceder"
 * Se abren las preferencias "configuracion"
 * Se comprueba si ya hay usuario guardado:
 *   ├─ Si sí hay → salta directamente a VistaPrincipal (sin mostrar login)
 *   └─ Si no hay → muestra el formulario de acceso
 * Cuando el usuario pulsa "Acceder":
 *   ├─ Se lee el nombre y la contraseña introducidos
 *   ├─ Se validan (sin símbolos raros ni números)
 *   ├─ Si son válidos:
 *   │     ├─ Se reinicia la simulación (día 1, fecha inicio actual)
 *   │     ├─ Se guarda el nombre de usuario en preferencias
 *   │     ├─ Se marca que es el "primer_arranque"
 *   │     └─ Se abre la pantalla VistaPrincipal
 *   └─ Si no son válidos → se muestra un Toast con "Datos no válidos"
 *
 * 🟩 onCreate()
 *  ├─ setContentView(R.layout.activity_login)
 *  ├─ campoNombre = findViewById(R.id.campoNombre)
 *  ├─ campoContrasena = findViewById(R.id.campoContrasena)
 *  ├─ botonAcceder = findViewById(R.id.botonAcceder)
 *  ├─ prefs = getSharedPreferences("configuracion", MODE_PRIVATE)
 *  ├─ if (Preferencias.hayUsuario(this)) → ir directo a VistaPrincipal
 *  ├─ if (nombreGuardado != null && !nombreGuardado.isEmpty()) → también ir directo
 *  └─ botonAcceder.setOnClickListener(...)
 *        ├─ validarNombre(nombre)
 *        ├─ validarContrasena(contrasena)
 *        ├─ Si todo bien:
 *        │     ├─ Preferencias.reiniciarSimulacion(this, false)
 *        │     ├─ Preferencias.guardarNombreUsuario(this, nombre)
 *        │     ├─ prefs.edit().putBoolean("primer_arranque", true).apply()
 *        │     └─ startActivity(new Intent(this, VistaPrincipal.class))
 *        └─ Si falla → Toast "Datos no válidos"
 *
 * 🟨 validarNombre(String texto)
 *  ├─ Acepta solo letras, espacios y acentos
 *  └─ Devuelve true si el formato es correcto
 *
 * 🟦 validarContrasena(String texto)
 *  ├─ Debe tener al menos 4 caracteres
 *  └─ No puede contener símbolos como : ; ? . , ! @ #
 *
 * 🔁 Relación entre métodos:
 * onCreate() → validarNombre() / validarContrasena()
 * onCreate() → Preferencias.reiniciarSimulacion()
 * onCreate() → Preferencias.guardarNombreUsuario()
 * onCreate() → VistaPrincipal (si login correcto)
 */

