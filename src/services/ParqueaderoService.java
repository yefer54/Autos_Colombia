package services;

import models.Celda;
import models.Cliente;
import models.MetodoPago;
import models.Pago;
import models.PagoPendiente;
import models.ResultadoPago;
import models.Rol;
import models.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class ParqueaderoService {

    public static final int    TOTAL_CELDAS_INICIAL = 10;
    public static final String RUTA_USUARIOS        = "data/usuarios.json";
    public static final String RUTA_CLIENTES        = "data/clientes.json";
    public static final String RUTA_CELDAS          = "data/celdas.json";
    public static final String RUTA_METODOS_PAGO    = "data/metodos_pago.json";
    public static final String RUTA_PAGOS           = "data/pagos.json";

    public static final double TARIFA_MENSUAL_BASE  = 120000.0;

    private List<Usuario>    usuarios;
    private List<Cliente>    clientes;
    private List<Celda>      celdas;
    private List<MetodoPago> metodosPago;
    private List<Pago>       pagos;

    private int siguienteNumeroCelda;
    private int siguienteIdPago;

    public ParqueaderoService() {
        usuarios = DataService.cargarUsuarios(RUTA_USUARIOS);
        if (usuarios == null) usuarios = new ArrayList<>();

        clientes = DataService.cargarClientes(RUTA_CLIENTES);

        if (clientes == null || clientes.isEmpty()) {
            clientes = DataService.cargarClientes(RUTA_USUARIOS);
            if (clientes != null && !clientes.isEmpty()) {
                DataService.guardarClientes(clientes, RUTA_CLIENTES);
                System.out.println("  [INFO] Clientes migrados al nuevo archivo clientes.json");
            }
        }
        if (clientes == null) clientes = new ArrayList<>();

        if (usuarios.isEmpty()) {
            usuarios.add(adminPorDefecto());
            DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
            System.out.println("  [INFO] Usuario administrador por defecto creado.");
            System.out.println("         Correo: admin@autoscolombia.com  |  Contraseña: admin123");
        }

        celdas = DataService.cargarCeldas(RUTA_CELDAS);
        if (celdas == null || celdas.isEmpty()) {
            celdas = inicializarCeldas();
            DataService.guardarCeldas(celdas, RUTA_CELDAS);
        }

        siguienteNumeroCelda = celdas.stream()
                                     .mapToInt(Celda::getNumero)
                                     .max().orElse(0) + 1;

        metodosPago = DataService.cargarMetodosPago(RUTA_METODOS_PAGO);
        if (metodosPago == null || metodosPago.isEmpty()) {
            metodosPago = inicializarMetodosPago();
            DataService.guardarMetodosPago(metodosPago, RUTA_METODOS_PAGO);
        }

        pagos = DataService.cargarPagos(RUTA_PAGOS);
        if (pagos == null) pagos = new ArrayList<>();

        siguienteIdPago = calcularSiguienteIdPago();

        sincronizarCeldas();
    }



    public Optional<Usuario> autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) return Optional.empty();
        return usuarios.stream()
                       .filter(u -> u.getCorreo().equalsIgnoreCase(correo.trim())
                                 && u.getContrasena().equals(contrasena.trim())
                                 && u.isActivo())
                       .findFirst();
    }

    public boolean registrarUsuario(String documento, String nombre, String correo,
                                    String contrasena, String telefono, Rol rol) {
        if (isBlank(documento) || isBlank(nombre)    || isBlank(correo)
         || isBlank(contrasena) || isBlank(telefono) || rol == null) return false;

        if (buscarUsuarioPorDocumento(documento).isPresent()) return false;

        usuarios.add(new Usuario(documento, nombre, correo, contrasena, telefono, rol, true));
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    public Optional<Usuario> buscarUsuarioPorDocumento(String documento) {
        if (documento == null) return Optional.empty();
        return usuarios.stream()
                       .filter(u -> u.getDocumento().equalsIgnoreCase(documento.trim()))
                       .findFirst();
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public boolean actualizarUsuario(String documento, String nuevoTelefono, String nuevoCorreo) {
        Optional<Usuario> opt = buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        if (!isBlank(nuevoTelefono)) u.setTelefono(nuevoTelefono.trim());
        if (!isBlank(nuevoCorreo))   u.setCorreo(nuevoCorreo.trim());
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    public boolean desactivarUsuario(String documento) {
        Optional<Usuario> opt = buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) return false;
        opt.get().setActivo(false);
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    public boolean asignarRol(String documento, Rol nuevoRol) {
        if (nuevoRol == null) return false;
        Optional<Usuario> opt = buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) return false;
        opt.get().setRol(nuevoRol);
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }


    public boolean registrarCelda(String codigo) {
        if (isBlank(codigo)) return false;
        if (buscarCeldaPorCodigo(codigo).isPresent()) return false;
        celdas.add(new Celda(siguienteNumeroCelda++, codigo.trim(), true, true, ""));
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    public Optional<Celda> buscarCeldaPorCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return celdas.stream()
                     .filter(c -> c.getCodigo().equalsIgnoreCase(codigo.trim()))
                     .findFirst();
    }

    public List<Celda> listarCeldas() {
        return new ArrayList<>(celdas);
    }

    public List<Celda> listarCeldasDisponibles() {
        return celdas.stream()
                     .filter(c -> c.isHabilitada() && c.isDisponible())
                     .collect(Collectors.toList());
    }

    public boolean actualizarCelda(String codigoActual, String nuevoCodigo, Boolean nuevoEstadoHabilitada) {
        Optional<Celda> opt = buscarCeldaPorCodigo(codigoActual);
        if (opt.isEmpty()) return false;
        Celda c = opt.get();

        if (!isBlank(nuevoCodigo)) {
            String nc = nuevoCodigo.trim();
            boolean duplicado = celdas.stream()
                                      .filter(x -> x != c)
                                      .anyMatch(x -> x.getCodigo().equalsIgnoreCase(nc));
            if (duplicado) return false;
            c.setCodigo(nc);
        }
        if (nuevoEstadoHabilitada != null) {
            c.setHabilitada(nuevoEstadoHabilitada);
        }
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    public boolean deshabilitarCelda(String codigo) {
        Optional<Celda> opt = buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) return false;
        Celda c = opt.get();
        if (!c.isDisponible()) return false;
        c.setHabilitada(false);
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    public boolean asignarCelda(String codigoCelda, String placa) {
        if (isBlank(codigoCelda) || isBlank(placa)) return false;
        Optional<Celda> opt = buscarCeldaPorCodigo(codigoCelda);
        if (opt.isEmpty()) return false;
        Celda c = opt.get();
        if (!c.isHabilitada() || !c.isDisponible()) return false;
        c.setDisponible(false);
        c.setPlacaVehiculo(placa.toUpperCase().trim());
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    public boolean liberarCelda(String codigoCelda) {
        Optional<Celda> opt = buscarCeldaPorCodigo(codigoCelda);
        if (opt.isEmpty()) return false;
        Celda c = opt.get();
        if (c.isDisponible()) return false;
        c.setDisponible(true);
        c.setPlacaVehiculo("");
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    public boolean hayCeldasDisponibles() {
        return celdas.stream().anyMatch(c -> c.isHabilitada() && c.isDisponible());
    }

    public Optional<Celda> obtenerCeldaDisponible() {
        return celdas.stream().filter(c -> c.isHabilitada() && c.isDisponible()).findFirst();
    }


    public boolean registrarNuevoCliente(String placa, String nombre, String telefono) {
        return registrarNuevoCliente(placa, nombre, "", telefono);
    }

    public boolean registrarNuevoCliente(String placa, String nombre, String documento, String telefono) {
        if (isBlank(placa)) return false;
        String pm = placa.toUpperCase().trim();
        if (buscarClientePorPlaca(pm).isPresent()) return false;

        Optional<Celda> celdaOpt = obtenerCeldaDisponible();
        if (celdaOpt.isEmpty()) return false;
        Celda celda = celdaOpt.get();

        LocalDateTime ahora  = LocalDateTime.now();

        LocalDateTime vence  = ahora;
        clientes.add(new Cliente(pm, nombre, documento, telefono, celda.getNumero(), ahora, vence, true));

        celda.setDisponible(false);
        celda.setPlacaVehiculo(pm);

        guardarDatos();
        return true;
    }

    public Optional<Cliente> buscarClientePorPlaca(String placa) {
        if (placa == null) return Optional.empty();
        String pm = placa.toUpperCase().trim();
        return clientes.stream()
                       .filter(c -> c.getPlaca().equalsIgnoreCase(pm))
                       .findFirst();
    }

    public boolean validarIngresoCliente(String placa) {
        Optional<Cliente> opt = buscarClientePorPlaca(placa);
        return opt.isPresent() && opt.get().isVigente();
    }

    public boolean renovarCliente(String placa) {
        Optional<Cliente> opt = buscarClientePorPlaca(placa);
        if (opt.isEmpty()) return false;
        Cliente c = opt.get();
        LocalDateTime base = (c.getFechaVencimiento() == null || c.getFechaVencimiento().isBefore(LocalDateTime.now()))
                ? LocalDateTime.now() : c.getFechaVencimiento();
        c.setFechaVencimiento(base.plusDays(30));
        c.setActivo(true);
        guardarDatos();
        return true;
    }

    public boolean eliminarCliente(String placa) {
        Optional<Cliente> opt = buscarClientePorPlaca(placa);
        if (opt.isEmpty()) return false;
        Cliente c = opt.get();
        if (c.isVigente()) return false;
        liberarCeldaPorNumero(c.getNumeroCelda());
        clientes.remove(c);
        guardarDatos();
        return true;
    }

    public List<Cliente> listarClientes() {
        return new ArrayList<>(clientes);
    }


    public double calcularValorServicio(String placa, int meses) {
        if (meses <= 0) meses = 1;
        return TARIFA_MENSUAL_BASE * meses;
    }


    public String validarValorPago(double valor, double valorEsperado) {
        if (valor <= 0) {
            return "El valor a pagar no puede ser menor o igual a cero. (Valor ingresado: " + valor + ")";
        }
        if (valor < valorEsperado) {
            return String.format("El valor ingresado ($%,.0f) es inferior al valor calculado del servicio ($%,.0f).",
                    valor, valorEsperado);
        }
        return null;
    }


    public String validarMetodoPago(String metodoPagoId) {
        if (isBlank(metodoPagoId)) {
            return "Debe seleccionar un método de pago.";
        }
        Optional<MetodoPago> mpOpt = buscarMetodoPagoPorId(metodoPagoId);
        if (mpOpt.isEmpty()) {
            return "El método de pago '" + metodoPagoId + "' no está registrado en el sistema.";
        }
        if (!mpOpt.get().isHabilitado()) {
            return "El método de pago '" + mpOpt.get().getNombre() + "' se encuentra DESHABILITADO y no está disponible para transacciones.";
        }
        return null;
    }

    public Optional<Pago> buscarPagoDuplicado(String placa) {
        if (isBlank(placa)) return Optional.empty();
        String pm = placa.toUpperCase().trim();
        Optional<Cliente> opt = buscarClientePorPlaca(pm);
        if (opt.isEmpty()) return Optional.empty();

        Cliente c = opt.get();

        if (c.isVigente() && c.getFechaVencimiento().isAfter(LocalDateTime.now().plusDays(25))) {
            return pagos.stream()
                        .filter(p -> p.getPlacaVehiculo().equalsIgnoreCase(pm)
                                  && "APROBADO".equalsIgnoreCase(p.getEstado()))
                        .reduce((primero, segundo) -> segundo);
        }
        return Optional.empty();
    }


    public ResultadoPago registrarPago(String placa, double valor, String metodoPagoId,
                                       String registradoPor, String observaciones) {
        return registrarPago(placa, valor, metodoPagoId, registradoPor, observaciones, 1, false);
    }

    public ResultadoPago registrarPago(String placa, double valor, String metodoPagoId,
                                       String registradoPor, String observaciones,
                                       int meses, boolean permitirAdelantado) {
        if (isBlank(placa)) {
            return ResultadoPago.fallo("Debe ingresar la placa del vehículo.");
        }
        String pm = placa.toUpperCase().trim();
        Optional<Cliente> clienteOpt = buscarClientePorPlaca(pm);
        if (clienteOpt.isEmpty()) {
            return ResultadoPago.fallo("No se encontró ningún cliente registrado con la placa: " + pm);
        }
        Cliente cliente = clienteOpt.get();

        if (meses <= 0) meses = 1;
        double valorCalculado = calcularValorServicio(pm, meses);

        String errorValor = validarValorPago(valor, valorCalculado);
        if (errorValor != null) {
            return ResultadoPago.fallo(errorValor);
        }

        String errorMetodo = validarMetodoPago(metodoPagoId);
        if (errorMetodo != null) {
            return ResultadoPago.fallo(errorMetodo);
        }
        MetodoPago metodo = buscarMetodoPagoPorId(metodoPagoId).get();

        if (!permitirAdelantado) {
            Optional<Pago> dupOpt = buscarPagoDuplicado(pm);
            if (dupOpt.isPresent()) {
                Pago pExistente = dupOpt.get();
                String vencStr = cliente.getFechaVencimiento() != null
                        ? cliente.getFechaVencimiento().format(Cliente.FMT_DISPLAY) : "N/A";
                return ResultadoPago.duplicado(
                    "Se identificó un pago existente para este servicio y usuario (Transacción "
                    + pExistente.getIdTransaccion() + ", valor $" + String.format("%.0f", pExistente.getValor())
                    + "). La mensualidad ya se encuentra cubierta hasta " + vencStr
                    + ". Se evita duplicar el cobro.",
                    pExistente
                );
            }
        }

        String idTransaccion = String.format("PAG-%04d", siguienteIdPago++);

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = (cliente.getFechaVencimiento() == null || cliente.getFechaVencimiento().isBefore(ahora))
                ? ahora : cliente.getFechaVencimiento();
        LocalDateTime finPeriodo = inicioPeriodo.plusDays(30L * meses);

        DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periodo = inicioPeriodo.format(fmtDia) + " al " + finPeriodo.format(fmtDia);

        // Crear pago
        Pago nuevoPago = new Pago(
            idTransaccion,
            pm,
            cliente.getNombrePropietario(),
            cliente.getDocumento(),
            cliente.getNumeroCelda(),
            ahora,
            periodo,
            valor,
            metodo.getId(),
            metodo.getNombre(),
            "APROBADO",
            registradoPor,
            observaciones
        );

        pagos.add(nuevoPago);

        cliente.setFechaVencimiento(finPeriodo);
        cliente.setActivo(true);

        celdas.stream()
              .filter(c -> c.getNumero() == cliente.getNumeroCelda())
              .findFirst()
              .ifPresent(c -> {
                  c.setDisponible(false);
                  c.setPlacaVehiculo(pm);
              });

        guardarDatos();

        return ResultadoPago.ok("Pago registrado exitosamente. Transacción " + idTransaccion, nuevoPago);
    }


    public List<PagoPendiente> consultarPagosPendientes() {
        List<PagoPendiente> pendientes = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (Cliente c : clientes) {
            boolean vencido = c.getFechaVencimiento() == null || c.getFechaVencimiento().isBefore(ahora);
            boolean porVencer = !vencido && c.getFechaVencimiento().isBefore(ahora.plusDays(5));

            boolean tienePagoAprobadoVigente = pagos.stream().anyMatch(p ->
                    p.getPlacaVehiculo().equalsIgnoreCase(c.getPlaca())
                    && "APROBADO".equalsIgnoreCase(p.getEstado())
                    && c.isVigente());

            if (vencido || porVencer || !tienePagoAprobadoVigente) {
                pendientes.add(new PagoPendiente(
                    c.getPlaca(),
                    c.getNombrePropietario(),
                    c.getTelefono(),
                    c.getNumeroCelda(),
                    c.getFechaVencimiento(),
                    TARIFA_MENSUAL_BASE
                ));
            }
        }
        return pendientes;
    }

    public Optional<Pago> buscarPagoPorId(String idTransaccion) {
        if (idTransaccion == null) return Optional.empty();
        return pagos.stream()
                    .filter(p -> p.getIdTransaccion().equalsIgnoreCase(idTransaccion.trim()))
                    .findFirst();
    }

    public List<Pago> listarPagos() {
        return new ArrayList<>(pagos);
    }

    public List<Pago> listarPagosPorPlaca(String placa) {
        if (placa == null) return new ArrayList<>();
        String pm = placa.toUpperCase().trim();
        return pagos.stream()
                    .filter(p -> p.getPlacaVehiculo().equalsIgnoreCase(pm))
                    .collect(Collectors.toList());
    }

    public List<Pago> listarPagosPorCliente(String criterio) {
        if (criterio == null || criterio.isBlank()) return new ArrayList<>();
        String cr = criterio.trim().toUpperCase();
        return pagos.stream()
                    .filter(p -> p.getPlacaVehiculo().equalsIgnoreCase(cr)
                              || p.getDocumentoCliente().equalsIgnoreCase(cr)
                              || p.getNombreCliente().toUpperCase().contains(cr))
                    .collect(Collectors.toList());
    }

    public boolean actualizarPago(String idTransaccion, String nuevoMetodoPagoId,
                                  String nuevoEstado, String nuevasObservaciones) {
        Optional<Pago> opt = buscarPagoPorId(idTransaccion);
        if (opt.isEmpty()) return false;
        Pago p = opt.get();

        if (!isBlank(nuevoMetodoPagoId)) {
            Optional<MetodoPago> mpOpt = buscarMetodoPagoPorId(nuevoMetodoPagoId);
            if (mpOpt.isEmpty() || !mpOpt.get().isHabilitado()) {
                return false;
            }
            p.setMetodoPagoId(mpOpt.get().getId());
            p.setMetodoPagoNombre(mpOpt.get().getNombre());
        }

        if (!isBlank(nuevoEstado)) {
            String ne = nuevoEstado.trim().toUpperCase();
            if (!ne.equals("APROBADO") && !ne.equals("PENDIENTE") && !ne.equals("ANULADO")) {
                return false;
            }
            p.setEstado(ne);
        }

        if (nuevasObservaciones != null) {
            p.setObservaciones(nuevasObservaciones.trim());
        }

        guardarDatos();
        return true;
    }

    public boolean eliminarPago(String idTransaccion) {
        Optional<Pago> opt = buscarPagoPorId(idTransaccion);
        if (opt.isEmpty()) return false;
        pagos.remove(opt.get());
        guardarDatos();
        return true;
    }


    public Optional<String> generarComprobantePago(String idTransaccion) {
        return buscarPagoPorId(idTransaccion).map(Pago::generarComprobante);
    }

    public List<MetodoPago> listarMetodosPago() {
        return new ArrayList<>(metodosPago);
    }

    public List<MetodoPago> listarMetodosPagoHabilitados() {
        return metodosPago.stream()
                          .filter(MetodoPago::isHabilitado)
                          .collect(Collectors.toList());
    }

    public Optional<MetodoPago> buscarMetodoPagoPorId(String id) {
        if (id == null) return Optional.empty();
        return metodosPago.stream()
                          .filter(m -> m.getId().equalsIgnoreCase(id.trim()))
                          .findFirst();
    }

    public boolean agregarMetodoPago(String nombre, String descripcion, boolean habilitado) {
        if (isBlank(nombre)) return false;
        String nm = nombre.trim();
        boolean existe = metodosPago.stream().anyMatch(m -> m.getNombre().equalsIgnoreCase(nm));
        if (existe) return false;

        int nextIdNum = metodosPago.stream()
                                   .mapToInt(m -> {
                                       try {
                                           return Integer.parseInt(m.getId().replace("MP-", "").trim());
                                       } catch (Exception e) { return 0; }
                                   }).max().orElse(0) + 1;
        String id = "MP-" + nextIdNum;
        metodosPago.add(new MetodoPago(id, nm, descripcion, habilitado));
        guardarDatos();
        return true;
    }


    public boolean cambiarEstadoMetodoPago(String id, boolean habilitado) {
        Optional<MetodoPago> opt = buscarMetodoPagoPorId(id);
        if (opt.isEmpty()) return false;
        opt.get().setHabilitado(habilitado);
        guardarDatos();
        return true;
    }


    public void guardarDatos() {
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        DataService.guardarClientes(clientes, RUTA_CLIENTES);
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        DataService.guardarMetodosPago(metodosPago, RUTA_METODOS_PAGO);
        DataService.guardarPagos(pagos, RUTA_PAGOS);
    }

    private Usuario adminPorDefecto() {
        return new Usuario("0000", "Administrador",
                           "admin@autoscolombia.com", "admin123",
                           "0000000000", Rol.ADMIN, true);
    }

    private List<Celda> inicializarCeldas() {
        List<Celda> lista = new ArrayList<>();
        for (int i = 1; i <= TOTAL_CELDAS_INICIAL; i++) {
            lista.add(new Celda(i, "C" + i, true, true, ""));
        }
        siguienteNumeroCelda = TOTAL_CELDAS_INICIAL + 1;
        return lista;
    }

    private List<MetodoPago> inicializarMetodosPago() {
        List<MetodoPago> lista = new ArrayList<>();
        lista.add(new MetodoPago("MP-1", "Efectivo", "Pago en efectivo en ventanilla", true));
        lista.add(new MetodoPago("MP-2", "Tarjeta Débito", "Pago con tarjeta débito por datáfono", true));
        lista.add(new MetodoPago("MP-3", "Tarjeta Crédito", "Pago con tarjeta crédito por datáfono o virtual", true));
        lista.add(new MetodoPago("MP-4", "Transferencia Bancaria (Nequi/Daviplata/PSE)", "Pago por transferencia bancaria o billeteras digitales", true));
        return lista;
    }

    private int calcularSiguienteIdPago() {
        int max = 0;
        for (Pago p : pagos) {
            String id = p.getIdTransaccion();
            if (id != null && id.startsWith("PAG-")) {
                try {
                    int num = Integer.parseInt(id.substring(4).trim());
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return max + 1;
    }

    private void sincronizarCeldas() {
        for (Cliente cliente : clientes) {
            int numCelda = cliente.getNumeroCelda();
            celdas.stream()
                  .filter(c -> c.getNumero() == numCelda && c.isHabilitada())
                  .findFirst()
                  .ifPresent(c -> {
                      c.setDisponible(false);
                      c.setPlacaVehiculo(cliente.getPlaca());
                  });
        }
    }

    private void liberarCeldaPorNumero(int numeroCelda) {
        celdas.stream()
              .filter(c -> c.getNumero() == numeroCelda)
              .findFirst()
              .ifPresent(c -> {
                  c.setDisponible(true);
                  c.setPlacaVehiculo("");
              });
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
