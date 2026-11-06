package models;

import java.util.Map;

/**
 * Representa un usuario del sistema (personal del parqueadero).
 * Se identifica por documento de identidad y accede mediante correo + contraseña.
 * Puede ser desactivado sin ser eliminado del sistema.
 */
public class Usuario {

    private String  documento;
    private String  nombre;
    private String  correo;
    private String  contrasena;
    private String  telefono;
    private Rol     rol;
    private boolean activo;

    public Usuario(String documento, String nombre, String correo, String contrasena,
                   String telefono, Rol rol, boolean activo) {
        this.documento  = safe(documento);
        this.nombre     = safe(nombre);
        this.correo     = correo != null ? correo.trim().toLowerCase() : "";
        this.contrasena = safe(contrasena);
        this.telefono   = safe(telefono);
        this.rol        = rol != null ? rol : Rol.OPERARIO;
        this.activo     = activo;
    }

    public String  getDocumento()  { return documento; }
    public String  getNombre()     { return nombre; }
    public String  getCorreo()     { return correo; }
    public String  getContrasena() { return contrasena; }
    public String  getTelefono()   { return telefono; }
    public Rol     getRol()        { return rol; }
    public boolean isActivo()      { return activo; }

    public void setDocumento(String v)  { this.documento  = safe(v); }
    public void setNombre(String v)     { this.nombre     = safe(v); }
    public void setCorreo(String v)     { this.correo     = v != null ? v.trim().toLowerCase() : ""; }
    public void setContrasena(String v) { this.contrasena = safe(v); }
    public void setTelefono(String v)   { this.telefono   = safe(v); }
    public void setRol(Rol v)           { this.rol        = v != null ? v : Rol.OPERARIO; }
    public void setActivo(boolean v)    { this.activo     = v; }

    public String toJsonString() {
        return "{"
             + jf("documento",  documento)              + ", "
             + jf("nombre",     nombre)                 + ", "
             + jf("correo",     correo)                 + ", "
             + jf("contrasena", contrasena)             + ", "
             + jf("telefono",   telefono)               + ", "
             + jf("rol",        rol.name())             + ", "
             + jf("activo",     String.valueOf(activo))
             + "}";
    }

    /**
     * Parsea un usuario desde un mapa JSON.
     * Retorna null si el documento o correo están vacíos (registro inválido o formato antiguo).
     */
    public static Usuario fromJsonObject(Map<String, String> map) {
        try {
            String  documento  = map.getOrDefault("documento",  "").trim();
            String  nombre     = map.getOrDefault("nombre",     "").trim();
            String  correo     = map.getOrDefault("correo",     "").trim();
            String  contrasena = map.getOrDefault("contrasena", "").trim();
            String  telefono   = map.getOrDefault("telefono",   "").trim();
            Rol     rol        = Rol.fromString(map.getOrDefault("rol", "OPERARIO"));
            boolean activo     = Boolean.parseBoolean(map.getOrDefault("activo", "true").trim());
            // Skip entries with old format (e.g. placa-based records)
            if (documento.isEmpty() || correo.isEmpty()) return null;
            return new Usuario(documento, nombre, correo, contrasena, telefono, rol, activo);
        } catch (Exception e) {
            System.err.println("No se pudo parsear usuario: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "┌─────────────────────────────────────────┐\n"
             + "│  Documento : " + pr(documento,            27) + "│\n"
             + "│  Nombre    : " + pr(nombre,               27) + "│\n"
             + "│  Correo    : " + pr(correo,               27) + "│\n"
             + "│  Teléfono  : " + pr(telefono,             27) + "│\n"
             + "│  Rol       : " + pr(rol.name(),           27) + "│\n"
             + "│  Estado    : " + pr(activo ? "ACTIVO" : "INACTIVO", 27) + "│\n"
             + "└─────────────────────────────────────────┘";
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
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
