package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.ManejoAforo;
import com.example.Inkapark.repositorio.BoletaRepositorio;
import com.example.Inkapark.repositorio.ManejoAforoRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ManejoAforoServicio {

    private final ManejoAforoRepositorio repo;
    private final BoletaRepositorio boletas;

    // Límite global configurable (por defecto 50)
    private final int aforoMax;

    public ManejoAforoServicio(
            ManejoAforoRepositorio repo,
            BoletaRepositorio boletas,
            @Value("${aforo.max:50}") int aforoMax
    ) {
        this.repo = repo;
        this.boletas = boletas;
        this.aforoMax = aforoMax;
    }

    /** Devuelve/crea aforo del día con total=aforoMax y disponible ajustado a vendidos */
    @Transactional
    public ManejoAforo ensureAforo(LocalDate fecha) {
        return repo.lockByFecha(fecha).orElseGet(() -> {
            ManejoAforo nuevo = new ManejoAforo();
            nuevo.setFechaEvento(fecha);
            nuevo.setAforoTotal(aforoMax);

            Long vendidosLong = Optional.ofNullable(boletas.vendidosDelDia(fecha)).orElse(0L);
            int vendidos = vendidosLong.intValue();

            nuevo.setAforoDisponible(Math.max(0, aforoMax - vendidos));
            return repo.save(nuevo);
        });
    }

    /** Cambia el aforo total del día y recalcula el disponible respetando vendidos */
    @Transactional
    public ManejoAforo ajustarAforo(LocalDate fecha, int nuevoTotal) {
        ManejoAforo m = repo.lockByFecha(fecha).orElseGet(() -> {
            ManejoAforo x = new ManejoAforo();
            x.setFechaEvento(fecha);
            return x;
        });

        int total = Math.min(nuevoTotal, aforoMax);

        Long vendidosLong = Optional.ofNullable(boletas.vendidosDelDia(fecha)).orElse(0L);
        int vendidos = vendidosLong.intValue();

        m.setAforoTotal(total);
        m.setAforoDisponible(Math.max(0, total - vendidos));

        return repo.save(m);
    }
}
