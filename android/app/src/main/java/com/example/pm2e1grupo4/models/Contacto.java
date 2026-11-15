package com.example.pm2e1grupo4.models;

import java.io.Serializable;

public class Contacto implements Serializable {
    private int id;
    private String nombre;
    private String telefono;
    private double latitud;
    private double longitud;
    private String urlArchivo;
    private String mimeType;
    private long archivoTamano;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getUrlArchivo() { return urlArchivo; }
}