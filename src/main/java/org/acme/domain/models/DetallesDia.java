package org.acme.domain.models;

public class DetallesDia {

    private Integer viajes;
    private Double pasajeros;

    public DetallesDia() {}

    public DetallesDia(Integer viajes, Double pasajeros) {
        this.viajes = viajes;
        this.pasajeros = pasajeros;
    }

    public Integer getViajes() { return viajes; }
    public void setViajes(Integer viajes) { this.viajes = viajes; }

    public Double getPasajeros() { return pasajeros; }
    public void setPasajeros(Double pasajeros) { this.pasajeros = pasajeros; }
}
