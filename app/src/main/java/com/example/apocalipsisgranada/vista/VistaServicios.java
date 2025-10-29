package com.example.apocalipsisgranada.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Controlador;

public class VistaServicios extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);

        // 🟡 Cargar metodos comunes
        Controlador.configurarModoDesarrolladorComun(this);
        Controlador.configurarMenuInferior(this);
        Controlador.actualizarCabecera(this);
        Controlador.mostrarSaludoUsuario(this);
        Controlador.actualizarColoresModoDesarrollador(this);
        Controlador.mostrarTextoModoDesarrollador(this);

        // Configuramos los servicios
        configurarServicio(R.id.itemPoliciaMunicipal, "Policía Municipal", "958111111", "https://granada.es/policia");
        configurarServicio(R.id.itemGuardiaCivil, "Guardia Civil", "958222222", "https://www.guardiacivil.es/");
        configurarServicio(R.id.itemPoliciaNacional, "Policía Nacional", "958333333", "https://www.policia.es/");
        configurarServicio(R.id.itemEjercito, "Ejército Español", "910000000", "https://ejercito.defensa.gob.es/");
        configurarServicio(R.id.itemRegistro, "Registro Electrónico General", null, "https://sede.administracion.gob.es/");
    }

    // ============================================================
    // CONFIGURA UN SERVICIO (nombre, teléfono, web)
    // ============================================================
    private void configurarServicio(int idVista, String nombre, String telefono, String url) {
        LinearLayout servicio = findViewById(idVista);

        // Si no se encuentra la vista en el layout
        if (servicio == null) {
            Toast.makeText(this, "Error: vista no encontrada para " + nombre, Toast.LENGTH_SHORT).show();
            return;
        }

        // Escribimos el nombre (y el teléfono si existe)
        TextView textoServicio = servicio.findViewById(R.id.textoServicio);
        if (telefono != null) {
            textoServicio.setText(nombre + " - " + telefono);
        } else {
            textoServicio.setText(nombre);
        }

        // Cuando se pulse el bloque, mostramos las opciones (llamar / abrir web)
        servicio.setOnClickListener(v -> mostrarOpcionesServicio(nombre, telefono, url));
    }

    // ============================================================
    // MUESTRA UN MENÚ DE OPCIONES (llamar o abrir web)
    // ============================================================
    private void mostrarOpcionesServicio(String titulo, String telefono, String url) {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        //AlertDialog es una ventana emergente (un cuadro de diálogo) que aparece encima de la pantalla principal de tu app para mostrar un mensaje, pedir confirmación o mostrar opciones.
        dialogo.setTitle(titulo);

        // Si el servicio tiene teléfono → mostrar las dos opciones
        if (telefono != null) {
            dialogo.setItems(new CharSequence[]{
                    "📞 Llamar a " + titulo,
                    "🌐 Abrir web oficial",
                    "❌ Cancelar"
            }, (dialog, opcion) -> {
                if (opcion == 0) {
                    realizarLlamada(telefono);
                } else if (opcion == 1) {
                    abrirPaginaWeb(url);
                } else {
                    dialog.dismiss();
                }
            });
        }
        // Si NO tiene teléfono → solo mostrar opción de web
        else {
            dialogo.setItems(new CharSequence[]{
                    "🌐 Abrir web oficial",
                    "❌ Cancelar"
            }, (dialog, opcion) -> {
                if (opcion == 0) {
                    abrirPaginaWeb(url);
                } else {
                    dialog.dismiss();
                }
            });
        }

        dialogo.show();
    }

    // ============================================================
    // ABRIR LA APP DE TELÉFONO CON EL NÚMERO
    // ============================================================
    private void realizarLlamada(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    // ============================================================
    // ABRIR EL NAVEGADOR CON LA WEB DEL SERVICIO
    // ============================================================
    private void abrirPaginaWeb(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
