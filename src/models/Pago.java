package models;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Representa una transacción de pago de servicio de parqueadero en Autos Colombia.
 */
public class Pago {

    public static final DateTimeFormatter FMT_ALMACEN =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static final DateTimeFormatter FMT_DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    private String        idTransaccion;
    private String        placaVehiculo;
    private String        nombreCliente;
    private String        documentoCliente;
    private int           numeroCelda;
    private LocalDateTime fechaPago;
    private String        periodo;
    private double        valor;
    private String        metodoPagoId;
    private String        metodoPagoNombre;
    private String        estado;           // APROBADO, PENDIENTE, ANULADO
    private String        registradoPor;
    private String        observaciones;

    public Pago(String idTransaccion, String placaVehiculo, String nombreCliente,
                String documentoCliente, int numeroCelda, LocalDateTime fechaPago,
                String periodo, double valor, String metodoPagoId, String metodoPagoNombre,
                String estado, String registradoPor, String observaciones) {
        this.idTransaccion    = safe(idTransaccion);
        this.placaVehiculo    = placaVehiculo != null ? placaVehiculo.toUpperCase().trim() : "";
        this.nombreCliente    = safe(nombreCliente);
        this.documentoCliente = safe(documentoCliente);
        this.numeroCelda      = numeroCelda;
        this.fechaPago        = fechaPago != null ? fechaPago : LocalDateTime.now();
        this.periodo          = safe(periodo);
        this.valor            = valor;
        this.metodoPagoId     = safe(metodoPagoId);
        this.metodoPagoNombre = safe(metodoPagoNombre);
        this.estado           = (estado != null && !estado.isBlank()) ? estado.toUpperCase().trim() : "APROBADO";
        this.registradoPor    = safe(registradoPor);
        this.observaciones    = safe(observaciones);
    }

    public String        getIdTransaccion()    { return idTransaccion; }
    public String        getPlacaVehiculo()    { return placaVehiculo; }
    public String        getNombreCliente()     { return nombreCliente; }
    public String        getDocumentoCliente()  { return documentoCliente; }
    public int           getNumeroCelda()       { return numeroCelda; }
    public LocalDateTime getFechaPago()         { return fechaPago; }
    public String        getPeriodo()           { return periodo; }
    public double        getValor()             { return valor; }
    public String        getMetodoPagoId()      { return metodoPagoId; }
    public String        getMetodoPagoNombre()  { return metodoPagoNombre; }
    public String        getEstado()            { return estado; }
    public String        getRegistradoPor()     { return registradoPor; }
    public String        getObservaciones()     { return observaciones; }

