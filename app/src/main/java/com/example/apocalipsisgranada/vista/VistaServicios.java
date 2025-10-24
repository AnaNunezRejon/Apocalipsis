package com.example.apocalipsisgranada.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.apocalipsisgranada.R;

public class VistaServicios extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);

        configurarServicio(R.id.itemPoliciaMunicipal, "Policía Municipal", "958111111", "https://granada.es/policia");
        configurarServicio(R.id.itemGuardiaCivil, "Guardia Civil", "958222222", "https://www.guardiacivil.es/");
        configurarServicio(R.id.itemPoliciaNacional, "Policía Nacional", "958333333", "https://www.policia.es/");
        configurarServicio(R.id.itemEjercito, "Ejército Español", "910000000", "https://ejercito.defensa.gob.es/");
        configurarServicio(R.id.itemRegistro, "Registro Electrónico General", null, "https://sede.administracion.gob.es/");
    }

    private void configurarServicio(int idVista, String nombre, String telefono, String url) {
        LinearLayout servicio = findViewById(idVista);

        TextView texto = servicio.findViewById(R.id.textoServicio);
        texto.setText(nombre + (telefono != null ? " - " + telefono : ""));
        servicio.setOnClickListener(v -> mostrarOpciones(nombre, telefono, url));
    }

    private void mostrarOpciones(String titulo, String telefono, String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);

        if (telefono != null) {
            builder.setItems(new CharSequence[]{
                            "📞 Llamar a " + titulo,
                            "🌐 Abrir web oficial",
                            "❌ Cancelar"},
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                llamar(telefono);
                                break;
                            case 1:
                                abrirWeb(url);
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }
                    });
        } else {
            builder.setItems(new CharSequence[]{
                            "🌐 Abrir web oficial",
                            "❌ Cancelar"},
                    (dialog, which) -> {
                        if (which == 0) abrirWeb(url);
                        else dialog.dismiss();
                    });
        }

        builder.show();
    }

    private void llamar(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    private void abrirWeb(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
