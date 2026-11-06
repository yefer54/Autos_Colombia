package services;

import models.Celda;
import models.Cliente;
import models.Rol;
import models.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio principal del sistema de parqueadero Autos Colombia.
 *
 * Gestiona tres dominios:
 * <ol>
 *   <li>Usuarios del sistema (personal con credenciales y roles)</li>
 *   <li>Celdas del parqueadero (con código, habilitada/disponible)</li>
 *   <li>Clientes / Mensualidades (vehículos con suscripción mensual)</li>
 * </ol>
 */
public class ParqueaderoService {

    public static final int    TOTAL_CELDAS_INICIAL = 10;
    public static final String RUTA_USUARIOS        = "data/usuarios.json";
    public static final String RUTA_CLIENTES        = "data/clientes.json";
    public static final String RUTA_CELDAS          = "data/celdas.json";

    private List<Usuario> usuarios;
    private List<Cliente> clientes;
    private List<Celda>   celdas;
    private int           siguienteNumeroCelda;

    public ParqueaderoService() {
        usuarios = DataService.cargarUsuarios(RUTA_USUARIOS);
        if (usuarios == null) usuarios = new ArrayList<>();

        clientes = DataService.cargarClientes(RUTA_CLIENTES);

        // Migración: si clientes.json no existe, intentar migrar desde el formato anterior
        if (clientes == null || clientes.isEmpty()) {
            clientes = DataService.cargarClientes(RUTA_USUARIOS);
            if (clientes != null && !clientes.isEmpty()) {
                DataService.guardarClientes(clientes, RUTA_CLIENTES);
                System.out.println("  [INFO] Clientes migrados al nuevo archivo clientes.json");
            }
        }
        if (clientes == null) clientes = new ArrayList<>();

        // Crear admin por defecto si no existen usuarios registrados
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

        sincronizarCeldas();
    }