    public void setIdTransaccion(String idTransaccion)       { this.idTransaccion = safe(idTransaccion); }
    public void setPlacaVehiculo(String placaVehiculo)       { this.placaVehiculo = placaVehiculo != null ? placaVehiculo.toUpperCase().trim() : ""; }
    public void setNombreCliente(String nombreCliente)       { this.nombreCliente = safe(nombreCliente); }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = safe(documentoCliente); }
    public void setNumeroCelda(int numeroCelda)              { this.numeroCelda = numeroCelda; }
    public void setFechaPago(LocalDateTime fechaPago)        { this.fechaPago = fechaPago; }
    public void setPeriodo(String periodo)                   { this.periodo = safe(periodo); }
    public void setValor(double valor)                       { this.valor = valor; }
    public void setMetodoPagoId(String metodoPagoId)         { this.metodoPagoId = safe(metodoPagoId); }
    public void setMetodoPagoNombre(String metodoPagoNombre) { this.metodoPagoNombre = safe(metodoPagoNombre); }
    public void setEstado(String estado)                     { this.estado = (estado != null) ? estado.toUpperCase().trim() : "APROBADO"; }
    public void setRegistradoPor(String registradoPor)       { this.registradoPor = safe(registradoPor); }
    public void setObservaciones(String observaciones)       { this.observaciones = safe(observaciones); }

    public String getValorFormateado() {
        return MONEDA.format(valor);
    }

    public String toJsonString() {
        String f = fechaPago != null ? fechaPago.format(FMT_ALMACEN) : "";
        return "{"
             + jf("idTransaccion",    idTransaccion)                   + ", "
             + jf("placaVehiculo",    placaVehiculo)                   + ", "
             + jf("nombreCliente",    nombreCliente)                   + ", "
             + jf("documentoCliente", documentoCliente)                + ", "
             + jf("numeroCelda",      String.valueOf(numeroCelda))     + ", "
             + jf("fechaPago",        f)                               + ", "
             + jf("periodo",          periodo)                         + ", "
             + jf("valor",            String.format(Locale.US, "%.2f", valor)) + ", "
             + jf("metodoPagoId",     metodoPagoId)                    + ", "
             + jf("metodoPagoNombre", metodoPagoNombre)                + ", "
             + jf("estado",           estado)                          + ", "
             + jf("registradoPor",    registradoPor)                   + ", "
             + jf("observaciones",    observaciones)
             + "}";
    }

    public static Pago fromJsonObject(Map<String, String> map) {
        try {
            String idTransaccion    = map.getOrDefault("idTransaccion",    "").trim();
            String placaVehiculo    = map.getOrDefault("placaVehiculo",    "").trim();
            String nombreCliente    = map.getOrDefault("nombreCliente",    "").trim();
            String documentoCliente = map.getOrDefault("documentoCliente", "").trim();
            int    numeroCelda      = Integer.parseInt(map.getOrDefault("numeroCelda", "0").trim());
            String fStr             = map.getOrDefault("fechaPago",        "").trim();
            LocalDateTime fechaPago = fStr.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(fStr, FMT_ALMACEN);
            String periodo          = map.getOrDefault("periodo",          "").trim();
            double valor            = Double.parseDouble(map.getOrDefault("valor", "0").trim());
            String metodoPagoId     = map.getOrDefault("metodoPagoId",     "").trim();
            String metodoPagoNombre = map.getOrDefault("metodoPagoNombre", "").trim();
            String estado           = map.getOrDefault("estado",           "APROBADO").trim();
            String registradoPor    = map.getOrDefault("registradoPor",    "").trim();
            String observaciones    = map.getOrDefault("observaciones",    "").trim();

            if (idTransaccion.isEmpty()) return null;
            return new Pago(idTransaccion, placaVehiculo, nombreCliente, documentoCliente,
                            numeroCelda, fechaPago, periodo, valor, metodoPagoId,
                            metodoPagoNombre, estado, registradoPor, observaciones);
        } catch (Exception e) {
            System.err.println("No se pudo parsear pago: " + e.getMessage());
            return null;
        }
    }

    /**
     * Genera un comprobante de pago formateado tipo recibo oficial.
     */
    public String generarComprobante() {
        String f = fechaPago != null ? fechaPago.format(FMT_DISPLAY) : "N/A";
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                PARQUEADERO AUTOS COLOMBIA                    ║\n");
        sb.append("║                   COMPROBANTE DE PAGO                        ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  N° Transacción : %-42s ║%n", idTransaccion));
        sb.append(String.format("║  Fecha y Hora   : %-42s ║%n", f));
        sb.append(String.format("║  Estado         : %-42s ║%n", estado));
        sb.append("╟──────────────────────────────────────────────────────────────╢\n");
        sb.append(String.format("║  Cliente        : %-42s ║%n", truncar(nombreCliente, 42)));
        if (!documentoCliente.isEmpty()) {
            sb.append(String.format("║  Documento      : %-42s ║%n", truncar(documentoCliente, 42)));
        }
        sb.append(String.format("║  Placa Vehículo : %-42s ║%n", placaVehiculo));
        sb.append(String.format("║  Celda Asignada : N° %-39d ║%n", numeroCelda));
        sb.append(String.format("║  Concepto       : %-42s ║%n", truncar("Mensualidad (" + periodo + ")", 42)));
        sb.append(String.format("║  Método de Pago : %-42s ║%n", truncar(metodoPagoNombre, 42)));
        sb.append(String.format("║  Valor Pagado   : %-42s ║%n", getValorFormateado()));
        if (!observaciones.isEmpty()) {
            sb.append(String.format("║  Observaciones  : %-42s ║%n", truncar(observaciones, 42)));
        }
        if (!registradoPor.isEmpty()) {
            sb.append(String.format("║  Atendido por   : %-42s ║%n", truncar(registradoPor, 42)));
        }
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append("║       ¡Gracias por su pago! Conserve este comprobante.       ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        String f = fechaPago != null ? fechaPago.format(FMT_DISPLAY) : "N/A";
        return String.format("  %-10s │ %-16s │ %-8s │ %-15s │ %-18s │ %-10s",
                idTransaccion, f, placaVehiculo, getValorFormateado(),
                truncar(metodoPagoNombre, 18), estado);
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    private static String truncar(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n - 3) + "..." : s;
    }

    private String jf(String clave, String valor) {
        String e = valor != null ? valor.replace("\\", "\\\\").replace("\"", "\\\"") : "";
        return "\"" + clave + "\": \"" + e + "\"";
    }
}
