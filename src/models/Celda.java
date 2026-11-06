package models;

import java.util.Map;

/**
 * Representa una celda (espacio) del parqueadero.
 *
 * <ul>
 *   <li>{@code numero}       – número secuencial interno (autogenerado).</li>
 *   <li>{@code codigo}       – código único definido por el usuario (ej. "A1", "B2").</li>
 *   <li>{@code habilitada}   – indica si la celda está en servicio.</li>
 *   <li>{@code disponible}   – indica si no hay vehículo asignado.</li>
 *   <li>{@code placaVehiculo}– placa del vehículo actualmente asignado.</li>
 * </ul>
 */
public class Celda {

    private int     numero;
    private String  codigo;
    private boolean habilitada;
    private boolean disponible;
    private String  placaVehiculo;

    public Celda(int numero, String codigo, boolean disponible, boolean habilitada, String placaVehiculo) {
        this.numero        = numero;
        this.codigo        = (codigo != null && !codigo.isBlank()) ? codigo.trim() : ("C" + numero);
        this.habilitada    = habilitada;
        this.disponible    = disponible;
        this.placaVehiculo = placaVehiculo != null ? placaVehiculo.trim() : "";
    }

    public int     getNumero()          { return numero; }
    public String  getCodigo()          { return codigo; }
    public boolean isHabilitada()       { return habilitada; }
    public boolean isDisponible()       { return disponible; }
    public String  getPlacaVehiculo()   { return placaVehiculo; }

    public void setNumero(int v)             { this.numero        = v; }
    public void setCodigo(String v)          { this.codigo        = v != null ? v.trim() : ""; }
    public void setHabilitada(boolean v)     { this.habilitada    = v; }
    public void setDisponible(boolean v)     { this.disponible    = v; }
    public void setPlacaVehiculo(String v)   { this.placaVehiculo = v != null ? v.trim() : ""; }

    public String toJsonString() {
        String placa = placaVehiculo != null ? placaVehiculo.replace("\"", "\\\"") : "";
        String cod   = codigo        != null ? codigo.replace("\"", "\\\"")        : "";
        return "{"
             + "\"numero\": \""        + numero     + "\", "
             + "\"codigo\": \""        + cod        + "\", "
             + "\"disponible\": \""    + disponible + "\", "
             + "\"habilitada\": \""    + habilitada + "\", "
             + "\"placaVehiculo\": \"" + placa      + "\""
             + "}";
    }

    /**
     * Parsea una celda desde un mapa JSON.
     * Los campos {@code codigo} y {@code habilitada} son retrocompatibles:
     * si no existen en el JSON se usan valores por defecto ("C{numero}" y true).
     */
    public static Celda fromJsonObject(Map<String, String> map) {
        try {
            int     numero     = Integer.parseInt(map.getOrDefault("numero",     "0").trim());
            String  codigo     = map.getOrDefault("codigo",        "").trim();
            boolean disponible = Boolean.parseBoolean(map.getOrDefault("disponible", "true").trim());
            boolean habilitada = Boolean.parseBoolean(map.getOrDefault("habilitada", "true").trim());
            String  placa      = map.getOrDefault("placaVehiculo", "").trim();
            if (codigo.isEmpty()) codigo = "C" + numero; // retrocompatibilidad
            return new Celda(numero, codigo, disponible, habilitada, placa);
        } catch (Exception e) {
            System.err.println("[ADVERTENCIA] No se pudo parsear celda: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        String estado;
        if (!habilitada)      estado = "DESHABILITADA";
        else if (disponible)  estado = "LIBRE        ";
        else                  estado = "OCUPADA      ";

        String placa = (!disponible && placaVehiculo != null && !placaVehiculo.isEmpty())
                       ? placaVehiculo : "---";
        return String.format("  %-6s │  %-13s │  %s", codigo, estado, placa);
    }
}
