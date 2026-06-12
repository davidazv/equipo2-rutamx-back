package org.acme.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AfluenciaMetrobus {

    private LocalDate fecha;
    private String mes;
    private short anio;
    private String linea;
    private String tipoPago;
    private BigDecimal afluencia;

    public AfluenciaMetrobus() {
        // intentionally empty
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public short getAnio() {
        return anio;
    }

    public void setAnio(short anio) {
        this.anio = anio;
    }

    public String getLinea() {
        return linea;
    }

    public void setLinea(String linea) {
        this.linea = linea;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public BigDecimal getAfluencia() {
        return afluencia;
    }

    public void setAfluencia(BigDecimal afluencia) {
        this.afluencia = afluencia;
    }
}
