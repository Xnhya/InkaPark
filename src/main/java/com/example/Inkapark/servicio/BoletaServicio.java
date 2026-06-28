package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.*;
import com.example.Inkapark.repositorio.*;
import com.example.Inkapark.util.BoletaIdGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class BoletaServicio {

    private final BoletaRepositorio boletas;
    private final ManejoAforoRepositorio aforos;
    private final UsuarioRepositorio usuarios;

    public BoletaServicio(BoletaRepositorio boletas, ManejoAforoRepositorio aforos, UsuarioRepositorio usuarios) {
        this.boletas = boletas;
        this.aforos = aforos;
        this.usuarios = usuarios;
    }

    /** Crea boleta (VIGENTE) y descuenta aforo */
    @Transactional
    public Boleta crearBoleta(Integer idUsuario, LocalDate fechaEvento, int cantidad, BigDecimal precioTotal) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        if (precioTotal == null || precioTotal.signum() < 0)
            throw new IllegalArgumentException("Precio inválido");

        var usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        var aforo = aforos.lockByFecha(fechaEvento)
                .orElseThrow(() -> new IllegalArgumentException("No existe aforo para esa fecha"));

        if (aforo.getAforoDisponible() < cantidad)
            throw new IllegalStateException("No hay aforo suficiente");

        // Descontar aforo
        aforo.setAforoDisponible(aforo.getAforoDisponible() - cantidad);
        aforos.save(aforo);

        // Generar ID único (intenta unas veces por si colisiona)
        String id;
        int intentos = 0;
        do {
            id = BoletaIdGenerator.generateId(8); // p.ej. 23OP2I6M
            intentos++;
        } while (boletas.existsById(id) && intentos < 5);
        if (boletas.existsById(id)) {
            throw new IllegalStateException("No se pudo generar un código único de boleta.");
        }

        // Crear y guardar boleta
        var b = new Boleta();
        b.setIdBoleta(id);
        b.setUsuario(usuario);
        b.setAforo(aforo);
        b.setCantidad(cantidad);
        b.setPrecio(precioTotal);
        b.setEstado(Boleta.Estado.VIGENTE);

        return boletas.save(b);
    }

    /** Cambiar estado; si CANCELADA → devolver aforo */
    @Transactional
    public void cambiarEstado(String idBoleta, Boleta.Estado nuevo) {
        var b = boletas.findById(idBoleta)
                .orElseThrow(() -> new IllegalArgumentException("Boleta no existe"));

        if (b.getEstado() == Boleta.Estado.CANCELADA) return;

        if (nuevo == Boleta.Estado.CANCELADA) {
            var aforo = aforos.lockByFecha(b.getAforo().getFechaEvento()).orElseThrow();
            aforo.setAforoDisponible(aforo.getAforoDisponible() + b.getCantidad());
            aforos.save(aforo);
        }

        b.setEstado(nuevo);
        boletas.save(b);
    }
}
