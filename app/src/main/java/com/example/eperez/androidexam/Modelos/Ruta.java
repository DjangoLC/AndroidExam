package com.example.eperez.androidexam.Modelos;

public class Ruta {

    private String hora;
    private String distance;
    private double lat;
    private double lgn;


    public Ruta(){

    }

    public Ruta(String hora, String distance, double lat, double lgn) {
        this.hora = hora;
        this.distance = distance;
        this.lat = lat;
        this.lgn = lgn;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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
