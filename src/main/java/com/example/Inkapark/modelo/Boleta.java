package com.example.Inkapark.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "boleta")
public class Boleta {

    @Id
    @Column(name = "id_boleta", length = 12)
    private String idBoleta;                         // ← PK alfanumérica

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // Relación por fecha hacia manejo_aforo(fecha_evento)
    @ManyToOne(optional = false)
    @JoinColumn(name = "fecha_evento", referencedColumnName = "fecha_evento", nullable = false)
    private ManejoAforo aforo;

    @Column(nullable = false)
    private Integer cantidad = 1;                    // ← NUEVO

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;                       // total

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('VIGENTE','USADA','CANCELADA')")
    private Estado estado = Estado.VIGENTE;

    public enum Estado {
        VIGENTE, USADA, CANCELADA
    }

    // getters & setters
    public String getIdBoleta() {
        return idBoleta;
    }

    public void setIdBoleta(String idBoleta) {
        this.idBoleta = idBoleta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ManejoAforo getAforo() {
        return aforo;
    }

    public void setAforo(ManejoAforo aforo) {
        this.aforo = aforo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
