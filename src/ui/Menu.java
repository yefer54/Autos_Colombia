package ui;

import models.Celda;
import models.Cliente;
import models.Rol;
import models.Usuario;
import services.ParqueaderoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interfaz de consola del sistema Parqueadero Autos Colombia.
 * Permite gestionar usuarios, celdas, asignaciones y mensualidades según el rol del usuario activo.
 */
public class Menu {

    private static final String LINEA_DOBLE  = "══════════════════════════════════════════════";
    private static final String LINEA_SIMPLE = "──────────────────────────────────────────────";

    private final ParqueaderoService servicio;
    private final Scanner            scanner;
    private       Usuario            usuarioActual;

    public Menu(ParqueaderoService servicio) {
        this.servicio = servicio;
        this.scanner  = new Scanner(System.in, "UTF-8");
    }

    public void iniciar() {
        mostrarBienvenida();
        usuarioActual = obtenerUsuarioInicial();
        menuPrincipalLoop();
        mostrarDespedida();
    }

    private Usuario obtenerUsuarioInicial() {
        return servicio.listarUsuarios().stream()
                       .filter(u -> u.getRol() == Rol.ADMIN && u.isActivo())
                       .findFirst()
                       .orElseGet(() -> servicio.listarUsuarios().stream()
                               .filter(Usuario::isActivo)
                               .findFirst()
                               .orElse(new Usuario("0000", "Administrador", "admin@autoscolombia.com", "admin123", "0000000000", Rol.ADMIN, true)));
    }

    private void cambiarUsuario() {
        Usuario u = flujoLogin();
        if (u != null) {
            usuarioActual = u;
        }
    }

    /** Solicita credenciales de un usuario; retorna null si cancela o son inválidas. */
    private Usuario flujoLogin() {
        imprimirEncabezado("INICIO DE SESIÓN");
        System.out.println("  Ingrese sus credenciales.  (Escriba 0 en correo para cancelar)");
        System.out.println(LINEA_SIMPLE);

        String correo = leerTextoConPrompt("  Correo     : ").trim();
        if ("0".equals(correo)) return null;

        String clave = leerTextoConPrompt("  Contraseña : ").trim();

        Optional<Usuario> opt = servicio.autenticar(correo, clave);
        if (opt.isPresent()) {
            System.out.println("\n  ✔  Bienvenido/a, " + opt.get().getNombre()
                    + "  [" + opt.get().getRol().name() + "]");
            pausar();
            return opt.get();
        }
        System.out.println("\n  ✘  Credenciales incorrectas o cuenta inactiva.");
        pausar();
        return null;
    }

    private void menuPrincipalLoop() {
        boolean salir = false;
        while (!salir) {
            salir = (usuarioActual.getRol() == Rol.ADMIN)
                    ? menuAdminLoop()
                    : menuOperarioLoop();
        }
    }

    /** Menú principal del Administrador. */
    private boolean menuAdminLoop() {
        imprimirEncabezado("MENÚ PRINCIPAL — ADMINISTRADOR");
        System.out.println("  Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getRol().name() + ")");
        System.out.println(LINEA_SIMPLE);
        System.out.println("  1. Gestión de Usuarios del Sistema");
        System.out.println("  2. Gestión de Celdas");
        System.out.println("  3. Asignaciones de Celdas");
        System.out.println("  4. Mensualidades / Clientes");
        System.out.println("  5. Iniciar sesión / Cambiar de usuario");
        System.out.println("  0. Salir");
        System.out.println(LINEA_SIMPLE);

        switch (leerEnteroConPrompt("  Seleccione una opción: ")) {
            case 1: menuUsuariosLoop();      break;
            case 2: menuCeldasLoop();        break;
            case 3: menuAsignacionesLoop();  break;
            case 4: menuMensualidadesLoop(); break;
            case 5: cambiarUsuario();        break;
            case 0: return true;
            default: System.out.println("\n  [!] Opción no válida."); pausar();
        }
        return false;
    }

