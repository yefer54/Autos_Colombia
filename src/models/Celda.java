package models;

import java.util.Map;


public class Celda {

    private int numero;

    private boolean disponible;

    private String placaVehiculo;

    public Celda(int numero, boolean disponible, String placaVehiculo) {
        this.numero        = numero;
        this.disponible    = disponible;
        this.placaVehiculo = (placaVehiculo != null) ? placaVehiculo.trim() : "";
    }


    public int getNumero() { return numero; }

    public boolean isDisponible() { return disponible; }

    public String getPlacaVehiculo() { return placaVehiculo; }

    public void setNumero(int numero) { this.numero = numero; }

    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = (placaVehiculo != null) ? placaVehiculo.trim() : "";
    }


    public String toJsonString() {
        String placa = (placaVehiculo != null) ? placaVehiculo.replace("\"", "\\\"") : "";
        return "{"
             + "\"numero\": \"" + numero + "\", "
             + "\"disponible\": \"" + disponible + "\", "
             + "\"placaVehiculo\": \"" + placa + "\""
             + "}";
    }

    public static Celda fromJsonObject(Map<String, String> map) {
        try {
            int     numero        = Integer.parseInt(map.getOrDefault("numero", "0").trim());
            boolean disponible    = Boolean.parseBoolean(map.getOrDefault("disponible", "true").trim());
            String  placaVehiculo = map.getOrDefault("placaVehiculo", "").trim();
            return new Celda(numero, disponible, placaVehiculo);
        } catch (Exception e) {
            System.err.println("[ADVERTENCIA] No se pudo parsear celda desde JSON: " + e.getMessage());
            return null;
        }
    }


    @Override
    public String toString() {
        String estado = disponible ? "LIBRE     " : "OCUPADA   ";
        String placa  = (disponible || placaVehiculo == null || placaVehiculo.isEmpty())
                        ? "---"
                        : placaVehiculo;
        return String.format("  Celda %2d  │  %s  │  %s", numero, estado, placa);
    }
}
