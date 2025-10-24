package com.example.apocalipsisgranada.controlador;

public class ControladorLogin {

    public boolean validarNombre(String texto) {
        // Solo letras y espacios (incluye acentos y ñ)
        return texto != null && texto.length() > 0 &&
                texto.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+");
    }

    public boolean validarContrasena(String texto) {
        // No permitir : ; ? . , ! @ #
        return texto != null && texto.length() >= 4 &&
                !texto.matches(".*[:;?.,!@#].*");
    }
}