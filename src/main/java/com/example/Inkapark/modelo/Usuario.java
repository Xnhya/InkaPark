package com.example.Inkapark.modelo;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "usuario",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo")
    },
    indexes = {
        @Index(name = "idx_usuario_correo", columnList = "correo")
    }
)
public class Usuario {

    public enum Rol { CLIENTE, ADMIN }

    // --- CAMPOS ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "Formato de correo inválido")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
        message = "Solo se permiten correos @gmail.com"
    )
    @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(max = 255, message = "La contraseña no debe exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('CLIENTE','ADMIN')")
    private Rol rol;

    // --- VERIFICACIÓN DE CUENTA ---
    @Column(nullable = false)
    private boolean verificado = false;

    @Size(max = 64, message = "El token no debe exceder 64 caracteres")
    @Column(name = "token_verificacion", length = 64)
    private String tokenVerificacion;

    @Column(name = "token_expira")
    private LocalDateTime tokenExpira;

    // --- GANCHOS DE CICLO DE VIDA (normaliza correo) ---
    @PrePersist
    @PreUpdate
    private void normalizarCorreo() {
        if (correo != null) {
            correo = correo.trim().toLowerCase();
        }
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getTokenVerificacion() { return tokenVerificacion; }
    public void setTokenVerificacion(String tokenVerificacion) { this.tokenVerificacion = tokenVerificacion; }

    public LocalDateTime getTokenExpira() { return tokenExpira; }
    public void setTokenExpira(LocalDateTime tokenExpira) { this.tokenExpira = tokenExpira; }
}
