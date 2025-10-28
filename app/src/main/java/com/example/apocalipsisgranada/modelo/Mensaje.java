package com.example.apocalipsisgranada.modelo;

import com.example.apocalipsisgranada.R;

public class Mensaje {

    // ====== CAMPOS ======
    private int dia;            // Día en el que se publica el mensaje
    private String hora;        // Hora opcional del mensaje
    private String texto;       // Contenido del mensaje
    private String sonido;      // "false", "notificacion", "alerta", "alarma"
    private String tipo;        // "alerta" o "guia"

    // ====== CONSTRUCTORES ======
    public Mensaje() {}

    public Mensaje(int dia, String hora, String texto, String sonido, String tipo) {
        this.dia = dia;
        this.hora = hora;
        this.texto = texto;
        this.sonido = sonido;
        this.tipo = tipo;
    }

    // ====== GETTERS ======
    public int getDia() { return dia; }
    public String getHora() { return hora; }
    public String getTexto() { return texto; }
    public String getSonido() { return sonido; }
    public String getTipo() { return tipo; }

    // ====== SETTERS ======
    public void setDia(int dia) { this.dia = dia; }
    public void setHora(String hora) { this.hora = hora; }
    public void setTexto(String texto) { this.texto = texto; }
    public void setSonido(String sonido) { this.sonido = sonido; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    // ====== MÉTODOS AUXILIARES ======
    /**
     * Devuelve el identificador del recurso de sonido según el tipo indicado en el JSON.
     */
    public int obtenerRecursoSonido() {
        switch (sonido) {
            case "alarm_short":
                return R.raw.alarm_short;
            case "alert":
                return R.raw.alert;
            case "gremlin_risa":
                return R.raw.gremlin_risa;
            case "alien_risa":
                return R.raw.alien_risa;
            case "sonido_raro":
                return R.raw.sonido_raro;
            case "siren_warning":
                return R.raw.siren_warning;
            default:
                return 0; // sin sonido
        }
    }

    /**
     * Indica si el mensaje debe generar una notificación del sistema. Solo las alertas lo hacen.
     */
    public boolean debeMostrarNotificacion() {
        return tipo.equalsIgnoreCase("alerta");
    }
}
