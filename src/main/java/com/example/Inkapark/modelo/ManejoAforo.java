package com.example.Inkapark.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "manejo_aforo")
public class ManejoAforo {

    @Id
    @Column(name = "fecha_evento")
    private LocalDate fechaEvento;          // PK

    @Column(name = "aforo_total", nullable = false)
    private Integer aforoTotal;

    @Column(name = "aforo_disponible", nullable = false)
    private Integer aforoDisponible;

    // getters & setters
    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public Integer getAforoTotal() {
        return aforoTotal;
    }

    public void setAforoTotal(Integer aforoTotal) {
        this.aforoTotal = aforoTotal;
    }

    public Integer getAforoDisponible() {
        return aforoDisponible;
    }

    public void setAforoDisponible(Integer aforoDisponible) {
        this.aforoDisponible = aforoDisponible;
    }
}
