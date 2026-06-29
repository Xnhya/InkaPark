package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.Boleta;
import com.example.Inkapark.modelo.ManejoAforo;
import com.example.Inkapark.modelo.Pago;
import com.example.Inkapark.modelo.Usuario;
import com.example.Inkapark.repositorio.BoletaRepositorio;
import com.example.Inkapark.repositorio.ManejoAforoRepositorio;
import com.example.Inkapark.repositorio.PagoRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TicketsServicio {

    private final ManejoAforoRepositorio aforoRepo;
    private final BoletaRepositorio boletaRepo;
    private final PagoRepositorio pagoRepo;
    private final EmailServicio emailServicio;
    private final PdfServicio pdfServicio;

    @Value("${incapark.tickets.aforo.default:50}")
    private int aforoDefault;

    @Value("${incapark.tickets.precio:50.00}")
    private BigDecimal precioTicket; 

    public TicketsServicio(ManejoAforoRepositorio aforoRepo,
                           BoletaRepositorio boletaRepo,
                           PagoRepositorio pagoRepo,
                           EmailServicio emailServicio,
                           PdfServicio pdfServicio) {

        this.aforoRepo = aforoRepo;
        this.boletaRepo = boletaRepo;
        this.pagoRepo = pagoRepo;
        this.emailServicio = emailServicio;
        this.pdfServicio = pdfServicio;
    }

    public BigDecimal getPrecioTicket() {
        return precioTicket;
    }

    public List<ManejoAforo> proximos7(LocalDate hoy) {
        List<ManejoAforo> out = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate f = hoy.plusDays(i);
            ManejoAforo m = aforoRepo.findById(f).orElseGet(() -> {
                ManejoAforo nuevo = new ManejoAforo();
                nuevo.setFechaEvento(f);
                nuevo.setAforoTotal(aforoDefault);
                nuevo.setAforoDisponible(aforoDefault);
                return aforoRepo.save(nuevo);
            });
            out.add(m);
        }
        return out;
    }

    public ManejoAforo obtener(LocalDate fecha) {
        return aforoRepo.findById(fecha).orElseThrow();
    }

    private Pago.TipoTarjeta detectarTipoTarjeta(String numero) {
        if (numero == null || numero.isBlank()) return Pago.TipoTarjeta.OTRA;
        char first = numero.charAt(0);
        return switch (first) {
            case '1' -> Pago.TipoTarjeta.VISA;
            case '2' -> Pago.TipoTarjeta.MASTERCARD;
            case '3' -> Pago.TipoTarjeta.AMEX;
            case '4' -> Pago.TipoTarjeta.DINERS;
            default  -> Pago.TipoTarjeta.OTRA;
        };
    }

    /**
     * Pago con tarjeta:
     * - Bloquea aforo
     * - Valida stock
     * - Crea Boleta
     * - Crea Pago (con tarjeta)
     * - Actualiza aforo
     * - Genera PDFs
     * - Envía correo
     */
    @Transactional
    public String pagarConTarjeta(Usuario usuario,
                                  LocalDate fecha,
                                  int cantidad,
                                  String numeroTarjeta,
                                  String cvv,
                                  String vencimiento,
                                  String telefono,
                                  String direccion,
                                  java.util.function.Supplier<String> idBoleta) {

        if (cantidad < 1) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        String numClean = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\s+", "");
        Pago.TipoTarjeta tipo = detectarTipoTarjeta(numClean);
        int esperado = (tipo == Pago.TipoTarjeta.AMEX) ? 15 : 16;
        if (numClean.length() != esperado) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        if (cvv == null || cvv.length() != 3) {
            throw new IllegalArgumentException("CVV inválido");
        }

        ManejoAforo af = aforoRepo.lockByFecha(fecha)
                .orElseThrow(() -> new IllegalArgumentException("No existe aforo para la fecha seleccionada"));

        if (af.getAforoDisponible() < cantidad) {
            throw new IllegalArgumentException("Aforo insuficiente para esa cantidad");
        }

        String id = idBoleta.get();

        // Boleta
        Boleta b = new Boleta();
        b.setIdBoleta(id);
        b.setUsuario(usuario);
        b.setAforo(af);
        b.setCantidad(cantidad);
        b.setPrecio(precioTicket.multiply(BigDecimal.valueOf(cantidad)));
        b.setEstado(Boleta.Estado.VIGENTE);
        boletaRepo.save(b);

        // Pago
        Pago p = new Pago();
        p.setBoleta(b);
        p.setMetodo(Pago.Metodo.TARJETA);
        p.setTipoTarjeta(detectarTipoTarjeta(numClean));
        p.setNumeroTarjeta(numClean);  // número largo que pediste
        p.setCvvEnc(cvv);              // se cifra con SimpleEncryptor
        p.setFechaVencEnc(vencimiento);
        p.setMonto(b.getPrecio());
        p.setEstado(Pago.Estado.CONFIRMADO);
        p.setFechaPago(java.time.LocalDateTime.now());
        pagoRepo.save(p);

        // Aforo
        af.setAforoDisponible(af.getAforoDisponible() - cantidad);
        aforoRepo.save(af);

        // PDFs
        byte[] pdfComprobante = pdfServicio.generarComprobantePago(usuario, b, p, telefono, direccion);
        byte[] pdfBoletos = pdfServicio.generarBoletos(usuario, b);

        // Email
        //emailServicio.enviarComprobantePagoTarjeta(usuario, b, p, pdfComprobante, pdfBoletos);
        try {
            emailServicio.enviarComprobantePagoTarjeta(usuario, b, p, pdfComprobante, pdfBoletos);
        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
        }
        return id;
    }

    public static String nombreDia(LocalDate fecha, Locale locale) {
        String s = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL,
                locale == null ? new Locale("es", "PE") : locale);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
