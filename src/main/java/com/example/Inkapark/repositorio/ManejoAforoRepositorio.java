package com.example.Inkapark.repositorio;

import com.example.Inkapark.modelo.ManejoAforo;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface ManejoAforoRepositorio extends JpaRepository<ManejoAforo, LocalDate> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from ManejoAforo m where m.fechaEvento = :fecha")
    Optional<ManejoAforo> lockByFecha(@Param("fecha") LocalDate fecha);
}
