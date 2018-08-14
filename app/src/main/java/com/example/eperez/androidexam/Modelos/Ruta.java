package com.example.eperez.androidexam.Modelos;

public class Ruta {

    private double hora;
    private String tipo;
    private double lat;
    private double lgn;


    public Ruta(){

    }

    public Ruta(double hora, String tipo, double lat, double lgn) {
        this.hora = hora;
        this.tipo = tipo;
        this.lat = lat;
        this.lgn = lgn;
    }

    public double getHora() {
        return hora;
    }

    public void setHora(double hora) {
        this.hora = hora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLgn() {
        return lgn;
    }

    public void setLgn(double lgn) {
        this.lgn = lgn;
    }
}
