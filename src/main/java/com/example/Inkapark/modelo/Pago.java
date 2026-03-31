package com.example.Inkapark.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.Inkapark.util.SimpleEncryptor;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pago")
public class Pago {

    public enum Metodo { YAPE, PLIN, TARJETA }

    public enum Estado { CONFIRMADO, FALLIDO }

    public enum TipoTarjeta { VISA, MASTERCARD, AMEX, DINERS, OTRA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_boleta", referencedColumnName = "id_boleta", nullable = false)
    private Boleta boleta;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('YAPE','PLIN','TARJETA')")
    private Metodo metodo;

    // NUEVO: tipo de tarjeta (derivado del primer dígito)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarjeta", nullable = false,
            columnDefinition = "ENUM('VISA','MASTERCARD','AMEX','DINERS','OTRA')")
    private TipoTarjeta tipoTarjeta;

    // Número largo SIN cifrar (solo para tu demo)
    @Column(name = "numero_tarjeta", nullable = false, length = 32)
    private String numeroTarjeta;

    // CVV cifrado
    @Convert(converter = SimpleEncryptor.class)
    @Column(name = "cvv_enc", nullable = false, length = 255)
    private String cvvEnc;

    // Fecha de vencimiento cifrada (MM/AA)
    @Convert(converter = SimpleEncryptor.class)
    @Column(name = "fecha_venc_enc", nullable = false, length = 255)
    private String fechaVencEnc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('CONFIRMADO','FALLIDO')")
    private Estado estado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    // ---------- getters y setters ----------

    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public Boleta getBoleta() {
        return boleta;
    }

    public void setBoleta(Boleta boleta) {
        this.boleta = boleta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Metodo getMetodo() {
        return metodo;
    }

    public void setMetodo(Metodo metodo) {
        this.metodo = metodo;
    }

    public TipoTarjeta getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(TipoTarjeta tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getCvvEnc() {
        return cvvEnc;
    }

    public void setCvvEnc(String cvvEnc) {
        this.cvvEnc = cvvEnc;
    }

    public String getFechaVencEnc() {
        return fechaVencEnc;
    }

    public void setFechaVencEnc(String fechaVencEnc) {
        this.fechaVencEnc = fechaVencEnc;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
