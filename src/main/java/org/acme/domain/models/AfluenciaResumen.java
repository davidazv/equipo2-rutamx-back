package org.acme.domain.models;

public class AfluenciaResumen {

    private String linea;
    private int dayOfWeek;
    private double totalAfluencia;

    public AfluenciaResumen() {}

    public AfluenciaResumen(String linea, int dayOfWeek, double totalAfluencia) {
        this.linea = linea;
        this.dayOfWeek = dayOfWeek;
        this.totalAfluencia = totalAfluencia;
    }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public double getTotalAfluencia() { return totalAfluencia; }
    public void setTotalAfluencia(double totalAfluencia) { this.totalAfluencia = totalAfluencia; }
}
