package com.example.Inkapark.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notif")
    private Integer idNotif;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;             // puede ser null

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "fecha")
    private LocalDateTime fecha = LocalDateTime.now();

    // getters & setters
    public Integer getIdNotif() {
        return idNotif;
    }

    public void setIdNotif(Integer idNotif) {
        this.idNotif = idNotif;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
