package com.example.apocalipsisgranada.modelo;

public class Usuario {
    private String nombre;

    public Usuario(String n) {
        nombre = n;
    }

    public String obtenerNombre() {
        return nombre;
    }
}