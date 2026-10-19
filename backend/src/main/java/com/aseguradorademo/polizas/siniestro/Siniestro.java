package com.aseguradorademo.polizas.siniestro;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aseguradorademo.polizas.poliza.Poliza;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "siniestros")
public class Siniestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(name = "fecha_ocurrencia", nullable = false)
    private LocalDate fechaOcurrencia;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDate fechaReporte;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "monto_reclamado", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoReclamado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoSiniestro estado;

    protected Siniestro() {
        // requerido por JPA
    }

    public Siniestro(Poliza poliza, LocalDate fechaOcurrencia, LocalDate fechaReporte,
                     String descripcion, BigDecimal montoReclamado) {
        this.poliza = poliza;
        this.fechaOcurrencia = fechaOcurrencia;
        this.fechaReporte = fechaReporte;
        this.descripcion = descripcion;
        this.montoReclamado = montoReclamado;
        this.estado = EstadoSiniestro.REPORTADO;
    }

    public Long getId() {
        return id;
    }

    public Poliza getPoliza() {
        return poliza;
    }

    public LocalDate getFechaOcurrencia() {
        return fechaOcurrencia;
    }

    public LocalDate getFechaReporte() {
        return fechaReporte;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getMontoReclamado() {
        return montoReclamado;
    }

    public EstadoSiniestro getEstado() {
        return estado;
    }
}
