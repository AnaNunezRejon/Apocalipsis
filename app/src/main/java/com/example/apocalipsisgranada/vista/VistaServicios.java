package com.example.apocalipsisgranada.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apocalipsisgranada.R;

public class VistaServicios extends BaseActivity {

    private TextView textoModo;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);


        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);

        // 🟡 Cargar metodos comunes
        configurarModoDesarrolladorComun();
        configurarMenuInferior();
        actualizarCabecera();
        mostrarSaludoUsuario();
        actualizarColoresModoDesarrollador();
        mostrarTextoModoDesarrollador();

        // 🏛 Configurar los bloques de servicios
        configurarServicio(R.id.itemPoliciaMunicipal, "Policía Municipal", "958111111", "https://granada.es/policia");
        configurarServicio(R.id.itemGuardiaCivil, "Guardia Civil", "958222222", "https://www.guardiacivil.es/");
        configurarServicio(R.id.itemPoliciaNacional, "Policía Nacional", "958333333", "https://www.policia.es/");
        configurarServicio(R.id.itemEjercito, "Ejército Español", "910000000", "https://ejercito.defensa.gob.es/");
        configurarServicio(R.id.itemRegistro, "Registro Electrónico General", null, "https://sede.administracion.gob.es/");
    }

    // ============================================================
    // 🏛 CONFIGURAR CADA SERVICIO
    // ============================================================
    private void configurarServicio(int idVista, String nombre, String telefono, String url) {
        LinearLayout servicio = findViewById(idVista);
        if (servicio == null) {
            Toast.makeText(this, "Error: vista no encontrada para " + nombre, Toast.LENGTH_SHORT).show();
            return;
        }

        TextView textoServicio = servicio.findViewById(R.id.textoServicio);
        textoServicio.setText(nombre + (telefono != null ? " - " + telefono : ""));
        servicio.setOnClickListener(v -> mostrarOpcionesServicio(nombre, telefono, url));
    }

    // ============================================================
    // 📞 MOSTRAR OPCIONES DE CONTACTO
    // ============================================================
    private void mostrarOpcionesServicio(String titulo, String telefono, String url) {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle(titulo);

        if (telefono != null) {
            dialogo.setItems(new CharSequence[]{
                            "📞 Llamar a " + titulo,
                            "🌐 Abrir web oficial",
                            "❌ Cancelar"},
                    (dialog, opcion) -> {
                        switch (opcion) {
                            case 0:
                                realizarLlamada(telefono);
                                break;
                            case 1:
                                abrirPaginaWeb(url);
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }
                    });
        } else {
            dialogo.setItems(new CharSequence[]{
                            "🌐 Abrir web oficial",
                            "❌ Cancelar"},
                    (dialog, opcion) -> {
                        if (opcion == 0) abrirPaginaWeb(url);
                        else dialog.dismiss();
                    });
        }

        dialogo.show();
    }

    // ============================================================
    // ☎️ LLAMAR A UN NÚMERO
    // ============================================================
    private void realizarLlamada(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    // ============================================================
    // 🌐 ABRIR UNA PÁGINA WEB
    // ============================================================
    private void abrirPaginaWeb(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
