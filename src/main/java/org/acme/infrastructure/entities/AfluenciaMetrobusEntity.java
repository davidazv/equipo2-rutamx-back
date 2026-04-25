package org.acme.infrastructure.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "afluencia_metrobus")
public class AfluenciaMetrobusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "mes", nullable = false, length = 20)
    private String mes;

    @Column(name = "anio", nullable = false)
    private Short anio;

    @Column(name = "linea", nullable = false, length = 30)
    private String linea;

    @Column(name = "tipo_pago", nullable = false, length = 30)
    private String tipoPago;

    @Column(name = "afluencia", nullable = false, precision = 12, scale = 2)
    private BigDecimal afluencia;

    public AfluenciaMetrobusEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public Short getAnio() { return anio; }
    public void setAnio(Short anio) { this.anio = anio; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public BigDecimal getAfluencia() { return afluencia; }
    public void setAfluencia(BigDecimal afluencia) { this.afluencia = afluencia; }
}
