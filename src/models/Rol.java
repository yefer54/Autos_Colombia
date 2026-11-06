package models;

/**
 * Roles disponibles para los usuarios del sistema.
 * ADMIN  → acceso total (gestión de usuarios, celdas, asignaciones, mensualidades).
 * OPERARIO → acceso operativo (asignaciones, mensualidades, consultas).
 */
public enum Rol {
    ADMIN,
    OPERARIO;

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
