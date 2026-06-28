package com.example.Inkapark.controlador;

import com.example.Inkapark.modelo.Boleta;
import com.example.Inkapark.repositorio.BoletaRepositorio;
import com.example.Inkapark.repositorio.ManejoAforoRepositorio;
import com.example.Inkapark.servicio.BoletaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/admin/tickets")
public class AdminTicketControlador {

    private final BoletaRepositorio boletaRepo;
    private final ManejoAforoRepositorio aforoRepo;
    private final BoletaServicio boletaSrv;

    public AdminTicketControlador(BoletaRepositorio boletaRepo,
                                  ManejoAforoRepositorio aforoRepo,
                                  BoletaServicio boletaSrv) {
        this.boletaRepo = boletaRepo;
        this.aforoRepo = aforoRepo;
        this.boletaSrv = boletaSrv;
    }

    /** Panel por día (default: hoy) */
    @GetMapping
    public String panel(@RequestParam(value = "fecha", required = false) String fechaStr,
                        @RequestParam(value = "ok", required = false) String ok,
                        @RequestParam(value = "error", required = false) String error,
                        Locale locale,
                        Model model) {

        LocalDate hoy = LocalDate.now();
        LocalDate fecha = (fechaStr == null || fechaStr.isBlank()) ? hoy : LocalDate.parse(fechaStr);

        // Semana lun-dom
        LocalDate lunes = fecha.with(DayOfWeek.MONDAY);
        List<DiaView> semana = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate f = lunes.plusDays(i);
            String nombre = f.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
            nombre = nombre.substring(0, 1).toUpperCase(locale) + nombre.substring(1);
            semana.add(new DiaView(nombre, f));
        }

        // Aforo
        var aforoOpt = aforoRepo.findById(fecha);
        Integer aforoTotal = aforoOpt.map(a -> a.getAforoTotal()).orElse(0);
        Integer aforoDisp  = aforoOpt.map(a -> a.getAforoDisponible()).orElse(0);

        // Vendidos (Long → int)
        Long vendidosLong = Optional.ofNullable(boletaRepo.vendidosDelDia(fecha)).orElse(0L);
        int vendidos = vendidosLong.intValue();

        // Boletas del día
        var boletas = boletaRepo.findByAforo_FechaEventoOrderByEstadoAsc(fecha);

        model.addAttribute("fecha", fecha);
        model.addAttribute("semana", semana);
        model.addAttribute("boletas", boletas);
        model.addAttribute("aforoTotal", aforoTotal);
        model.addAttribute("vendidos", vendidos);
        model.addAttribute("aforoDisp", aforoDisp);
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);

        return "adminticket";
    }

    /** Verificar ticket (VIGENTE → USADA) */
    @PostMapping("/verificar")
    public String verificar(@RequestParam("codigo") String idBoleta,
                            @RequestParam("fecha") String fechaStr) {
        try {
            String code = idBoleta == null ? "" : idBoleta.trim();
            var b = boletaRepo.findById(code)
                    .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

            if (b.getEstado() == Boleta.Estado.USADA) {
                return "redirect:/admin/tickets?fecha=" + fechaStr + "&error=Ya%20fue%20verificado";
            }
            if (b.getEstado() == Boleta.Estado.CANCELADA) {
                return "redirect:/admin/tickets?fecha=" + fechaStr + "&error=Boleto%20cancelado";
            }

            boletaSrv.cambiarEstado(code, Boleta.Estado.USADA);
            return "redirect:/admin/tickets?fecha=" + fechaStr + "&ok=Verificado";

        } catch (Exception e) {
            return "redirect:/admin/tickets?fecha=" + fechaStr + "&error=" + e.getMessage();
        }
    }

    public record DiaView(String nombre, LocalDate fecha) {}
}
