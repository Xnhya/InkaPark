package com.example.Inkapark.controlador;

import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.repositorio.UsuarioRepositorio;
import com.example.Inkapark.servicio.AuthServicio;
import com.example.Inkapark.servicio.RegistroServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthControlador {

    private final AuthServicio authServicio;
    private final RegistroServicio registroServicio;
    private final UsuarioRepositorio usuarioRepositorio;

    public AuthControlador(AuthServicio authServicio,
                           RegistroServicio registroServicio,
                           UsuarioRepositorio usuarioRepositorio) {
        this.authServicio = authServicio;
        this.registroServicio = registroServicio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // ======== LOGIN ========
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "redirect", required = false) String redirect, Model model) {
        if (redirect != null && !redirect.isBlank()) {
            model.addAttribute("redirect", redirect);
        }
        return "login"; // /templates/login.html
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                HttpSession session,
                                Model model) {

        String correoNorm = email == null ? null : email.trim().toLowerCase();

        // Mensaje claro si la cuenta existe pero NO está verificada
        usuarioRepositorio.findByCorreo(correoNorm).ifPresent(u -> {
            if (!u.isVerificado()) {
                model.addAttribute("error", "Tu cuenta no está verificada. Revisa tu correo @gmail.com o solicita un nuevo enlace.");
            }
        });
        if (model.containsAttribute("error")) {
            if (redirect != null && !redirect.isBlank()) model.addAttribute("redirect", redirect);
            return "login";
        }

        return authServicio.login(correoNorm, password)
                .map(u -> {
                    // Sesión
                    session.setAttribute("USER_ID", u.getIdUsuario());
                    session.setAttribute("USER_NOMBRE", u.getNombre());
                    session.setAttribute("USER_ROL", u.getRol().name());
                    session.setAttribute("usuario", u);

                    // Redirección interna prioritaria
                    if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
                        return "redirect:" + redirect;
                    }
                    // Admin a /admin, cliente a /
                    return (u.getRol() == Usuario.Rol.ADMIN) ? "redirect:/admin" : "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Correo o contraseña incorrectos.");
                    if (redirect != null && !redirect.isBlank()) model.addAttribute("redirect", redirect);
                    return "login";
                });
    }

    // ======== REGISTRO ========
    @GetMapping("/register")
    public String mostrarRegistro() {
        return "registro"; // /templates/registro.html
    }

    @PostMapping("/register")
    public String procesarRegistro(@RequestParam String name,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   Model model) {
        try {
            // Envía correo con token y deja la cuenta pendiente de verificación
            registroServicio.registrarCliente(name, email, password);
            model.addAttribute("ok", "Registro iniciado. Revisa tu correo @gmail.com para verificar tu cuenta.");
            return "login"; // Muestra el login con el mensaje OK
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    // ======== VERIFICAR CUENTA (desde el correo) ========
    // Asegúrate de construir el link como /auth/verificar?email=...&token=...
    @GetMapping("/verificar")
    public String verificarCuenta(@RequestParam("email") String email,
                                  @RequestParam("token") String token,
                                  Model model) {
        try {
            registroServicio.verificarCuenta(email, token);
            model.addAttribute("ok", "Tu cuenta se ha verificado correctamente. Ya puedes iniciar sesión.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "login";
    }

    // ======== REENVIAR VERIFICACIÓN (opcional) ========
    @PostMapping("/reenviar-verificacion")
    public String reenviarVerificacion(@RequestParam("email") String email, Model model) {
        try {
            registroServicio.reenviarVerificacion(email);
            model.addAttribute("ok", "Si existe una cuenta con ese correo y no está verificada, enviamos un nuevo enlace.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "login";
    }

    // ======== LOGOUT ========
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