    /** Menú principal del Operario (acceso restringido). */
    private boolean menuOperarioLoop() {
        imprimirEncabezado("MENÚ PRINCIPAL — OPERARIO");
        System.out.println("  Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getRol().name() + ")");
        System.out.println(LINEA_SIMPLE);
        System.out.println("  1. Asignaciones de Celdas");
        System.out.println("  2. Mensualidades / Clientes");
        System.out.println("  3. Ver celdas disponibles");
        System.out.println("  4. Iniciar sesión / Cambiar de usuario");
        System.out.println("  0. Salir");
        System.out.println(LINEA_SIMPLE);

        switch (leerEnteroConPrompt("  Seleccione una opción: ")) {
            case 1: menuAsignacionesLoop();   break;
            case 2: menuMensualidadesLoop();  break;
            case 3: flujoCeldasDisponibles(); break;
            case 4: cambiarUsuario();         break;
            case 0: return true;
            default: System.out.println("\n  [!] Opción no válida."); pausar();
        }
        return false;
    }

    private void menuUsuariosLoop() {
        boolean activo = true;
        while (activo) {
            imprimirEncabezado("GESTIÓN DE USUARIOS DEL SISTEMA");
            System.out.println("  1. Registrar usuario");
            System.out.println("  2. Consultar usuarios");
            System.out.println("  3. Actualizar usuario");
            System.out.println("  4. Desactivar usuario");
            System.out.println("  5. Asignar rol");
            System.out.println("  6. Iniciar sesión con un usuario");
            System.out.println("  0. Volver");
            System.out.println(LINEA_SIMPLE);

            switch (leerEnteroConPrompt("  Seleccione: ")) {
                case 1: flujoRegistrarUsuario();  break;
                case 2: flujoConsultarUsuarios(); break;
                case 3: flujoActualizarUsuario(); break;
                case 4: flujoDesactivarUsuario(); break;
                case 5: flujoAsignarRol();        break;
                case 6: cambiarUsuario();         break;
                case 0: activo = false; break;
                default: System.out.println("\n  [!] Opción no válida."); pausar();
            }
        }
    }

    private void menuCeldasLoop() {
        boolean activo = true;
        while (activo) {
            imprimirEncabezado("GESTIÓN DE CELDAS");
            System.out.println("  1. Registrar celda");
            System.out.println("  2. Consultar todas las celdas");
            System.out.println("  3. Actualizar celda");
            System.out.println("  4. Ver celdas disponibles");
            System.out.println("  5. Deshabilitar celda");
            System.out.println("  0. Volver");
            System.out.println(LINEA_SIMPLE);

            switch (leerEnteroConPrompt("  Seleccione: ")) {
                case 1: flujoRegistrarCelda();    break;
                case 2: flujoConsultarCeldas();   break;
                case 3: flujoActualizarCelda();   break;
                case 4: flujoCeldasDisponibles(); break;
                case 5: flujoDeshabilitarCelda(); break;
                case 0: activo = false; break;
                default: System.out.println("\n  [!] Opción no válida."); pausar();
            }
        }
    }

    private void menuAsignacionesLoop() {
        boolean activo = true;
        while (activo) {
            imprimirEncabezado("ASIGNACIONES DE CELDAS");
            System.out.println("  1. Asignar celda a vehículo");
            System.out.println("  2. Liberar celda");
            System.out.println("  0. Volver");
            System.out.println(LINEA_SIMPLE);

            switch (leerEnteroConPrompt("  Seleccione: ")) {
                case 1: flujoAsignarCelda(); break;
                case 2: flujoLiberarCelda(); break;
                case 0: activo = false; break;
                default: System.out.println("\n  [!] Opción no válida."); pausar();
            }
        }
    }

    private void menuMensualidadesLoop() {
        boolean activo = true;
        while (activo) {
            imprimirEncabezado("MENSUALIDADES / CLIENTES");
            System.out.println("  1. Registrar nuevo cliente");
            System.out.println("  2. Validar ingreso de vehículo");
            System.out.println("  3. Consultar lista de clientes");
            System.out.println("  4. Buscar cliente por placa");
            System.out.println("  5. Renovar mensualidad");
            System.out.println("  6. Eliminar cliente (mensualidad vencida)");
            System.out.println("  0. Volver");
            System.out.println(LINEA_SIMPLE);

            switch (leerEnteroConPrompt("  Seleccione: ")) {
                case 1: flujoRegistrarCliente();  break;
                case 2: flujoValidarIngreso();    break;
                case 3: flujoConsultarClientes(); break;
                case 4: flujoBuscarPorPlaca();    break;
                case 5: flujoRenovarCliente();    break;
                case 6: flujoEliminarCliente();   break;
                case 0: activo = false; break;
                default: System.out.println("\n  [!] Opción no válida."); pausar();
            }
        }
    }

