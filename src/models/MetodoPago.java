package models;

import java.util.Map;

/**
 * Representa un método de pago aceptado por el parqueadero Autos Colombia.
 * Permite ser habilitado o deshabilitado por el administrador.
 */
public class MetodoPago {

    private String  id;
    private String  nombre;
    private String  descripcion;
    private boolean habilitado;

    public MetodoPago(String id, String nombre, String descripcion, boolean habilitado) {
        this.id          = safe(id);
        this.nombre      = safe(nombre);
        this.descripcion = safe(descripcion);
        this.habilitado  = habilitado;
    }

    public String  getId()          { return id; }
    public String  getNombre()      { return nombre; }
    public String  getDescripcion() { return descripcion; }
    public boolean isHabilitado()   { return habilitado; }

    public void setId(String id)                   { this.id          = safe(id); }
    public void setNombre(String nombre)           { this.nombre      = safe(nombre); }
    public void setDescripcion(String descripcion) { this.descripcion = safe(descripcion); }
    public void setHabilitado(boolean habilitado)  { this.habilitado  = habilitado; }

    public String toJsonString() {
        return "{"
             + jf("id",          id)                    + ", "
             + jf("nombre",      nombre)                + ", "
             + jf("descripcion", descripcion)           + ", "
             + jf("habilitado",  String.valueOf(habilitado))
             + "}";
    }

    public static MetodoPago fromJsonObject(Map<String, String> map) {
        try {
            String  id          = map.getOrDefault("id",          "").trim();
            String  nombre      = map.getOrDefault("nombre",      "").trim();
            String  descripcion = map.getOrDefault("descripcion", "").trim();
            boolean habilitado  = Boolean.parseBoolean(map.getOrDefault("habilitado", "true").trim());
            if (id.isEmpty() || nombre.isEmpty()) return null;
            return new MetodoPago(id, nombre, descripcion, habilitado);
        } catch (Exception e) {
            System.err.println("No se pudo parsear método de pago: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        String estado = habilitado ? "HABILITADO   " : "DESHABILITADO";
        return String.format("  %-6s │  %-28s │  %-14s │  %s", id, nombre, estado, descripcion);
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    private String jf(String clave, String valor) {
        String e = valor != null ? valor.replace("\\", "\\\\").replace("\"", "\\\"") : "";
        return "\"" + clave + "\": \"" + e + "\"";
    }
}
