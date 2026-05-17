package com.example.Inkapark.repositorio;

import com.example.Inkapark.modelo.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BoletaRepositorio extends JpaRepository<Boleta, String> {

    List<Boleta> findByAforo_FechaEventoOrderByEstadoAsc(LocalDate fechaEvento);

    @Query("""
           select coalesce(sum(b.cantidad), 0)
           from Boleta b
           where b.aforo.fechaEvento = :fecha
             and b.estado <> com.example.Inkapark.modelo.Boleta$Estado.CANCELADA
           """)
    Long vendidosDelDia(LocalDate fecha);

    boolean existsById(String id);
}
