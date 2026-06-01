package com.example.Inkapark.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Inkapark.servicio.AuthServicio;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    private final AuthServicio authServicio;

    public AdminControlador(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    // ========== LOGIN ==========
    @GetMapping("/login")
    public String mostrarLogin() {
        return "admin-login"; // /templates/admin-login.html
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("correo") String correo,
                                @RequestParam("contrasena") String contrasena,
                                HttpSession session,
                                Model model) {

        return authServicio.loginAdmin(correo, contrasena)
                .map(u -> {
                    session.setAttribute("ADMIN_ID", u.getIdUsuario());
                    session.setAttribute("ADMIN_NOMBRE", u.getNombre());
                    return "redirect:/admin";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Credenciales inválidas o sin permiso.");
                    return "admin-login";
                });
    }

    // ========== LOGOUT ==========
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ========== PANEL ==========
    @GetMapping
    public String panel() {
        return "admin"; // /templates/admin.html
    }


    @GetMapping("/noticias")
    public String noticias() { return "adminnoticia"; }
}
