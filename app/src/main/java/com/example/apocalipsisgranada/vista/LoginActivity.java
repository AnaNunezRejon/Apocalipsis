package com.example.apocalipsisgranada.vista;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.ControladorLogin;
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

        botonAcceder.setOnClickListener(v -> {
            String nombre = campoNombre.getText().toString().trim();
            String contrasena = campoContrasena.getText().toString().trim();

            ControladorLogin controlador = new ControladorLogin();

            if (controlador.validarNombre(nombre) && controlador.validarContrasena(contrasena)) {
                Usuario usuario = new Usuario(nombre);
                Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
                intent.putExtra("nombreUsuario", usuario.obtenerNombre());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Datos no válidos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
