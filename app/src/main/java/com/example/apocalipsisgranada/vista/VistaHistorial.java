package com.example.apocalipsisgranada.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apocalipsisgranada.R;

public class VistaHistorial extends AppCompatActivity {

    private LinearLayout botonInicio, botonServicios, botonHistorial, botonGuia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        botonInicio = findViewById(R.id.botonInicio);
        botonServicios = findViewById(R.id.botonServicios);
        botonHistorial = findViewById(R.id.botonHistorial);
        botonGuia = findViewById(R.id.botonGuia);

        botonInicio.setOnClickListener(v -> startActivity(new Intent(this, PrincipalActivity.class)));
        botonServicios.setOnClickListener(v -> startActivity(new Intent(this, VistaServicios.class)));
        botonGuia.setOnClickListener(v -> startActivity(new Intent(this, VistaGuia.class)));
    }
}
