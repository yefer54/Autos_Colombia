package models;

/**
 * Roles disponibles para los usuarios del sistema.
 * ADMIN    → acceso total (gestión de usuarios, celdas, asignaciones, mensualidades, pagos y métodos de pago).
 * OPERARIO → acceso operativo (asignaciones, mensualidades, pagos, consultas).
 * CLIENTE  → acceso cliente (consulta de su propio historial, pagos pendientes y comprobantes).
 */
public enum Rol {
    ADMIN,
    OPERARIO,
    CLIENTE;

    /**
     * Convierte un String al enum correspondiente.
     * Si el valor es inválido o nulo devuelve OPERARIO por defecto.
     */
    public static Rol fromString(String s) {
        if (s == null) return OPERARIO;
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OPERARIO;
        }
    }
}
