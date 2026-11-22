package models;

/**
 * Resultado de una operación de registro o validación de pago.
 * Proporciona información detallada sobre el estado de la transacción.
 */
public class ResultadoPago {

    private final boolean exito;
    private final String  mensaje;
    private final Pago    pago;
    private final Pago    pagoExistente; // Utilizado cuando se detecta un pago duplicado

    public ResultadoPago(boolean exito, String mensaje, Pago pago, Pago pagoExistente) {
        this.exito         = exito;
        this.mensaje       = mensaje != null ? mensaje : "";
        this.pago          = pago;
        this.pagoExistente = pagoExistente;
    }

    public static ResultadoPago ok(String mensaje, Pago pago) {
        return new ResultadoPago(true, mensaje, pago, null);
    }

    public static ResultadoPago fallo(String mensaje) {
        return new ResultadoPago(false, mensaje, null, null);
    }

    public static ResultadoPago duplicado(String mensaje, Pago pagoExistente) {
        return new ResultadoPago(false, mensaje, null, pagoExistente);
    }

    public boolean isExito()         { return exito; }
    public String  getMensaje()       { return mensaje; }
    public Pago    getPago()          { return pago; }
    public Pago    getPagoExistente() { return pagoExistente; }
    public boolean esDuplicado()      { return pagoExistente != null; }
}
