package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional // asegura commit/rollback en cada método público
public class UsuarioServicio {

    private final UsuarioRepositorio repo;
    private final PasswordEncoder encoder; // puede ser null

    public UsuarioServicio(UsuarioRepositorio repo, ObjectProvider<PasswordEncoder> encoderProvider) {
        this.repo = repo;
        this.encoder = encoderProvider.getIfAvailable(); // null si no hay Security
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Integer id) {
        return repo.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean correoDisponibleParaCrear(String correo) {
        return !repo.existsByCorreo(normalizar(correo));
    }

    @Transactional(readOnly = true)
    public boolean correoDisponibleParaEditar(Integer id, String correo) {
        correo = normalizar(correo);
        return repo.findByCorreo(correo)
                   .map(u -> u.getIdUsuario().equals(id))
                   .orElse(true);
    }

    public Usuario crear(Usuario u) {
        u.setCorreo(normalizar(u.getCorreo()));
        if (!correoDisponibleParaCrear(u.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }
        if (u.getContrasena() != null && !u.getContrasena().isBlank() && encoder != null) {
            u.setContrasena(encoder.encode(u.getContrasena()));
        }
        // Si el admin crea usuarios desde panel, puedes dejar verificado=true
        // u.setVerificado(true);
        return repo.save(u);
    }

    public Usuario actualizar(Integer id, Usuario cambio) {
        cambio.setCorreo(normalizar(cambio.getCorreo()));
        return repo.findById(id).map(db -> {
            // evitar cambiar a un correo que ya está tomado por otro id
            if (!correoDisponibleParaEditar(id, cambio.getCorreo())) {
                throw new IllegalArgumentException("Ese correo ya pertenece a otro usuario.");
            }
            db.setNombre(cambio.getNombre());
            db.setCorreo(cambio.getCorreo());
            db.setRol(cambio.getRol());
            if (cambio.getContrasena() != null && !cambio.getContrasena().isBlank()) {
                if (encoder != null) db.setContrasena(encoder.encode(cambio.getContrasena()));
                else db.setContrasena(cambio.getContrasena());
            }
            return repo.save(db);
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public void eliminar(Integer id) {
        repo.deleteById(id);
    }

    private String normalizar(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }
}
