package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder encoder;

    public AuthServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder encoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.encoder = encoder;
    }

    /** Login general (cliente o admin). Bloquea si no está verificado. Soporta legacy texto plano. */
    public Optional<Usuario> login(String correo, String contrasena) {
        correo = normalizar(correo);
        return usuarioRepositorio.findByCorreo(correo)
                .filter(Usuario::isVerificado) // ← IMPIDE login si no verificó
                .filter(u -> passwordMatches(contrasena, u.getContrasena()));
    }

    /** Login admin (rol ADMIN). */
    public Optional<Usuario> loginAdmin(String correo, String contrasena) {
        correo = normalizar(correo);
        return login(correo, contrasena)
                .filter(u -> u.getRol() == Usuario.Rol.ADMIN);
    }

    /** BCrypt con fallback a texto plano para cuentas viejas. */
    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) return false;
        boolean looksHashed = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
        if (looksHashed) {
            return encoder.matches(raw, stored);
        }
        return stored.equals(raw); // legacy
    }

    private String normalizar(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }
}
