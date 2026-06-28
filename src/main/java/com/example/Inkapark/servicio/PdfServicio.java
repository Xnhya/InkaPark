package com.example.Inkapark.servicio;

import com.example.Inkapark.modelo.Boleta;
import com.example.Inkapark.modelo.Pago;
import com.example.Inkapark.modelo.Usuario;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfServicio {

    public byte[] generarComprobantePago(Usuario u, Boleta b, Pago p,
            String telefono, String direccion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Fuentes
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 13, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font headerWhite = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);

            // ================= 1) Encabezado =================
            String marca = (p.getTipoTarjeta() == null) ? "Tarjeta" : switch (p.getTipoTarjeta()) {
                case VISA ->
                    "VISA";
                case MASTERCARD ->
                    "MasterCard";
                case AMEX ->
                    "American Express";
                case DINERS ->
                    "Diners Club";
                case OTRA ->
                    "Tarjeta";
            };

            String titulo = "Comprobante de pago - " + marca;

            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);

            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(new Color(13, 71, 161)); // azul InkaPark
            hCell.setPaddingTop(12);
            hCell.setPaddingBottom(12);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph headerText = new Paragraph(titulo + "\nInkaPark", headerWhite);
            headerText.setAlignment(Element.ALIGN_CENTER);
            hCell.addElement(headerText);
            hCell.setBorder(Rectangle.NO_BORDER);

            header.addCell(hCell);
            doc.add(header);

            doc.add(new Paragraph(" ", normal));

            // ================= 2) Datos del cliente =================
            Paragraph secCliente = new Paragraph("Datos del cliente", sectionFont);
            secCliente.setSpacingBefore(5f);
            secCliente.setSpacingAfter(5f);
            doc.add(secCliente);

            PdfPTable tablaCliente = new PdfPTable(2);
            tablaCliente.setWidthPercentage(100);
            tablaCliente.setSpacingAfter(10f);
            tablaCliente.setWidths(new float[] { 30f, 70f });

            addRow(tablaCliente, "Cliente:", safe(u.getNombre()), normal, bold);
            addRow(tablaCliente, "Correo:", safe(u.getCorreo()), normal, bold);
            addRow(tablaCliente, "Teléfono:", safe(telefono), normal, bold);
            addRow(tablaCliente, "Dirección:", safe(direccion), normal, bold);

            doc.add(tablaCliente);

            // ================= 3) Detalle de la operación =================
            Paragraph secOp = new Paragraph("Detalle de la operación", sectionFont);
            secOp.setSpacingBefore(5f);
            secOp.setSpacingAfter(5f);
            doc.add(secOp);

            PdfPTable tablaOperacion = new PdfPTable(2);
            tablaOperacion.setWidthPercentage(100);
            tablaOperacion.setSpacingAfter(10f);
            tablaOperacion.setWidths(new float[] { 35f, 65f });

            String fechaEvento = b.getAforo().getFechaEvento()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String fechaPago = p.getFechaPago()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            addRow(tablaOperacion, "Código de boleta:", safe(b.getIdBoleta()), normal, bold);
            addRow(tablaOperacion, "Fecha del evento:", fechaEvento, normal, bold);
            addRow(tablaOperacion, "Cantidad de tickets:", String.valueOf(b.getCantidad()), normal, bold);
            addRow(tablaOperacion, "Monto pagado:", "S/ " + b.getPrecio(), normal, bold);
            addRow(tablaOperacion, "Fecha de pago:", fechaPago, normal, bold);

            doc.add(tablaOperacion);

            // ================= 4) Resumen de la tarjeta =================
            Paragraph secTarjeta = new Paragraph("Detalle de la tarjeta", sectionFont);
            secTarjeta.setSpacingBefore(5f);
            secTarjeta.setSpacingAfter(5f);
            doc.add(secTarjeta);

            PdfPTable tablaTarjeta = new PdfPTable(2);
            tablaTarjeta.setWidthPercentage(100);
            tablaTarjeta.setSpacingAfter(15f);
            tablaTarjeta.setWidths(new float[] { 35f, 65f });

            // Enmascarar número largo
            String numeroMasked = "**** **** **** ****";
            String numReal = p.getNumeroTarjeta();
            if (numReal != null && numReal.length() >= 4) {
                String last4 = numReal.substring(numReal.length() - 4);
                numeroMasked = "**** **** **** " + last4;
            }

            addRow(tablaTarjeta, "Marca:", marca, normal, bold);
            addRow(tablaTarjeta, "Número:", numeroMasked, normal, bold);
            addRow(tablaTarjeta, "Vencimiento:", safe(p.getFechaVencEnc()), normal, bold);

            doc.add(tablaTarjeta);

            // ================= 5) Bloque de resumen visual =================
            PdfPTable resumen = new PdfPTable(3);
            resumen.setWidthPercentage(100);
            resumen.setSpacingAfter(15f);
            resumen.setWidths(new float[] { 33f, 33f, 34f });

            PdfPCell r1 = resumenCell("Total pagado", "S/ " + b.getPrecio(), bold, normal);
            PdfPCell r2 = resumenCell("Tickets", String.valueOf(b.getCantidad()), bold, normal);
            PdfPCell r3 = resumenCell("Fecha evento", fechaEvento, bold, normal);

            resumen.addCell(r1);
            resumen.addCell(r2);
            resumen.addCell(r3);

            doc.add(resumen);

            // ================= 6) Términos y condiciones =================
            Paragraph termTitle = new Paragraph("Términos y condiciones de la compra", sectionFont);
            termTitle.setSpacingBefore(5f);
            termTitle.setSpacingAfter(5f);
            doc.add(termTitle);

            Paragraph terminos = new Paragraph(
                    "- Este comprobante es válido solo para el ingreso a InkaPark en la fecha indicada.\n"
                            + "- El titular de la tarjeta deberá presentar un documento de identidad al ingresar.\n"
                            + "- No se aceptan devoluciones pasadas las 24 horas de realizada la compra.\n"
                            + "- Las operaciones se procesan únicamente con tarjetas emitidas en el territorio peruano.\n"
                            + "- Para este proyecto académico se simula el cobro; no se realizan cargos reales.\n"
                            + "- InkaPark no solicita claves ni contraseñas de tu banca por internet.",
                    normal);
            doc.add(terminos);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de comprobante", e);
        }
    }

    public byte[] generarBoletos(Usuario u, Boleta b) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Fuentes y colores
            Font title = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(13, 71, 161)); // azul InkaPark
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font small = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(90, 90, 90));

            // ===== Cabecera general =====
            Paragraph encabezado = new Paragraph("Boleto de ingreso - InkaPark", title);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setSpacingAfter(10f);
            doc.add(encabezado);

            // Info básica arriba
            doc.add(new Paragraph("Titular: " + safe(u.getNombre()), normal));
            doc.add(new Paragraph("Código de boleta: " + safe(b.getIdBoleta()), normal));
            doc.add(new Paragraph("Fecha del evento: "
                    + b.getAforo().getFechaEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normal));
            doc.add(new Paragraph(" ", normal));

            // ===== Cargamos la imagen del boleto (si existe) =====
            Image boletoImg = null;
            try {
                URL url = PdfServicio.class.getResource("/static/images/Boleto.jpg");
                if (url != null) {
                    boletoImg = Image.getInstance(url);
                    boletoImg.scaleToFit(260, 110); // más pequeño, estilo postal
                }
            } catch (Exception e) {
                boletoImg = null; // si falla, simplemente no se muestra
            }

            // ===== Tarjeta principal tipo "ticket" =====
            PdfPTable ticketCard = new PdfPTable(2);
            ticketCard.setWidthPercentage(100);
            ticketCard.setWidths(new float[] { 65f, 35f });

            // --- Columna izquierda: imagen + info principal ---
            PdfPCell left = new PdfPCell();
            left.setPadding(12f);
            left.setBorderColor(new Color(210, 210, 210));
            left.setBackgroundColor(new Color(250, 248, 240)); // beige suave

            if (boletoImg != null) {
                boletoImg.setAlignment(Image.ALIGN_LEFT);
                boletoImg.setSpacingAfter(6f);
                left.addElement(boletoImg);
            }

            Paragraph tituloBoleto = new Paragraph("Acceso general al parque InkaPark", bold);
            tituloBoleto.setSpacingAfter(4f);
            left.addElement(tituloBoleto);

            left.addElement(new Paragraph(
                    "Este boleto permite el ingreso al parque en la fecha indicada.", normal));
            left.addElement(new Paragraph(
                    "Presenta este boleto en la puerta de ingreso junto con tu documento de identidad.", normal));

            left.addElement(new Paragraph(" ", normal));
            left.addElement(new Paragraph("Titular: " + safe(u.getNombre()), normal));
            left.addElement(new Paragraph("Cantidad de personas: " + b.getCantidad(), normal));
            left.addElement(new Paragraph("Fecha del evento: "
                    + b.getAforo().getFechaEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normal));

            // --- Columna derecha: "banda" con código / tipo ---
            PdfPCell right = new PdfPCell();
            right.setPadding(10f);
            right.setBorderColor(new Color(210, 210, 210));
            right.setBackgroundColor(new Color(232, 234, 246)); // lila/azul muy claro

            Paragraph etiqueta = new Paragraph("BOLETO DIGITAL", bold);
            etiqueta.setAlignment(Element.ALIGN_CENTER);
            etiqueta.setSpacingAfter(10f);
            right.addElement(etiqueta);

            Paragraph codigoLabel = new Paragraph("Código:", normal);
            codigoLabel.setAlignment(Element.ALIGN_CENTER);
            right.addElement(codigoLabel);

            Paragraph codigoValor = new Paragraph(safe(b.getIdBoleta()), bold);
            codigoValor.setAlignment(Element.ALIGN_CENTER);
            codigoValor.setSpacingAfter(10f);
            right.addElement(codigoValor);

            Paragraph infoCant = new Paragraph(
                    "Válido para " + b.getCantidad() + " persona(s)", normal);
            infoCant.setAlignment(Element.ALIGN_CENTER);
            infoCant.setSpacingAfter(10f);
            right.addElement(infoCant);

            Paragraph notaSide = new Paragraph(
                    "Presentar completo y legible.\nUso único.", small);
            notaSide.setAlignment(Element.ALIGN_CENTER);
            right.addElement(notaSide);

            ticketCard.addCell(left);
            ticketCard.addCell(right);

            doc.add(ticketCard);

            // ===== Nota al pie =====
            doc.add(new Paragraph(" ", normal));
            Paragraph footer = new Paragraph(
                    "Este boleto es válido solo para la fecha indicada y para la cantidad de personas señalada. "
                            + "Proyecto académico: no se realizan cargos reales.",
                    small);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(8f);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de boletos", e);
        }
    }

    // ================= Helpers =================
    private void addRow(PdfPTable t, String label, String value, Font normal, Font bold) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, bold));
        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "", normal));
        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3f);
        c2.setPadding(3f);
        t.addCell(c1);
        t.addCell(c2);
    }

    private PdfPCell resumenCell(String label, String value, Font bold, Font normal) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8f);
        cell.setBorderColor(new Color(200, 200, 200));

        Paragraph l = new Paragraph(label, normal);
        l.setAlignment(Element.ALIGN_CENTER);
        Paragraph v = new Paragraph(value, bold);
        v.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(l);
        cell.addElement(v);
        return cell;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}
