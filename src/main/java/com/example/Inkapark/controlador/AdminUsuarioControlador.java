package com.example.Inkapark.controlador;

import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.servicio.UsuarioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioControlador {

    private final UsuarioServicio servicio;

    public AdminUsuarioControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model,
                         @RequestParam(required = false) String ok,
                         @RequestParam(required = false) String error) {
        model.addAttribute("usuarios", servicio.listar());
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);
        return "adminusuario"; // vista de listado
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("esEdicion", false);
        return "usuario-form"; // vista de formulario
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuario, RedirectAttributes ra) {
        try {
            if (usuario.getIdUsuario() == null) {
                servicio.crear(usuario);
                ra.addAttribute("ok", "Usuario creado");
            } else {
                servicio.actualizar(usuario.getIdUsuario(), usuario);
                ra.addAttribute("ok", "Usuario actualizado");
            }
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            ra.addAttribute("error", e.getMessage());
            if (usuario.getIdUsuario() == null) {
                return "redirect:/admin/usuarios/nuevo";
            } else {
                return "redirect:/admin/usuarios/editar/" + usuario.getIdUsuario();
            }
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Usuario u = servicio.obtener(id);
        if (u == null) {
            ra.addAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
        // Por seguridad, no mandes la contraseña al form (se queda vacío)
        u.setContrasena("");
        model.addAttribute("usuario", u);
        model.addAttribute("esEdicion", true);
        return "usuario-form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            servicio.eliminar(id);
            ra.addAttribute("ok", "Usuario eliminado");
        } catch (Exception e) {
            ra.addAttribute("error", "No se pudo eliminar");
        }
        return "redirect:/admin/usuarios";
    }
}
