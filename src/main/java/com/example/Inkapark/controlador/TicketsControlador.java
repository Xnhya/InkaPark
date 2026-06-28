package com.example.Inkapark.controlador;

import com.example.Inkapark.modelo.ManejoAforo;
import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.repositorio.UsuarioRepositorio;
import com.example.Inkapark.servicio.TicketsServicio;
import com.example.Inkapark.util.BoletaIdGenerator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/tickets")
public class TicketsControlador {

    private final TicketsServicio ticketsServicio;
    private final UsuarioRepositorio usuarioRepo;

    public TicketsControlador(TicketsServicio ticketsServicio,
            UsuarioRepositorio usuarioRepo) {
        this.ticketsServicio = ticketsServicio;
        this.usuarioRepo = usuarioRepo;
    }

    /**
     * Obtiene el usuario logueado desde sesión (soporta 2 esquemas): -
     * "usuario" => objeto Usuario guardado en sesión - "USER_ID" => sólo el id
     * (se busca en BD)
     */
    private Usuario getUsuarioDeSesion(HttpSession session) {
        Object uObj = session.getAttribute("usuario");
        if (uObj instanceof Usuario u) {
            return u;
        }

        Object idObj = session.getAttribute("USER_ID");
        if (idObj instanceof Integer id) {
            return usuarioRepo.findById(id).orElse(null);
        }
        return null;
    }

    /**
     * Página de tickets. Siempre muestra los próximos 7 días.
     */
    @GetMapping
    public String pagina(@RequestParam(value = "f", required = false) String f,
            HttpSession session,
            Model model,
            Locale locale) {

        LocalDate hoy = LocalDate.now();

        // 7 días desde BD (si falta alguno, el servicio lo crea con aforo por defecto)
        List<ManejoAforo> dias = ticketsServicio.proximos7(hoy);
        model.addAttribute("dias", dias);
        model.addAttribute("precio", ticketsServicio.getPrecioTicket());
        model.addAttribute("locale", (locale != null) ? locale : new Locale("es", "PE"));

        // Fecha seleccionada: ?f=yyyy-MM-dd o el primer día de la lista
        LocalDate seleccionFecha = (f != null && !f.isBlank())
                ? LocalDate.parse(f)
                : (dias.isEmpty() ? hoy : dias.get(0).getFechaEvento());

        ManejoAforo seleccion = ticketsServicio.obtener(seleccionFecha);
        model.addAttribute("seleccion", seleccion);

        // La vista usa session.usuario / USER_NOMBRE para decidir si muestra form o alerta
        return "tickets";
    }

    /**
     * Pago con YAPE o PLIN: crea Boleta, Pago y descuenta aforo.
     */
    @PostMapping("/pagar")
    public String pagar(@RequestParam("fechaStr") String fechaStr,
            @RequestParam("cantidad") int cantidad,
            @RequestParam("numeroTarjeta") String numeroTarjeta,
            @RequestParam("cvv") String cvv,
            @RequestParam("vencimiento") String vencimiento,
            @RequestParam("telefono") String telefono,
            @RequestParam("direccion") String direccion,
            HttpSession session,
            RedirectAttributes ra) {

        try {
            Usuario u = getUsuarioDeSesion(session);
            if (u == null) {
                ra.addFlashAttribute("error", "Debes iniciar sesión para comprar un ticket.");
                return "redirect:/auth/login";
            }

            LocalDate fecha = LocalDate.parse(fechaStr);

            String idBoleta = ticketsServicio.pagarConTarjeta(
                    u,
                    fecha,
                    cantidad,
                    numeroTarjeta,
                    cvv,
                    vencimiento,
                    telefono,
                    direccion,
                    () -> BoletaIdGenerator.generateId(12)
            );

            ra.addFlashAttribute("ok", "Pago exitoso. Código de boleta: " + idBoleta);
            return "redirect:/tickets?f=" + fechaStr;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/tickets?f=" + fechaStr;
        }
    }
}
