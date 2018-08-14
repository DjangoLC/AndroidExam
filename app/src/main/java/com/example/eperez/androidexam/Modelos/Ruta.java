package com.example.eperez.androidexam.Modelos;

public class Ruta {

    private double hora;
    private String tipo;


    public Ruta(){

    }

    public Ruta(double hora, String tipo) {
        this.hora = hora;
        this.tipo = tipo;
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
}
