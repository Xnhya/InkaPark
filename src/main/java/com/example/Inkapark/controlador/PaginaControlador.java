package com.example.Inkapark.controlador;

import com.example.Inkapark.servicio.ContactoMensajeServicio;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaginaControlador {

    private final ContactoMensajeServicio contactoMensajeServicio;

    public PaginaControlador(ContactoMensajeServicio contactoMensajeServicio) {
        this.contactoMensajeServicio = contactoMensajeServicio;
    }

    @GetMapping({"/", "/inicio"}) public String home()      { return "index"; }
    @GetMapping("/atracciones")   public String atracciones(){ return "atracciones"; }
    @GetMapping("/galeria")       public String galeria()    { return "galeria"; }
    @GetMapping("/nosotros")      public String nosotros()   { return "nosotros"; }

    @GetMapping("/contacto") public String contacto() { return "contacto"; }

    @PostMapping("/contacto")
    public String postContacto(@RequestParam String nombre,
                               @RequestParam String correo,
                               @RequestParam String asunto,
                               @RequestParam String mensaje) {
        try {
            contactoMensajeServicio.guardarDesdeFormulario(nombre, correo, asunto, mensaje);
            return "redirect:/contacto?ok=Mensaje%20enviado";
        } catch (Exception e) {
            return "redirect:/contacto?error=No%20se%20pudo%20enviar";
        }
    }

    @GetMapping("/login")    public String login()    { return "login"; }
    @GetMapping("/registro") public String registro() { return "registro"; }
}
