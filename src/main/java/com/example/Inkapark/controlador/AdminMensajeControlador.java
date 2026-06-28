package com.example.Inkapark.controlador;

import com.example.Inkapark.modelo.ContactoMensaje;
import com.example.Inkapark.servicio.ContactoMensajeServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/mensajes")
public class AdminMensajeControlador {

    private final ContactoMensajeServicio servicio;

    public AdminMensajeControlador(ContactoMensajeServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String estado,
            @RequestParam(required = false) String ok,
            @RequestParam(required = false) String error,
            Model model) {
        List<ContactoMensaje> mensajes = servicio.listar(estado);
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("estado", estado);
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);
        return "adminform"; // tu plantilla de bandeja
    }

    /**
     * Marcar como atendido + enviar acuse al remitente (POST)
     */
    @PostMapping("/{id}/atender")
    public String atender(@PathVariable Integer id,
            @RequestParam(required = false) String respuesta, // opcional
            @RequestParam(required = false) String estado, // para conservar filtro
            RedirectAttributes ra) {
        boolean ok = servicio.marcarAtendidoYNotificar(id, respuesta);
        if (ok) {
            ra.addAttribute("ok", "Marcado como atendido y correo enviado."); 
        }else {
            ra.addAttribute("error", "No encontrado");
        }
        if (estado != null && !estado.isBlank()) {
            ra.addAttribute("estado", estado);
        }
        return "redirect:/admin/mensajes";
    }

    /**
     * Eliminar (POST)
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id,
            @RequestParam(required = false) String estado,
            RedirectAttributes ra) {
        boolean ok = servicio.eliminar(id);
        if (ok) {
            ra.addAttribute("ok", "Mensaje eliminado"); 
        }else {
            ra.addAttribute("error", "No encontrado");
        }
        if (estado != null && !estado.isBlank()) {
            ra.addAttribute("estado", estado);
        }
        return "redirect:/admin/mensajes";
    }

    @PostMapping("/{id}/responder")
    public String responder(@PathVariable Integer id,
            @RequestParam String respuesta, // textarea del modal
            @RequestParam(required = false) String estado,
            RedirectAttributes ra) {
        boolean ok = servicio.responderYNotificar(id, respuesta);
        if (ok) {
            ra.addAttribute("ok", "Respuesta enviada al usuario."); 
        }else {
            ra.addAttribute("error", "No se pudo enviar la respuesta.");
        }
        if (estado != null && !estado.isBlank()) {
            ra.addAttribute("estado", estado);
        }
        return "redirect:/admin/mensajes";
    }

}
