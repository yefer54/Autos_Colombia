package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


public class Usuario {

    public static final DateTimeFormatter FMT_ALMACEN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static final DateTimeFormatter FMT_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String placa;

    private String nombrePropietario;

    private String telefono;

    private int numeroCelda;

    private LocalDateTime fechaIngreso;

    private LocalDateTime fechaVencimiento;

    private boolean activo;


    public Usuario(String placa, String nombrePropietario, String telefono, int numeroCelda,LocalDateTime fechaIngreso, LocalDateTime fechaVencimiento, boolean activo) {
        this.placa              = (placa != null) ? placa.toUpperCase().trim() : "";
        this.nombrePropietario  = (nombrePropietario != null) ? nombrePropietario.trim() : "";
        this.telefono           = (telefono != null) ? telefono.trim() : "";
        this.numeroCelda        = numeroCelda;
        this.fechaIngreso       = fechaIngreso;
        this.fechaVencimiento   = fechaVencimiento;
        this.activo             = activo;
    }

    public String getPlaca()             { return placa; }

    public String getNombrePropietario() { return nombrePropietario; }

    public String getTelefono()          { return telefono; }

    public int getNumeroCelda()          { return numeroCelda; }

    public LocalDateTime getFechaIngreso()      { return fechaIngreso; }

    public LocalDateTime getFechaVencimiento()  { return fechaVencimiento; }

    public boolean isActivo() { return activo; }

    public void setPlaca(String placa) {
        this.placa = (placa != null) ? placa.toUpperCase().trim() : "";
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = (nombrePropietario != null) ? nombrePropietario.trim() : "";
    }

    public void setTelefono(String telefono) {
        this.telefono = (telefono != null) ? telefono.trim() : "";
    }

    public void setNumeroCelda(int numeroCelda) { this.numeroCelda = numeroCelda; }

    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isVigente() {
        if (fechaVencimiento == null) return false;
        return !fechaVencimiento.isBefore(LocalDateTime.now());
    }

    public String toJsonString() {
        String ingreso     = (fechaIngreso     != null) ? fechaIngreso.format(FMT_ALMACEN)     : "";
        String vencimiento = (fechaVencimiento != null) ? fechaVencimiento.format(FMT_ALMACEN) : "";

        return "{"
             + jsonField("placa",             placa)             + ", "
             + jsonField("nombrePropietario", nombrePropietario) + ", "
             + jsonField("telefono",          telefono)          + ", "
             + jsonField("numeroCelda",       String.valueOf(numeroCelda)) + ", "
             + jsonField("fechaIngreso",      ingreso)           + ", "
             + jsonField("fechaVencimiento",  vencimiento)       + ", "
             + jsonField("activo",            String.valueOf(activo))
             + "}";
    }


    public static Usuario fromJsonObject(Map<String, String> map) {
        try {
            String placa            = map.getOrDefault("placa",            "").trim();
            String nombre           = map.getOrDefault("nombrePropietario","").trim();
            String telefono         = map.getOrDefault("telefono",         "").trim();
            int    numeroCelda      = Integer.parseInt(map.getOrDefault("numeroCelda", "0").trim());
            String ingStr           = map.getOrDefault("fechaIngreso",     "").trim();
            String vencStr          = map.getOrDefault("fechaVencimiento", "").trim();
            boolean activo          = Boolean.parseBoolean(map.getOrDefault("activo", "true").trim());

            LocalDateTime ingreso     = ingStr.isEmpty()   ? LocalDateTime.now() : LocalDateTime.parse(ingStr,   FMT_ALMACEN);
            LocalDateTime vencimiento = vencStr.isEmpty()  ? ingreso.plusDays(30) : LocalDateTime.parse(vencStr, FMT_ALMACEN);

            return new Usuario(placa, nombre, telefono, numeroCelda, ingreso, vencimiento, activo);
        } catch (Exception e) {
            System.err.println("No se pudo parsear usuario desde JSON: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        String ingreso     = (fechaIngreso     != null) ? fechaIngreso.format(FMT_DISPLAY)     : "N/A";
        String vencimiento = (fechaVencimiento != null) ? fechaVencimiento.format(FMT_DISPLAY) : "N/A";
        String estado      = isVigente() ? "ACTIVO" : "INACTIVO";

        return "┌─────────────────────────────────────────┐\n"
             + "│  Placa         : " + padRight(placa, 23)             + "│\n"
             + "│  Propietario   : " + padRight(nombrePropietario, 23) + "│\n"
             + "│  Teléfono      : " + padRight(telefono, 23)          + "│\n"
             + "│  Celda         : " + padRight(String.valueOf(numeroCelda), 23) + "│\n"
             + "│  Ingreso       : " + padRight(ingreso, 23)           + "│\n"
             + "│  Vencimiento   : " + padRight(vencimiento, 23)       + "│\n"
             + "│  Estado        : " + padRight(estado, 23)            + "│\n"
             + "└─────────────────────────────────────────┘";
    }

    private String jsonField(String clave, String valor) {
        String valorEscapado = (valor != null) ? valor.replace("\\", "\\\\").replace("\"", "\\\"") : "";
        return "\"" + clave + "\": \"" + valorEscapado + "\"";
    }

    private String padRight(String texto, int longitud) {
        if (texto == null) texto = "";
        if (texto.length() >= longitud) return texto.substring(0, longitud);
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < longitud) sb.append(' ');
        return sb.toString();
    }
}
