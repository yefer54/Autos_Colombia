package models;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Representa una cuenta o mensualidad pendiente de pago en el sistema.
 */
public class PagoPendiente {

    public static final DateTimeFormatter FMT_DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    private String        placa;
    private String        nombreCliente;
    private String        telefono;
    private int           numeroCelda;
    private LocalDateTime fechaVencimiento;
    private double        valorPendiente;
    private long          diasRetraso;
    private String        estado; // VENCIDO, POR_VENCER, PENDIENTE_INICIAL

    public PagoPendiente(String placa, String nombreCliente, String telefono,
                         int numeroCelda, LocalDateTime fechaVencimiento,
                         double valorPendiente) {
        this.placa            = placa != null ? placa.toUpperCase().trim() : "";
        this.nombreCliente    = nombreCliente != null ? nombreCliente.trim() : "";
        this.telefono         = telefono != null ? telefono.trim() : "";
        this.numeroCelda      = numeroCelda;
        this.fechaVencimiento = fechaVencimiento;
        this.valorPendiente   = valorPendiente;

        LocalDateTime ahora = LocalDateTime.now();
        if (fechaVencimiento == null) {
            this.diasRetraso = 0;
            this.estado      = "PENDIENTE_INICIAL";
        } else if (fechaVencimiento.isBefore(ahora)) {
            this.diasRetraso = ChronoUnit.DAYS.between(fechaVencimiento, ahora);
            this.estado      = "VENCIDO";
        } else {
            long diasRestantes = ChronoUnit.DAYS.between(ahora, fechaVencimiento);
            this.diasRetraso = 0;
            this.estado      = diasRestantes <= 5 ? "POR_VENCER (" + diasRestantes + "d)" : "AL_DIA";
        }
    }

    public String        getPlaca()            { return placa; }
    public String        getNombreCliente()     { return nombreCliente; }
    public String        getTelefono()          { return telefono; }
    public int           getNumeroCelda()       { return numeroCelda; }
    public LocalDateTime getFechaVencimiento()  { return fechaVencimiento; }
    public double        getValorPendiente()    { return valorPendiente; }
    public long          getDiasRetraso()       { return diasRetraso; }
    public String        getEstado()            { return estado; }

    public String getValorFormateado() {
        return MONEDA.format(valorPendiente);
    }

    @Override
    public String toString() {
        String f = fechaVencimiento != null ? fechaVencimiento.format(FMT_DISPLAY) : "SIN VENCIMIENTO";
        String dias = diasRetraso > 0 ? (diasRetraso + " días") : "---";
        return String.format("  %-8s │ %-20s │ Celda %-2d │ %-12s │ %-10s │ %-14s │ %-15s",
                placa, truncar(nombreCliente, 20), numeroCelda, f, dias, getValorFormateado(), estado);
    }

    private static String truncar(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n - 3) + "..." : s;
    }
}
