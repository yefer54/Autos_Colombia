package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Representa un cliente del parqueadero: vehículo con mensualidad activa.
 * Equivale al antiguo modelo Usuario, renombrado para distinguirlo del
 * personal del sistema ({@link Usuario}).
 */
public class Cliente {

    public static final DateTimeFormatter FMT_ALMACEN =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static final DateTimeFormatter FMT_DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String        placa;
    private String        nombrePropietario;
    private String        documento;
    private String        telefono;
    private int           numeroCelda;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaVencimiento;
    private boolean       activo;

    public Cliente(String placa, String nombrePropietario, String telefono,
                   int numeroCelda, LocalDateTime fechaIngreso,
                   LocalDateTime fechaVencimiento, boolean activo) {
        this(placa, nombrePropietario, "", telefono, numeroCelda, fechaIngreso, fechaVencimiento, activo);
    }

    public Cliente(String placa, String nombrePropietario, String documento,
                   String telefono, int numeroCelda, LocalDateTime fechaIngreso,
                   LocalDateTime fechaVencimiento, boolean activo) {
        this.placa             = placa             != null ? placa.toUpperCase().trim() : "";
        this.nombrePropietario = nombrePropietario != null ? nombrePropietario.trim()   : "";
        this.documento         = documento         != null ? documento.trim()           : "";
        this.telefono          = telefono          != null ? telefono.trim()            : "";
        this.numeroCelda       = numeroCelda;
        this.fechaIngreso      = fechaIngreso;
        this.fechaVencimiento  = fechaVencimiento;
        this.activo            = activo;
    }

    public String        getPlaca()             { return placa; }
    public String        getNombrePropietario()  { return nombrePropietario; }
    public String        getDocumento()          { return documento; }
    public String        getTelefono()           { return telefono; }
    public int           getNumeroCelda()        { return numeroCelda; }
    public LocalDateTime getFechaIngreso()       { return fechaIngreso; }
    public LocalDateTime getFechaVencimiento()   { return fechaVencimiento; }
    public boolean       isActivo()              { return activo; }

    public void setPlaca(String v)                   { this.placa             = v != null ? v.toUpperCase().trim() : ""; }
    public void setNombrePropietario(String v)       { this.nombrePropietario = v != null ? v.trim() : ""; }
    public void setDocumento(String v)               { this.documento         = v != null ? v.trim() : ""; }
    public void setTelefono(String v)                { this.telefono          = v != null ? v.trim() : ""; }
    public void setNumeroCelda(int v)                { this.numeroCelda       = v; }
    public void setFechaIngreso(LocalDateTime v)     { this.fechaIngreso      = v; }
    public void setFechaVencimiento(LocalDateTime v) { this.fechaVencimiento  = v; }
    public void setActivo(boolean v)                 { this.activo            = v; }

    /** Retorna true si la mensualidad aún no ha vencido. */
    public boolean isVigente() {
        if (fechaVencimiento == null) return false;
        return !fechaVencimiento.isBefore(LocalDateTime.now());
    }

    public String toJsonString() {
        String ing = fechaIngreso     != null ? fechaIngreso.format(FMT_ALMACEN)     : "";
        String ven = fechaVencimiento != null ? fechaVencimiento.format(FMT_ALMACEN) : "";
        return "{"
             + jf("placa",             placa)                       + ", "
             + jf("nombrePropietario", nombrePropietario)           + ", "
             + jf("documento",         documento)                   + ", "
             + jf("telefono",          telefono)                    + ", "
             + jf("numeroCelda",       String.valueOf(numeroCelda)) + ", "
             + jf("fechaIngreso",      ing)                         + ", "
             + jf("fechaVencimiento",  ven)                         + ", "
             + jf("activo",            String.valueOf(activo))
             + "}";
    }

    /**
     * Parsea un cliente desde un mapa JSON.
     * Compatible con el formato antiguo de usuarios.json (campo "placa").
     */
    public static Cliente fromJsonObject(Map<String, String> map) {
        try {
            String placa  = map.getOrDefault("placa",             "").trim();
            String nombre = map.getOrDefault("nombrePropietario", "").trim();
            String doc    = map.getOrDefault("documento",         "").trim();
            String tel    = map.getOrDefault("telefono",          "").trim();
            int    celda  = Integer.parseInt(map.getOrDefault("numeroCelda", "0").trim());
            String ingStr = map.getOrDefault("fechaIngreso",      "").trim();
            String venStr = map.getOrDefault("fechaVencimiento",  "").trim();
            boolean activo = Boolean.parseBoolean(map.getOrDefault("activo", "true").trim());
            if (placa.isEmpty()) return null;
            LocalDateTime ing = ingStr.isEmpty() ? LocalDateTime.now()       : LocalDateTime.parse(ingStr, FMT_ALMACEN);
            LocalDateTime ven = venStr.isEmpty() ? ing.plusDays(30)          : LocalDateTime.parse(venStr, FMT_ALMACEN);
            return new Cliente(placa, nombre, doc, tel, celda, ing, ven, activo);
        } catch (Exception e) {
            System.err.println("No se pudo parsear cliente: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        String ing = fechaIngreso     != null ? fechaIngreso.format(FMT_DISPLAY)     : "N/A";
        String ven = fechaVencimiento != null ? fechaVencimiento.format(FMT_DISPLAY) : "N/A";
        String est = isVigente() ? "ACTIVO" : "INACTIVO";
        return "┌─────────────────────────────────────────┐\n"
             + "│  Placa         : " + pr(placa,             23) + "│\n"
             + "│  Propietario   : " + pr(nombrePropietario, 23) + "│\n"
             + (documento.isEmpty() ? "" : ("│  Documento     : " + pr(documento, 23) + "│\n"))
             + "│  Teléfono      : " + pr(telefono,          23) + "│\n"
             + "│  Celda N°      : " + pr(String.valueOf(numeroCelda), 23) + "│\n"
             + "│  Ingreso       : " + pr(ing,               23) + "│\n"
             + "│  Vencimiento   : " + pr(ven,               23) + "│\n"
             + "│  Estado        : " + pr(est,               23) + "│\n"
             + "└─────────────────────────────────────────┘";
    }

    private String jf(String clave, String valor) {
        String e = valor != null ? valor.replace("\\", "\\\\").replace("\"", "\\\"") : "";
        return "\"" + clave + "\": \"" + e + "\"";
    }

    private String pr(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }
}
