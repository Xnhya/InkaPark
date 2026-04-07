package com.example.Inkapark.repositorio;

import com.example.Inkapark.modelo.ContactoMensaje;
import com.example.Inkapark.modelo.ContactoMensaje.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactoMensajeRepositorio extends JpaRepository<ContactoMensaje, Integer> {
    List<ContactoMensaje> findByEstadoOrderByFechaEnvioDesc(Estado estado);
    List<ContactoMensaje> findAllByOrderByFechaEnvioDesc();
}

