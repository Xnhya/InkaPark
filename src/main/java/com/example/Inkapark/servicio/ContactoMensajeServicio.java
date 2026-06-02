package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.ContactoMensaje;
import com.example.Inkapark.modelo.ContactoMensaje.Estado;
import com.example.Inkapark.repositorio.ContactoMensajeRepositorio;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional // transaccional por defecto en todos los métodos
public class ContactoMensajeServicio {

    private final ContactoMensajeRepositorio repo;
    private final EmailServicio emailServicio;

    // Validación simple de email (suficiente para server-side)
    private static final Pattern EMAIL_REGEX
            = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Límites de seguridad (coincidir con columnas)
    private static final int MAX_NOMBRE = 120;
    private static final int MAX_CORREO = 150;
    private static final int MAX_ASUNTO = 200;

    public ContactoMensajeServicio(ContactoMensajeRepositorio repo, EmailServicio emailServicio) {
        this.repo = repo;
        this.emailServicio = emailServicio;
    }

    // 1) Guardar desde el formulario público
    public ContactoMensaje guardarDesdeFormulario(String nombre, String correo, String asunto, String mensaje) {
        nombre = safeTrim(nombre);
        correo = safeTrimLower(correo);
        asunto = safeTrim(asunto);
        mensaje = safeTrim(mensaje);

        if (isBlank(nombre)) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (isBlank(correo) || !EMAIL_REGEX.matcher(correo).matches()) {
            throw new IllegalArgumentException("Correo inválido.");
        }
        if (isBlank(asunto)) {
            throw new IllegalArgumentException("El asunto es obligatorio.");
        }
        if (isBlank(mensaje)) {
            throw new IllegalArgumentException("El mensaje es obligatorio.");
        }

        // aplica límites
        nombre = truncate(nombre, MAX_NOMBRE);
        correo = truncate(correo, MAX_CORREO);
        asunto = truncate(asunto, MAX_ASUNTO);

        ContactoMensaje m = new ContactoMensaje();
        m.setNombre(nombre);
        m.setCorreo(correo);
        m.setAsunto(asunto);
        m.setMensaje(mensaje);
        m.setFechaEnvio(LocalDateTime.now());
        m.setEstado(Estado.NUEVO);
        return repo.save(m);
    }

    // 2) Listado (con filtro opcional)
    @Transactional(readOnly = true)
    public List<ContactoMensaje> listar(String estado) {
        if (isBlank(estado)) {
            return repo.findAllByOrderByFechaEnvioDesc();
        }
        try {
            Estado e = Estado.valueOf(estado.trim().toUpperCase());
            return repo.findByEstadoOrderByFechaEnvioDesc(e);
        } catch (Exception ex) {
            return repo.findAllByOrderByFechaEnvioDesc();
        }
    }

    // 3) Marcar atendido + enviar acuse al remitente (respuesta es opcional)
    //    No hacemos rollback si falla el email: el estado queda ATENDIDO igual.
    @Transactional(noRollbackFor = MailException.class)
    public boolean marcarAtendidoYNotificar(Integer id, String respuestaOpcional) {
        Optional<ContactoMensaje> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return false;
        }

        ContactoMensaje m = opt.get();

        // Si ya estaba atendido, no reenviamos acuse (idempotencia)
        boolean yaAtendido = (m.getEstado() == Estado.ATENDIDO);

        m.setEstado(Estado.ATENDIDO);
        m.setFechaRespuesta(LocalDateTime.now());
        if (!isBlank(respuestaOpcional)) {
            m.setRespuesta(respuestaOpcional.trim());
        }
        repo.save(m);

        if (!yaAtendido && !isBlank(m.getCorreo())) {
            try {
                emailServicio.enviarAcuseAtencion(m.getCorreo(), m.getNombre(), m.getAsunto());
            } catch (MailException ex) {
                // Loguea si tienes logger; no rompas la transacción
                // logger.warn("Fallo al enviar acuse: {}", ex.getMessage());
            }
        }
        return true;
    }

    // 4) Eliminar
    public boolean eliminar(Integer id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);
        return true;
    }

    // --------- helpers ---------
    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private static String safeTrimLower(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Transactional
    public boolean responderYNotificar(Integer id, String respuesta) {
        var opt = repo.findById(id);
        if (opt.isEmpty()) {
            return false;
        }

        var m = opt.get();
        m.setRespuesta(respuesta == null ? null : respuesta.trim());
        m.setEstado(Estado.ATENDIDO);
        m.setFechaRespuesta(LocalDateTime.now());
        repo.save(m);

        if (m.getCorreo() != null && !m.getCorreo().isBlank() && m.getRespuesta() != null) {
            emailServicio.enviarRespuestaContacto(m.getCorreo(), m.getNombre(), m.getAsunto(), m.getRespuesta());
        }
        return true;
    }

}