    /** Registra un nuevo usuario solicitando todos los datos obligatorios. */
    private void flujoRegistrarUsuario() {
        imprimirEncabezado("REGISTRAR USUARIO DEL SISTEMA");
        System.out.println("  Todos los campos son obligatorios.");
        System.out.println(LINEA_SIMPLE);

        String documento  = leerTextoConPrompt("  Documento    : ").trim();
        String nombre     = leerTextoConPrompt("  Nombre       : ").trim();
        String correo     = leerTextoConPrompt("  Correo       : ").trim();
        String contrasena = leerTextoConPrompt("  Contraseña   : ").trim();
        String telefono   = leerTextoConPrompt("  Teléfono     : ").trim();
        System.out.println("  Rol: 1) ADMIN   2) OPERARIO");
        int    rolOp      = leerEnteroConPrompt("  Rol           : ");
        Rol    rol        = rolOp == 1 ? Rol.ADMIN : rolOp == 2 ? Rol.OPERARIO : null;

        List<String> faltantes = new ArrayList<>();
        if (documento.isEmpty())  faltantes.add("Documento");
        if (nombre.isEmpty())     faltantes.add("Nombre");
        if (correo.isEmpty())     faltantes.add("Correo");
        if (contrasena.isEmpty()) faltantes.add("Contraseña");
        if (telefono.isEmpty())   faltantes.add("Teléfono");
        if (rol == null)          faltantes.add("Rol (selección inválida, elija 1 o 2)");

        if (!faltantes.isEmpty()) {
            System.out.println("\n  [!] Los siguientes campos son requeridos:");
            faltantes.forEach(f -> System.out.println("      • " + f));
            pausar();
            return;
        }

        if (servicio.buscarUsuarioPorDocumento(documento).isPresent()) {
            System.out.println("\n  [!] ADVERTENCIA: El documento '" + documento
                    + "' ya está registrado en el sistema. Registro rechazado.");
            pausar();
            return;
        }

        boolean ok = servicio.registrarUsuario(documento, nombre, correo, contrasena, telefono, rol);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Usuario registrado exitosamente en el sistema."
            : "  [!] No se pudo registrar el usuario. Intente nuevamente.");
        pausar();
    }

    /** Muestra información de todos los usuarios registrados. */
    private void flujoConsultarUsuarios() {
        imprimirEncabezado("LISTA DE USUARIOS DEL SISTEMA");

        List<Usuario> usuarios = servicio.listarUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("  No hay usuarios registrados.");
            pausar();
            return;
        }

        System.out.println();
        String fmt = "  %-12s %-20s %-28s %-10s %-8s";
        System.out.printf(fmt, "DOCUMENTO", "NOMBRE", "CORREO", "ROL", "ESTADO");
        System.out.println();
        System.out.println("  " + "─".repeat(82));

        for (Usuario u : usuarios) {
            String nom = u.getNombre().length() > 19 ? u.getNombre().substring(0, 19) : u.getNombre();
            String cor = u.getCorreo().length()  > 27 ? u.getCorreo().substring(0, 27)  : u.getCorreo();
            System.out.printf(fmt,
                u.getDocumento(), nom, cor, u.getRol().name(),
                u.isActivo() ? "ACTIVO" : "INACTIVO");
            System.out.println();
        }

        System.out.println("  " + "─".repeat(82));
        System.out.println("  Total: " + usuarios.size() + " usuario(s)");
        pausar();
    }

    /** Permite modificar el teléfono o correo de un usuario. */
    private void flujoActualizarUsuario() {
        imprimirEncabezado("ACTUALIZAR USUARIO");

        String documento = leerTextoConPrompt("  Documento del usuario: ").trim();
        if (documento.isEmpty()) {
            System.out.println("  [!] Debe ingresar un documento.");
            pausar();
            return;
        }

        Optional<Usuario> opt = servicio.buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró usuario con documento: " + documento);
            pausar();
            return;
        }

        Usuario u = opt.get();
        System.out.println("\n  Usuario encontrado:");
        System.out.println(u.toString());
        System.out.println("\n  Deje en blanco para conservar el valor actual.");

        String nuevoTel    = leerTextoConPrompt("  Nuevo teléfono  [" + u.getTelefono() + "]: ").trim();
        String nuevoCorreo = leerTextoConPrompt("  Nuevo correo    [" + u.getCorreo()   + "]: ").trim();

        if (nuevoTel.isEmpty() && nuevoCorreo.isEmpty()) {
            System.out.println("\n  No se realizaron cambios.");
            pausar();
            return;
        }

        boolean ok = servicio.actualizarUsuario(documento,
            nuevoTel.isEmpty()    ? null : nuevoTel,
            nuevoCorreo.isEmpty() ? null : nuevoCorreo);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Usuario actualizado correctamente."
            : "  [!] No se pudo actualizar el usuario.");
        pausar();
    }

    /** Desactiva un usuario activo para revocar su acceso al sistema. */
    private void flujoDesactivarUsuario() {
        imprimirEncabezado("DESACTIVAR USUARIO");

        String documento = leerTextoConPrompt("  Documento del usuario a desactivar: ").trim();
        if (documento.isEmpty()) {
            System.out.println("  [!] Debe ingresar un documento.");
            pausar();
            return;
        }

        Optional<Usuario> opt = servicio.buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró usuario con documento: " + documento);
            pausar();
            return;
        }

        Usuario u = opt.get();
        System.out.println("\n  Usuario encontrado:");
        System.out.println(u.toString());

        if (!u.isActivo()) {
            System.out.println("\n  [!] El usuario ya se encuentra INACTIVO.");
            pausar();
            return;
        }
        if (u.getDocumento().equals(usuarioActual.getDocumento())) {
            System.out.println("\n  [!] No puede desactivar su propia cuenta.");
            pausar();
            return;
        }

        String conf = leerTextoConPrompt("\n  ¿Confirma la desactivación? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.desactivarUsuario(documento);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Usuario '" + u.getNombre() + "' desactivado exitosamente.");
            System.out.println("     Estado: INACTIVO — El usuario ya no podrá iniciar sesión.");
        } else {
            System.out.println("  [!] No se pudo desactivar el usuario.");
        }
        pausar();
    }

    /** Asigna un nuevo rol (ADMIN / OPERARIO) a un usuario. */
    private void flujoAsignarRol() {
        imprimirEncabezado("ASIGNAR ROL A USUARIO");

        String documento = leerTextoConPrompt("  Documento del usuario: ").trim();
        if (documento.isEmpty()) {
            System.out.println("  [!] Debe ingresar un documento.");
            pausar();
            return;
        }

        Optional<Usuario> opt = servicio.buscarUsuarioPorDocumento(documento);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró usuario con documento: " + documento);
            pausar();
            return;
        }

        Usuario u = opt.get();
        System.out.println("\n  Usuario: " + u.getNombre() + "  |  Rol actual: " + u.getRol().name());
        System.out.println("  1) ADMIN     2) OPERARIO");
        int rolOp = leerEnteroConPrompt("  Seleccione nuevo rol: ");
        Rol nuevoRol = rolOp == 1 ? Rol.ADMIN : rolOp == 2 ? Rol.OPERARIO : null;

        if (nuevoRol == null) {
            System.out.println("\n  [!] Selección inválida. Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.asignarRol(documento, nuevoRol);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Rol asignado: " + nuevoRol.name());
            System.out.println("     Los permisos se aplicarán en el próximo inicio de sesión.");
        } else {
            System.out.println("  [!] No se pudo asignar el rol.");
        }
        pausar();
    }

    /** Registra una nueva celda en el parqueadero con su código único. */
    private void flujoRegistrarCelda() {
        imprimirEncabezado("REGISTRAR CELDA");

        String codigo = leerTextoConPrompt("  Código de la celda: ").trim();
        if (codigo.isEmpty()) {
            System.out.println("  [!] El código es obligatorio.");
            pausar();
            return;
        }

        if (servicio.buscarCeldaPorCodigo(codigo).isPresent()) {
            System.out.println("\n  [!] El código '" + codigo
                    + "' ya existe. No se permite registro duplicado.");
            pausar();
            return;
        }

        boolean ok = servicio.registrarCelda(codigo);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Celda '" + codigo + "' registrada correctamente.");
            System.out.println("     Estado: LIBRE  |  Habilitada: SÍ");
        } else {
            System.out.println("  [!] No se pudo registrar la celda.");
        }
        pausar();
    }

    /** Consulta y muestra todas las celdas con sus estados actuales. */
    private void flujoConsultarCeldas() {
        imprimirEncabezado("ESTADO DE CELDAS DEL PARQUEADERO");

        List<Celda> celdas = servicio.listarCeldas();
        if (celdas.isEmpty()) {
            System.out.println("  No hay celdas registradas.");
            pausar();
            return;
        }

        long libres         = celdas.stream().filter(c ->  c.isHabilitada() &&  c.isDisponible()).count();
        long ocupadas       = celdas.stream().filter(c ->  c.isHabilitada() && !c.isDisponible()).count();
        long deshabilitadas = celdas.stream().filter(c -> !c.isHabilitada()).count();

        System.out.println();
        System.out.println("  ┌────────┬───────────────┬─────────────┐");
        System.out.println("  │ CÓDIGO │    ESTADO     │    PLACA    │");
        System.out.println("  ├────────┼───────────────┼─────────────┤");
        for (Celda c : celdas) {
            String estado;
            if (!c.isHabilitada())    estado = "DESHABILITADA";
            else if (c.isDisponible()) estado = "LIBRE        ";
            else                       estado = "OCUPADA      ";
            String placa = (!c.isDisponible() && c.getPlacaVehiculo() != null
                            && !c.getPlacaVehiculo().isEmpty())
                           ? c.getPlacaVehiculo() : "    ---    ";
            System.out.printf("  │ %-6s │ %-13s │ %-11s │%n",
                c.getCodigo(), estado, placa);
        }
        System.out.println("  └────────┴───────────────┴─────────────┘");
        System.out.printf("%n  Libres: %d  |  Ocupadas: %d  |  Deshabilitadas: %d  |  Total: %d%n",
            libres, ocupadas, deshabilitadas, celdas.size());
        pausar();
    }

    /** Permite modificar el código o estado de una celda. */
    private void flujoActualizarCelda() {
        imprimirEncabezado("ACTUALIZAR CELDA");

        String codigo = leerTextoConPrompt("  Código actual de la celda: ").trim();
        if (codigo.isEmpty()) {
            System.out.println("  [!] Debe ingresar un código.");
            pausar();
            return;
        }

        Optional<Celda> opt = servicio.buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró celda con código: " + codigo);
            pausar();
            return;
        }

        Celda c = opt.get();
        System.out.println("\n  Celda encontrada:");
        System.out.println("     Código     : " + c.getCodigo());
        System.out.println("     Habilitada : " + (c.isHabilitada() ? "Sí" : "No"));
        System.out.println("     Disponible : " + (c.isDisponible() ? "Sí" : "No"));
        System.out.println("\n  Deje en blanco para conservar el valor actual.");

        String nuevoCodigo = leerTextoConPrompt("  Nuevo código       [" + c.getCodigo() + "]: ").trim();
        System.out.println("  Cambiar estado habilitada:  1) Habilitar   2) Deshabilitar   0) Sin cambio");
        int     estOp      = leerEnteroConPrompt("  Seleccione: ");
        Boolean nuevoEst   = estOp == 1 ? Boolean.TRUE : estOp == 2 ? Boolean.FALSE : null;

        if (nuevoCodigo.isEmpty() && nuevoEst == null) {
            System.out.println("\n  No se realizaron cambios.");
            pausar();
            return;
        }

        // Proteger celda ocupada de ser deshabilitada
        if (Boolean.FALSE.equals(nuevoEst) && !c.isDisponible()) {
            System.out.println("\n  [!] No se puede deshabilitar: la celda tiene una asignación activa.");
            System.out.println("      Placa asignada: " + c.getPlacaVehiculo());
            pausar();
            return;
        }

        boolean ok = servicio.actualizarCelda(codigo,
            nuevoCodigo.isEmpty() ? null : nuevoCodigo,
            nuevoEst);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Celda actualizada correctamente."
            : "  [!] No se pudo actualizar. El nuevo código puede estar duplicado.");
        pausar();
    }

    /** Muestra únicamente las celdas libres y habilitadas. */
    private void flujoCeldasDisponibles() {
        imprimirEncabezado("CELDAS DISPONIBLES");

        List<Celda> disponibles = servicio.listarCeldasDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("  No hay celdas disponibles en este momento.");
            pausar();
            return;
        }

        System.out.println();
        System.out.println("  Celdas habilitadas y libres:");
        System.out.println();
        System.out.println("  ┌────────┬───────────┐");
        System.out.println("  │ CÓDIGO │   ESTADO  │");
        System.out.println("  ├────────┼───────────┤");
        for (Celda c : disponibles) {
            System.out.printf("  │ %-6s │   LIBRE   │%n", c.getCodigo());
        }
        System.out.println("  └────────┴───────────┘");
        System.out.println("\n  Total disponibles: " + disponibles.size());
        pausar();
    }

    /** Deshabilita una celda asegurando que no tenga una asignación activa. */
    private void flujoDeshabilitarCelda() {
        imprimirEncabezado("DESHABILITAR CELDA");

        String codigo = leerTextoConPrompt("  Código de la celda a deshabilitar: ").trim();
        if (codigo.isEmpty()) {
            System.out.println("  [!] Debe ingresar un código.");
            pausar();
            return;
        }

        Optional<Celda> opt = servicio.buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró celda con código: " + codigo);
            pausar();
            return;
        }

        Celda c = opt.get();
        System.out.println("\n  Celda: " + c.getCodigo()
                + "  |  " + (c.isHabilitada() ? "Habilitada" : "Ya deshabilitada")
                + "  |  " + (c.isDisponible() ? "LIBRE" : "OCUPADA — Placa: " + c.getPlacaVehiculo()));

        if (!c.isHabilitada()) {
            System.out.println("\n  [!] La celda ya está deshabilitada.");
            pausar();
            return;
        }

        if (!c.isDisponible()) {
            System.out.println("\n  [!] OPERACIÓN IMPEDIDA: La celda tiene una asignación activa.");
            System.out.println("      Placa asignada: " + c.getPlacaVehiculo());
            System.out.println("      Debe liberar la celda antes de deshabilitarla.");
            pausar();
            return;
        }

        String conf = leerTextoConPrompt("\n  ¿Confirma deshabilitar la celda '" + codigo + "'? (S/N): ")
                .toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.deshabilitarCelda(codigo);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Celda '" + codigo + "' deshabilitada exitosamente."
            : "  [!] No se pudo deshabilitar la celda.");
        pausar();
    }

    /** Asigna una celda disponible a un vehículo. */
    private void flujoAsignarCelda() {
        imprimirEncabezado("ASIGNAR CELDA A VEHÍCULO");

        List<Celda> disponibles = servicio.listarCeldasDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("  [!] No hay celdas disponibles en este momento.");
            pausar();
            return;
        }

        System.out.println("  Celdas disponibles:");
        disponibles.forEach(c -> System.out.println("    • " + c.getCodigo()));
        System.out.println();

        String codigo = leerTextoConPrompt("  Código de la celda  : ").trim();
        String placa  = leerTextoConPrompt("  Placa del vehículo  : ").toUpperCase().trim();

        if (codigo.isEmpty() || placa.isEmpty()) {
            System.out.println("\n  [!] Código y placa son obligatorios.");
            pausar();
            return;
        }

        Optional<Celda> opt = servicio.buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró celda con código: " + codigo);
            pausar();
            return;
        }

        Celda c = opt.get();

        if (!c.isDisponible()) {
            System.out.println("\n  [!] ASIGNACIÓN IMPEDIDA: La celda '" + codigo + "' ya está ocupada.");
            System.out.println("      Vehículo actual: " + c.getPlacaVehiculo());
            pausar();
            return;
        }
        if (!c.isHabilitada()) {
            System.out.println("\n  [!] La celda '" + codigo + "' está deshabilitada.");
            pausar();
            return;
        }

        boolean ok = servicio.asignarCelda(codigo, placa);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Celda '" + codigo + "' asignada al vehículo [" + placa + "].");
            System.out.println("     Estado: OCUPADA");
        } else {
            System.out.println("  [!] No se pudo realizar la asignación.");
        }
        pausar();
    }

    /** Libera una celda ocupada registrando la salida del vehículo. */
    private void flujoLiberarCelda() {
        imprimirEncabezado("LIBERAR CELDA — REGISTRO DE SALIDA");

        String codigo = leerTextoConPrompt("  Código de la celda a liberar: ").trim();
        if (codigo.isEmpty()) {
            System.out.println("  [!] Debe ingresar un código.");
            pausar();
            return;
        }

        Optional<Celda> opt = servicio.buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró celda con código: " + codigo);
            pausar();
            return;
        }

        Celda c = opt.get();
        if (c.isDisponible()) {
            System.out.println("\n  [!] La celda '" + codigo + "' ya se encuentra libre.");
            pausar();
            return;
        }

        System.out.println("\n  Celda: " + c.getCodigo() + "  |  Vehículo: " + c.getPlacaVehiculo());
        String conf = leerTextoConPrompt("  ¿Confirmar salida del vehículo? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        String placaLiberada = c.getPlacaVehiculo();
        boolean ok = servicio.liberarCelda(codigo);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Vehículo [" + placaLiberada + "] registrado como salida.");
            System.out.println("     Celda '" + codigo + "' ahora está DISPONIBLE.");
        } else {
            System.out.println("  [!] No se pudo liberar la celda.");
        }
        pausar();
    }

    private void flujoRegistrarCliente() {
        imprimirEncabezado("REGISTRAR NUEVO CLIENTE (MENSUALIDAD)");

        if (!servicio.hayCeldasDisponibles()) {
            System.out.println("  [!] No hay celdas disponibles. El parqueadero está lleno.");
            pausar();
            return;
        }

        String placa    = leerTextoConPrompt("  Placa del vehículo    : ").toUpperCase().trim();
        String nombre   = leerTextoConPrompt("  Nombre del propietario: ").trim();
        String telefono = leerTextoConPrompt("  Teléfono de contacto  : ").trim();

        if (placa.isEmpty() || nombre.isEmpty()) {
            System.out.println("\n  [!] La placa y el nombre son obligatorios.");
            pausar();
            return;
        }

        if (servicio.buscarClientePorPlaca(placa).isPresent()) {
            System.out.println("\n  [!] La placa '" + placa + "' ya está registrada en el sistema.");
            pausar();
            return;
        }

        boolean ok = servicio.registrarNuevoCliente(placa, nombre, telefono);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            Optional<Cliente> reg = servicio.buscarClientePorPlaca(placa);
            System.out.println("  ✔  Cliente registrado exitosamente.");
            reg.ifPresent(c ->
                System.out.println("     Celda N°: " + c.getNumeroCelda()
                    + "  |  Vencimiento: " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY))
            );
        } else {
            System.out.println("  [!] No se pudo registrar el cliente.");
        }
        pausar();
    }

    private void flujoValidarIngreso() {
        imprimirEncabezado("VALIDAR INGRESO DE VEHÍCULO");

        String placa = leerTextoConPrompt("  Placa del vehículo: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("  [!] Placa no puede estar vacía.");
            pausar();
            return;
        }

        System.out.println();
        System.out.println(LINEA_SIMPLE);

        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        if (opt.isEmpty()) {
            System.out.println("  ✘  Placa [" + placa + "] NO encontrada. El vehículo no está registrado.");
        } else {
            Cliente c = opt.get();
            if (c.isVigente()) {
                System.out.println("  ✔  ACCESO PERMITIDO");
                System.out.println("     Placa       : " + c.getPlaca());
                System.out.println("     Propietario : " + c.getNombrePropietario());
                System.out.println("     Celda N°    : " + c.getNumeroCelda());
                System.out.println("     Vence       : " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
            } else {
                System.out.println("  ✘  ACCESO DENEGADO — Mensualidad vencida.");
                System.out.println("     Placa       : " + c.getPlaca());
                System.out.println("     Venció el   : " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
                System.out.println("     Renueve su mensualidad (opción 5 en Mensualidades).");
            }
        }
        pausar();
    }

    private void flujoConsultarClientes() {
        imprimirEncabezado("LISTA DE CLIENTES (MENSUALIDADES)");

        List<Cliente> clientes = servicio.listarClientes();
        if (clientes.isEmpty()) {
            System.out.println("  No hay clientes registrados.");
            pausar();
            return;
        }

        System.out.println();
        String fmt = "  %-10s %-20s %-13s %-17s %-17s %-8s";
        System.out.printf(fmt, "PLACA", "PROPIETARIO", "TELÉFONO", "INGRESO", "VENCIMIENTO", "ESTADO");
        System.out.println();
        System.out.println("  " + "─".repeat(90));

        for (Cliente c : clientes) {
            String ing = c.getFechaIngreso()     != null ? c.getFechaIngreso().format(Cliente.FMT_DISPLAY)     : "N/A";
            String ven = c.getFechaVencimiento() != null ? c.getFechaVencimiento().format(Cliente.FMT_DISPLAY) : "N/A";
            String nom = c.getNombrePropietario().length() > 19
                         ? c.getNombrePropietario().substring(0, 19)
                         : c.getNombrePropietario();
            System.out.printf(fmt,
                c.getPlaca(), nom, c.getTelefono(), ing, ven,
                c.isVigente() ? "ACTIVO" : "INACTIVO");
            System.out.println();
        }

        System.out.println("  " + "─".repeat(90));
        System.out.println("  Total: " + clientes.size() + " cliente(s)");
        pausar();
    }

    private void flujoBuscarPorPlaca() {
        imprimirEncabezado("BUSCAR CLIENTE POR PLACA");

        String placa = leerTextoConPrompt("  Placa a buscar: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        System.out.println();
        if (opt.isPresent()) {
            System.out.println("  Información del cliente:");
            System.out.println();
            System.out.println(opt.get().toString());
        } else {
            System.out.println("  [!] No se encontró ningún cliente con placa: " + placa);
        }
        pausar();
    }

    private void flujoRenovarCliente() {
        imprimirEncabezado("RENOVAR MENSUALIDAD");

        String placa = leerTextoConPrompt("  Placa del vehículo: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró cliente con placa: " + placa);
            pausar();
            return;
        }

        Cliente c = opt.get();
        System.out.println("\n  Cliente encontrado:");
        System.out.printf("     Propietario  : %s%n", c.getNombrePropietario());
        System.out.printf("     Estado actual: %s%n", c.isVigente() ? "ACTIVO" : "INACTIVO");
        System.out.printf("     Vencimiento  : %s%n", c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));

        String conf = leerTextoConPrompt("\n  ¿Confirma renovación por 30 días? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.renovarCliente(placa);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            Optional<Cliente> renovado = servicio.buscarClientePorPlaca(placa);
            System.out.println("  ✔  Mensualidad renovada exitosamente.");
            renovado.ifPresent(r ->
                System.out.println("     Nuevo vencimiento: " + r.getFechaVencimiento().format(Cliente.FMT_DISPLAY))
            );
        } else {
            System.out.println("  [!] No se pudo renovar la mensualidad.");
        }
        pausar();
    }

    private void flujoEliminarCliente() {
        imprimirEncabezado("ELIMINAR CLIENTE");

        String placa = leerTextoConPrompt("  Placa del cliente a eliminar: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró cliente con placa: " + placa);
            pausar();
            return;
        }

        Cliente c = opt.get();
        System.out.println("\n  Cliente encontrado:");
        System.out.println(c.toString());

        if (c.isVigente()) {
            System.out.println("\n  [!] No se puede eliminar: la mensualidad aún está VIGENTE.");
            System.out.println("      Vence el: " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
            pausar();
            return;
        }

        String conf = leerTextoConPrompt("\n  ¿Confirma eliminar este cliente? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.eliminarCliente(placa);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Cliente [" + placa + "] eliminado. La celda ha sido liberada."
            : "  [!] No se pudo eliminar el cliente.");
        pausar();
    }

    private void mostrarBienvenida() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║       PARQUEADERO AUTOS COLOMBIA             ║");
        System.out.println("  ║           Sistema de Gestión                 ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    private void mostrarDespedida() {
        System.out.println();
        System.out.println("  " + LINEA_DOBLE);
        System.out.println("    Gracias por usar el sistema Autos Colombia.");
        System.out.println("    ¡Hasta pronto!");
        System.out.println("  " + LINEA_DOBLE);
    }

    private void imprimirEncabezado(String titulo) {
        System.out.println();
        System.out.println("  " + LINEA_DOBLE);
        System.out.println("    " + titulo);
        System.out.println("  " + LINEA_DOBLE);
    }

    private String leerTextoConPrompt(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : "";
    }

    private int leerEnteroConPrompt(String prompt) {
        System.out.print(prompt);
        String linea = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        try {
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void pausar() {
        System.out.println();
        System.out.print("  Presione ENTER para continuar...");
        if (scanner.hasNextLine()) scanner.nextLine();
    }
}