    /**
     * Busca un usuario por correo y contraseña.
     * Solo retorna el usuario si está activo (inactivo → no puede ingresar).
     */
    public Optional<Usuario> autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) return Optional.empty();
        return usuarios.stream()
                       .filter(u -> u.getCorreo().equalsIgnoreCase(correo.trim())
                                 && u.getContrasena().equals(contrasena.trim())
                                 && u.isActivo())
                       .findFirst();
    }

    /**
     * Registra un nuevo usuario del sistema.
     * Todos los campos son obligatorios.
     * Rechaza si el documento ya existe.
     */
    public boolean registrarUsuario(String documento, String nombre, String correo,
                                    String contrasena, String telefono, Rol rol) {
        if (isBlank(documento) || isBlank(nombre)    || isBlank(correo)
         || isBlank(contrasena) || isBlank(telefono) || rol == null) return false;

        if (buscarUsuarioPorDocumento(documento).isPresent()) return false;

        usuarios.add(new Usuario(documento, nombre, correo, contrasena, telefono, rol, true));
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    /** Busca usuario por documento de identidad. */
    public Optional<Usuario> buscarUsuarioPorDocumento(String documento) {
        if (documento == null) return Optional.empty();
        return usuarios.stream()
                       .filter(u -> u.getDocumento().equalsIgnoreCase(documento.trim()))
                       .findFirst();
    }

    /** Retorna copia de la lista de usuarios del sistema. */
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    /**
     * Actualiza teléfono y/o correo de un usuario.
     * Los parámetros null o vacíos se ignoran (conservan valor actual).
     */
    public boolean actualizarUsuario(String documento, String nuevoTelefono, String nuevoCorreo) {
        Optional<Usuario> opt = buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        if (!isBlank(nuevoTelefono)) u.setTelefono(nuevoTelefono.trim());
        if (!isBlank(nuevoCorreo))   u.setCorreo(nuevoCorreo.trim());
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    /**
     * Desactiva un usuario del sistema.
     * El usuario queda inactivo y no puede iniciar sesión.
     */
    public boolean desactivarUsuario(String documento) {
        Optional<Usuario> opt = buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) return false;
        opt.get().setActivo(false);
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    /**
     * Asigna un rol a un usuario del sistema.
     * Los permisos se aplican en el próximo inicio de sesión.
     */
    public boolean asignarRol(String documento, Rol nuevoRol) {
        if (nuevoRol == null) return false;
        Optional<Usuario> opt = buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) return false;
        opt.get().setRol(nuevoRol);
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        return true;
    }

    /**
     * Registra una nueva celda con el código indicado.
     * Rechaza si el código ya existe.
     */
    public boolean registrarCelda(String codigo) {
        if (isBlank(codigo)) return false;
        if (buscarCeldaPorCodigo(codigo).isPresent()) return false;
        celdas.add(new Celda(siguienteNumeroCelda++, codigo.trim(), true, true, ""));
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    /** Busca una celda por su código único. */
    public Optional<Celda> buscarCeldaPorCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return celdas.stream()
                     .filter(c -> c.getCodigo().equalsIgnoreCase(codigo.trim()))
                     .findFirst();
    }

    /** Retorna copia de todas las celdas. */
    public List<Celda> listarCeldas() {
        return new ArrayList<>(celdas);
    }

    /** Retorna solo las celdas habilitadas y libres. */
    public List<Celda> listarCeldasDisponibles() {
        return celdas.stream()
                     .filter(c -> c.isHabilitada() && c.isDisponible())
                     .collect(Collectors.toList());
    }

    /**
     * Actualiza el código y/o estado habilitada de una celda.
     * El nuevo código no puede ser igual al de otra celda.
     */
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

    /**
     * Deshabilita una celda.
     * Rechaza si la celda tiene una asignación activa (está ocupada).
     */
    public boolean deshabilitarCelda(String codigo) {
        Optional<Celda> opt = buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) return false;
        Celda c = opt.get();
        if (!c.isDisponible()) return false;
        c.setHabilitada(false);
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
        return true;
    }

    /**
     * Asigna una celda disponible a un vehículo.
     * Rechaza si la celda ya está ocupada o deshabilitada.
     */
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

    /**
     * Libera una celda (registra salida del vehículo).
     * La celda cambia a estado disponible.
     */
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

    /** Indica si existe al menos una celda habilitada y disponible. */
    public boolean hayCeldasDisponibles() {
        return celdas.stream().anyMatch(c -> c.isHabilitada() && c.isDisponible());
    }

    /** Retorna la primera celda habilitada y disponible, si existe. */
    public Optional<Celda> obtenerCeldaDisponible() {
        return celdas.stream().filter(c -> c.isHabilitada() && c.isDisponible()).findFirst();
    }

    /** Registra un nuevo cliente y le asigna automáticamente la primera celda disponible. */
    public boolean registrarNuevoCliente(String placa, String nombre, String telefono) {
        if (isBlank(placa)) return false;
        String pm = placa.toUpperCase().trim();
        if (buscarClientePorPlaca(pm).isPresent()) return false;

        Optional<Celda> celdaOpt = obtenerCeldaDisponible();
        if (celdaOpt.isEmpty()) return false;
        Celda celda = celdaOpt.get();

        LocalDateTime ahora  = LocalDateTime.now();
        LocalDateTime vence  = ahora.plusDays(30);
        clientes.add(new Cliente(pm, nombre, telefono, celda.getNumero(), ahora, vence, true));

        celda.setDisponible(false);
        celda.setPlacaVehiculo(pm);

        guardarDatos();
        return true;
    }

    /** Busca un cliente por placa de vehículo. */
    public Optional<Cliente> buscarClientePorPlaca(String placa) {
        if (placa == null) return Optional.empty();
        String pm = placa.toUpperCase().trim();
        return clientes.stream()
                       .filter(c -> c.getPlaca().equalsIgnoreCase(pm))
                       .findFirst();
    }

    /** Valida si un vehículo puede ingresar (mensualidad vigente). */
    public boolean validarIngresoCliente(String placa) {
        Optional<Cliente> opt = buscarClientePorPlaca(placa);
        return opt.isPresent() && opt.get().isVigente();
    }

    /** Renueva la mensualidad de un cliente por 30 días más. */
    public boolean renovarCliente(String placa) {
        Optional<Cliente> opt = buscarClientePorPlaca(placa);
        if (opt.isEmpty()) return false;
        Cliente c = opt.get();
        c.setFechaVencimiento(LocalDateTime.now().plusDays(30));
        c.setActivo(true);
        guardarDatos();
        return true;
    }

    /** Elimina un cliente (solo si la mensualidad está vencida) y libera su celda. */
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

    /** Retorna copia de la lista de clientes. */
    public List<Cliente> listarClientes() {
        return new ArrayList<>(clientes);
    }

    /** Persiste todos los datos en disco. */
    public void guardarDatos() {
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        DataService.guardarClientes(clientes, RUTA_CLIENTES);
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
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

    /**
     * Sincroniza el estado de las celdas con los clientes activos.
     * Solo marca como ocupadas las celdas de clientes; no toca otras asignaciones.
     */
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
