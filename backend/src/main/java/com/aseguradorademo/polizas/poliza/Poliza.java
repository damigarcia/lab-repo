package com.aseguradorademo.polizas.poliza;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aseguradorademo.polizas.cliente.Cliente;

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
@Table(name = "polizas")
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoPoliza estado;

    @Column(name = "suma_asegurada", nullable = false, precision = 14, scale = 2)
    private BigDecimal sumaAsegurada;

    @Column(name = "prima_anual", nullable = false, precision = 12, scale = 2)
    private BigDecimal primaAnual;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    protected Poliza() {
        // requerido por JPA
    }

    public Poliza(String numero, Cliente cliente, TipoPoliza tipo, BigDecimal sumaAsegurada,
                  BigDecimal primaAnual, LocalDate fechaInicio, LocalDate fechaFin) {
        this.numero = numero;
        this.cliente = cliente;
        this.tipo = tipo;
        this.estado = EstadoPoliza.VIGENTE;
        this.sumaAsegurada = sumaAsegurada;
        this.primaAnual = primaAnual;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public void cancelar(String motivo) {
        this.estado = EstadoPoliza.CANCELADA;
        this.motivoCancelacion = motivo;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public EstadoPoliza getEstado() {
        return estado;
    }

    public BigDecimal getSumaAsegurada() {
        return sumaAsegurada;
    }

    public BigDecimal getPrimaAnual() {
        return primaAnual;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    /** Uso exclusivo de tests. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Uso exclusivo de tests. */
    void setEstado(EstadoPoliza estado) {
        this.estado = estado;
    }
}
