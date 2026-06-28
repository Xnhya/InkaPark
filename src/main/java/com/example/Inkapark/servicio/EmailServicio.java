package com.example.Inkapark.servicio;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.example.Inkapark.modelo.Boleta;
import com.example.Inkapark.modelo.Pago;
import com.example.Inkapark.modelo.Usuario;

@Service
public class EmailServicio {

    private final JavaMailSender mailSender;

    // Usamos el remitente real desde application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.contact.from-name:InkaPark}")
    private String fromName;

    public EmailServicio(JavaMailSender mailSender) {
        this.mailSender = mailSender;   // <-- NADA MÁS AQUÍ
    }

    /**
     * Verificación de cuenta
     */
    public void enviarVerificacion(String correo, String link) {
        String subject = "Verifica tu cuenta en InkaPark";
        String html = """
            <p>¡Hola!</p>
            <p>Gracias por registrarte en InkaPark. Para activar tu cuenta, haz clic en el siguiente botón:</p>
            <p><a href="%s" style="background:#1363DF;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;">
                Verificar mi cuenta
            </a></p>
            <p>Si el botón no funciona, copia y pega este enlace en tu navegador:<br>%s</p>
            <p>Este enlace expira en 24 horas.</p>
        """.formatted(link, link);

        enviarHtml(correo, subject, html);
    }

    /**
     * Acuse cuando el admin marca como ATENDIDO
     */
    public void enviarAcuseAtencion(String correoUsuario, String nombre, String asuntoOriginal) {
        String subject = "Hemos revisado tu mensaje - InkaPark";
        String html = """
            <p>Hola %s,</p>
            <p>Te confirmamos que hemos <b>revisado</b> tu mensaje con asunto <b>%s</b>.</p>
            <p>¡Gracias por escribirnos! Si es necesario, te responderemos a este mismo correo.</p>
            <p>— Equipo InkaPark</p>
        """.formatted(escape(nombre), escape(asuntoOriginal));

        enviarHtml(correoUsuario, subject, html);
    }

    /**
     * (Opcional) Notificar al staff cuando llega un mensaje de contacto
     */
    public void enviarNotificacionContacto(String destinatarioStaff,
            String nombre, String correo, String asunto, String mensaje) {
        String subject = "Nuevo mensaje de contacto - InkaPark";
        String html = """
            <h3>Nuevo mensaje desde el sitio</h3>
            <ul>
              <li><b>Nombre:</b> %s</li>
              <li><b>Correo:</b> %s</li>
              <li><b>Asunto:</b> %s</li>
            </ul>
            <p><b>Mensaje:</b></p>
            <pre style="white-space:pre-wrap; font-family:inherit">%s</pre>
        """.formatted(escape(nombre), escape(correo), escape(asunto), escape(mensaje));

        enviarHtml(destinatarioStaff, subject, html);
    }

    // -------------------- Helper central --------------------
    private void enviarHtml(String to, String subject, String html) {
        MimeMessagePreparator prep = mensaje -> {
            mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            // Remitente REAL: tu Gmail + nombre mostrado
            mensaje.setFrom(new InternetAddress(fromEmail, fromName));
            mensaje.setSubject(subject, "UTF-8");

            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setText(html, true);
        };
        mailSender.send(prep);
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    public void enviarRespuestaContacto(String correoUsuario, String nombre,
            String asuntoOriginal, String respuestaAdmin) {
        String subject = "Respuesta de InkaPark: " + asuntoOriginal;
        String html = """
        <p>Hola %s,</p>
        <p>Sobre tu consulta <b>%s</b>, te respondemos:</p>
        <blockquote style="border-left:4px solid #ddd;padding:8px 12px;margin:12px 0;">
            %s
        </blockquote>
        <p>Gracias por escribirnos. — Equipo InkaPark</p>
    """.formatted(escape(nombre), escape(asuntoOriginal), escape(respuestaAdmin));
        enviarHtml(correoUsuario, subject, html);  // usa tu helper existente
    }

    public void enviarComprobantePagoTarjeta(Usuario usuario,
            Boleta boleta,
            Pago pago,
            byte[] pdfComprobante,
            byte[] pdfBoletos) {

        String to = usuario.getCorreo();
        String marca = switch (pago.getTipoTarjeta()) {
            case VISA ->
                "VISA";
            case MASTERCARD ->
                "MasterCard";
            case AMEX ->
                "American Express";
            case DINERS ->
                "Diners Club";
            default ->
                "Tarjeta";
        };

        String subject = "Comprobante de pago " + marca + " - InkaPark (" + boleta.getIdBoleta() + ")";

        String html = """
            <div style="font-family:Arial, sans-serif; font-size:14px; color:#222;">
              <div style="background:#0D47A1; color:#fff; padding:16px; border-radius:8px 8px 0 0;">
                <h2 style="margin:0;">InkaPark - Pago con %s</h2>
                <p style="margin:4px 0 0;">Gracias por tu compra, %s</p>
              </div>
              <div style="border:1px solid #ddd; border-top:none; padding:16px; border-radius:0 0 8px 8px;">
                <p>Hemos registrado correctamente tu pago con tarjeta <b>%s</b>.</p>
                <p><b>Detalle del pago:</b></p>
                <ul>
                  <li>Código de boleta: <b>%s</b></li>
                  <li>Fecha del evento: <b>%s</b></li>
                  <li>Cantidad de tickets: <b>%d</b></li>
                  <li>Monto pagado: <b>S/ %s</b></li>
                </ul>
                <p>Adjuntamos tu <b>comprobante de pago</b> y tus <b>boletos InkaPark</b> en formato PDF.</p>
                <p style="margin-top:16px;">¡Te esperamos en el parque!</p>
                <hr>
                <p style="font-size:11px; color:#666;">
                  InkaPark nunca solicitará tu CVV ni contraseñas bancarias por correo electrónico o teléfono.<br>
                  Si recibes un mensaje sospechoso, repórtalo a nuestro canal oficial.
                </p>
              </div>
            </div>
        """.formatted(
                marca,
                escape(usuario.getNombre()),
                marca,
                boleta.getIdBoleta(),
                boleta.getAforo().getFechaEvento().toString(),
                boleta.getCantidad(),
                boleta.getPrecio().toString()
        );

        MimeMessagePreparator prep = mensaje -> {
            mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mensaje.setFrom(new InternetAddress(fromEmail, fromName));
            mensaje.setSubject(subject, "UTF-8");

            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setText(html, true);

            helper.addAttachment("Comprobante_InkaPark.pdf", new ByteArrayResource(pdfComprobante));
            helper.addAttachment("Boletos_InkaPark.pdf", new ByteArrayResource(pdfBoletos));
        };

        mailSender.send(prep);
    }

}
