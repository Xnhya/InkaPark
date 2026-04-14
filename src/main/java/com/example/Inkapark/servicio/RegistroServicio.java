package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class RegistroServicio {

    private final UsuarioRepositorio usuarioRepo;
    private final EmailServicio emailServicio;    
    private final PasswordEncoder encoder;      

    private static final Pattern GMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");

    // Lee la URL base desde properties; por defecto localhost
    @Value("${app.base-url:http://localhost:8080}")
    private String urlBase;

    public RegistroServicio(UsuarioRepositorio usuarioRepo,
                            EmailServicio emailServicio,
                            ObjectProvider<PasswordEncoder> encoderProvider) {
        this.usuarioRepo = usuarioRepo;
        this.emailServicio = emailServicio;
        this.encoder = encoderProvider.getIfAvailable(); // null si no hay bean
    }

    // ---------- REGISTRO ----------
    @Transactional
    public void registrarCliente(String nombre, String correo, String contrasena) {
        correo = normalizarCorreo(correo);
        validarSoloGmail(correo);

        Optional<Usuario> existenteOpt = usuarioRepo.findByCorreo(correo);
        if (existenteOpt.isPresent()) {
            Usuario existente = existenteOpt.get();
            if (existente.isVerificado()) {
                throw new IllegalStateException("El correo ya está registrado y verificado.");
            }
            // existe pero no verificado → regenerar token y reenviar
            String nuevoToken = UUID.randomUUID().toString();
            existente.setTokenVerificacion(nuevoToken);
            existente.setTokenExpira(LocalDateTime.now().plusHours(24));
            usuarioRepo.save(existente);

            String link = construirLinkVerificacion(correo, nuevoToken);
            emailServicio.enviarVerificacion(correo, link);
            return;
        }

        // crear nuevo usuario pendiente de verificación
        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        nuevo.setContrasena(encriptarContrasena(contrasena));
        nuevo.setRol(Usuario.Rol.CLIENTE);
        nuevo.setVerificado(false);
        nuevo.setTokenVerificacion(UUID.randomUUID().toString());
        nuevo.setTokenExpira(LocalDateTime.now().plusHours(24));
        usuarioRepo.save(nuevo);

        String link = construirLinkVerificacion(correo, nuevo.getTokenVerificacion());
        emailServicio.enviarVerificacion(correo, link);
    }

    // ---------- VERIFICAR ----------
    @Transactional
    public void verificarCuenta(String correo, String token) {
        correo = normalizarCorreo(correo);
        Usuario u = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Enlace inválido: usuario no encontrado."));

        if (u.isVerificado()) return;

        if (u.getTokenVerificacion() == null || !u.getTokenVerificacion().equals(token)) {
            throw new IllegalArgumentException("Token de verificación inválido.");
        }
        if (u.getTokenExpira() == null || LocalDateTime.now().isAfter(u.getTokenExpira())) {
            throw new IllegalStateException("El enlace de verificación ha expirado.");
        }

        u.setVerificado(true);
        u.setTokenVerificacion(null);
        u.setTokenExpira(null);
        usuarioRepo.save(u);
    }

    // ---------- REENVIAR ----------
    @Transactional
    public void reenviarVerificacion(String correo) {
        correo = normalizarCorreo(correo);
        Usuario u = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese correo."));

        if (u.isVerificado()) {
            throw new IllegalStateException("La cuenta ya está verificada.");
        }

        String nuevoToken = UUID.randomUUID().toString();
        u.setTokenVerificacion(nuevoToken);
        u.setTokenExpira(LocalDateTime.now().plusHours(24));
        usuarioRepo.save(u);

        String link = construirLinkVerificacion(correo, nuevoToken);
        emailServicio.enviarVerificacion(correo, link);
    }

    // ---------- HELPERS ----------
    private String construirLinkVerificacion(String correo, String token) {
        // IMPORTANTE: ruta del controlador es /auth/verificar
        return urlBase + "/auth/verificar?email=" +
                URLEncoder.encode(correo, StandardCharsets.UTF_8) +
                "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }

    private void validarSoloGmail(String correo) {
        if (correo == null || !GMAIL_REGEX.matcher(correo).matches()) {
            throw new IllegalArgumentException("Solo se permiten correos @gmail.com válidos.");
        }
    }

    private String encriptarContrasena(String contrasena) {
        return (encoder != null) ? encoder.encode(contrasena) : contrasena; // temporal si no hay encoder
    }
}
