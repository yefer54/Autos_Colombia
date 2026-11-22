package ui;

import models.Celda;
import models.Cliente;
import models.MetodoPago;
import models.Pago;
import models.PagoPendiente;
import models.ResultadoPago;
import models.Rol;
import models.Usuario;
import services.ParqueaderoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class Menu {

    private static final String LINEA_DOBLE  = "══════════════════════════════════════════════════════════════════════════════════";
    private static final String LINEA_SIMPLE = "──────────────────────────────────────────────────────────────────────────────────";

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
            if (usuarioActual.getRol() == Rol.ADMIN) {
                salir = menuAdminLoop();
            } else if (usuarioActual.getRol() == Rol.OPERARIO) {
                salir = menuOperarioLoop();
            } else {
                salir = menuClienteLoop();
            }
        }
    }

    private boolean menuAdminLoop() {
        imprimirEncabezado("MENÚ PRINCIPAL — ADMINISTRADOR");
        System.out.println("  Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getRol().name() + ")");
        System.out.println(LINEA_SIMPLE);
        System.out.println("  1. Gestión de Usuarios del Sistema");
        System.out.println("  2. Gestión de Celdas");
        System.out.println("  3. Asignaciones de Celdas");
        System.out.println("  4. Mensualidades / Clientes");
        System.out.println("  5. Gestión de Pagos");
        System.out.println("  6. Iniciar sesión / Cambiar de usuario");
        System.out.println("  0. Salir");
        System.out.println(LINEA_SIMPLE);

        switch (leerEnteroConPrompt("  Seleccione una opción: ")) {
            case 1: menuUsuariosLoop();      break;
            case 2: menuCeldasLoop();        break;
            case 3: menuAsignacionesLoop();  break;
            case 4: menuMensualidadesLoop(); break;
            case 5: menuPagosLoop();         break;
            case 6: cambiarUsuario();        break;
            case 0: return true;
            default: System.out.println("\n  [!] Opción no válida."); pausar();
        }
        return false;
    }

    private boolean menuOperarioLoop() {
        imprimirEncabezado("MENÚ PRINCIPAL — OPERARIO");
        System.out.println("  Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getRol().name() + ")");
        System.out.println(LINEA_SIMPLE);
        System.out.println("  1. Asignaciones de Celdas");
        System.out.println("  2. Mensualidades / Clientes");
        System.out.println("  3. Gestión de Pagos");
        System.out.println("  4. Ver celdas disponibles");
        System.out.println("  5. Iniciar sesión / Cambiar de usuario");
        System.out.println("  0. Salir");
        System.out.println(LINEA_SIMPLE);

        switch (leerEnteroConPrompt("  Seleccione una opción: ")) {
            case 1: menuAsignacionesLoop();   break;
            case 2: menuMensualidadesLoop();  break;
            case 3: menuPagosLoop();          break;
            case 4: flujoCeldasDisponibles(); break;
            case 5: cambiarUsuario();         break;
            case 0: return true;
            default: System.out.println("\n  [!] Opción no válida."); pausar();
        }
        return false;
    }

    private boolean menuClienteLoop() {
        imprimirEncabezado("PORTAL CLIENTE — AUTOS COLOMBIA");
        System.out.println("  Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getRol().name() + ")");
        System.out.println(LINEA_SIMPLE);
        System.out.println("  1. Consultar mi historial de pagos");
        System.out.println("  2. Consultar mis pagos pendientes");
        System.out.println("  3. Realizar pago de mensualidad");
        System.out.println("  4. Consultar / Generar comprobante de pago");
        System.out.println("  5. Iniciar sesión / Cambiar de usuario");
        System.out.println("  0. Salir");
        System.out.println(LINEA_SIMPLE);

        switch (leerEnteroConPrompt("  Seleccione una opción: ")) {
            case 1: flujoConsultarHistorialClienteAutenticado(); break;
            case 2: flujoConsultarPagosPendientesCliente();      break;
            case 3: flujoRealizarPago();                         break;
            case 4: flujoGenerarComprobante();                   break;
            case 5: cambiarUsuario();                            break;
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
            System.out.println("  5. Renovar mensualidad (con pago)");
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


    private void menuPagosLoop() {
        boolean activo = true;
        while (activo) {
            boolean esAdmin = (usuarioActual.getRol() == Rol.ADMIN);
            imprimirEncabezado("GESTIÓN DE PAGOS — " + (esAdmin ? "ADMINISTRADOR" : "OPERARIO"));
            System.out.println("  1. Realizar nuevo pago");
            System.out.println("  2. Consultar historial de pagos");
            System.out.println("  3. Consultar pagos pendientes");
            System.out.println("  4. Generar / Ver comprobante de pago");
            if (esAdmin) {
                System.out.println("  5. Actualizar pago (modificar datos autorizados)");
                System.out.println("  6. Eliminar pago");
                System.out.println("  7. Gestión de métodos de pago");
            }
            System.out.println("  0. Volver");
            System.out.println(LINEA_SIMPLE);

            int op = leerEnteroConPrompt("  Seleccione una opción: ");
            switch (op) {
                case 1: flujoRealizarPago();             break;
                case 2: flujoConsultarHistorialPagos();  break;
                case 3: flujoConsultarPagosPendientes(); break;
                case 4: flujoGenerarComprobante();       break;
                case 5:
                    if (esAdmin) flujoActualizarPago();
                    else { System.out.println("\n  [!] Opción no válida."); pausar(); }
                    break;
                case 6:
                    if (esAdmin) flujoEliminarPago();
                    else { System.out.println("\n  [!] Opción no válida."); pausar(); }
                    break;
                case 7:
                    if (esAdmin) menuGestionMetodosPagoLoop();
                    else { System.out.println("\n  [!] Opción no válida."); pausar(); }
                    break;
                case 0: activo = false; break;
                default: System.out.println("\n  [!] Opción no válida."); pausar();
            }
        }
    }


    private void flujoRealizarPago() {
        flujoRealizarPagoConPlaca("");
    }

    private void flujoRealizarPagoConPlaca(String placaInicial) {
        imprimirEncabezado("REALIZAR PAGO DE SERVICIO DE PARQUEADERO");

        String placa = placaInicial;
        if (placa == null || placa.isBlank()) {
            placa = leerTextoConPrompt("  Placa del vehículo a pagar: ").toUpperCase().trim();
        }

        if (placa.isEmpty()) {
            System.out.println("\n  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Cliente> optCliente = servicio.buscarClientePorPlaca(placa);
        if (optCliente.isEmpty()) {
            System.out.println("\n  [!] No se encontró ningún cliente registrado con la placa: " + placa);
            System.out.println("      Registre primero al cliente en el menú de mensualidades.");
            pausar();
            return;
        }

        Cliente cliente = optCliente.get();
        System.out.println();
        System.out.println("  Información del Cliente y Vehículo:");
        System.out.println("  • Propietario      : " + cliente.getNombrePropietario());
        System.out.println("  • Documento        : " + (cliente.getDocumento().isEmpty() ? "N/A" : cliente.getDocumento()));
        System.out.println("  • Celda Asignada   : N° " + cliente.getNumeroCelda());
        System.out.println("  • Vencimiento Actual: " + cliente.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
        System.out.println("  • Estado           : " + (cliente.isVigente() ? "AL DÍA (VIGENTE)" : "VENCIDO / PENDIENTE"));
        System.out.println(LINEA_SIMPLE);

        // Control de pagos duplicados
        boolean permitirAdelantado = false;
        Optional<Pago> dupOpt = servicio.buscarPagoDuplicado(placa);
        if (dupOpt.isPresent()) {
            Pago pExistente = dupOpt.get();
            System.out.println("\n  [!] ADVERTENCIA — PAGO EXISTENTE IDENTIFICADO (EVITAR DUPLICADOS):");
            System.out.println("      El vehículo ya cuenta con un pago activo para este servicio.");
            System.out.println("      N° Transacción : " + pExistente.getIdTransaccion());
            System.out.println("      Fecha de pago  : " + pExistente.getFechaPago().format(Pago.FMT_DISPLAY));
            System.out.println("      Valor pagado   : " + pExistente.getValorFormateado());
            System.out.println("      Vigente hasta  : " + cliente.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
            System.out.println();
            String confAdelanto = leerTextoConPrompt("  ¿Desea registrar el pago de un mes ADICIONAL por adelantado? (S/N): ").toUpperCase().trim();
            if (!"S".equals(confAdelanto)) {
                System.out.println("\n  ✔  Operación cancelada. Se evitó duplicar el cobro.");
                pausar();
                return;
            }
            permitirAdelantado = true;
        }

        int meses = 1;
        System.out.print("\n  Cantidad de meses a pagar [presione ENTER para 1]: ");
        String mStr = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (!mStr.isEmpty()) {
            try {
                meses = Integer.parseInt(mStr);
                if (meses <= 0) meses = 1;
            } catch (NumberFormatException e) {
                meses = 1;
            }
        }

        double valorCalculado = servicio.calcularValorServicio(placa, meses);
        System.out.printf("  ✔  Valor calculado del servicio: $ %,.0f COP (%d mes(es) x $ %,.0f)%n",
                valorCalculado, meses, ParqueaderoService.TARIFA_MENSUAL_BASE);
        System.out.println(LINEA_SIMPLE);

        System.out.printf("  Ingrese el valor a pagar [ENTER para aceptar $ %,.0f]: ", valorCalculado);
        String vStr = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        double valorIngresado = valorCalculado;
        if (!vStr.isEmpty()) {
            try {
                vStr = vStr.replace("$", "").replace(".", "").replace(",", ".").trim();
                valorIngresado = Double.parseDouble(vStr);
            } catch (NumberFormatException e) {
                valorIngresado = -1.0;
            }
        }

        String errorValor = servicio.validarValorPago(valorIngresado, valorCalculado);
        if (errorValor != null) {
            System.out.println("\n  [!] ERROR DE VALIDACIÓN DE VALOR:");
            System.out.println("      " + errorValor);
            System.out.println("      El sistema rechaza el valor inválido y no realiza la transacción.");
            pausar();
            return;
        }

        System.out.println("\n  MÉTODOS DE PAGO DISPONIBLES:");
        List<MetodoPago> metodos = servicio.listarMetodosPago();
        for (MetodoPago mp : metodos) {
            System.out.printf("   [%-4s] %-32s (%s)%n",
                    mp.getId(), mp.getNombre(), mp.isHabilitado() ? "HABILITADO" : "DESHABILITADO");
        }
        System.out.println(LINEA_SIMPLE);

        String idMetodo = leerTextoConPrompt("  Seleccione el código del método de pago (ej. MP-1): ").trim().toUpperCase();

        String errorMetodo = servicio.validarMetodoPago(idMetodo);
        if (errorMetodo != null) {
            System.out.println("\n  [!] ERROR DE VALIDACIÓN DE MÉTODO DE PAGO:");
            System.out.println("      " + errorMetodo);
            System.out.println("      El sistema impide continuar con el pago e informa que no está disponible.");
            pausar();
            return;
        }

        String obs = leerTextoConPrompt("  Observaciones (opcional): ").trim();

        System.out.println();
        String conf = leerTextoConPrompt(String.format("  ¿Confirma registrar el pago por $ %,.0f? (S/N): ", valorIngresado))
                .toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("\n  Operación cancelada por el usuario.");
            pausar();
            return;
        }

        ResultadoPago resultado = servicio.registrarPago(
                placa, valorIngresado, idMetodo,
                usuarioActual.getNombre(), obs, meses, permitirAdelantado);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (resultado.isExito()) {
            System.out.println("  ✔  " + resultado.getMensaje());
            System.out.println();
            // Generar comprobante
            System.out.println(resultado.getPago().generarComprobante());
        } else {
            System.out.println("  [!] " + resultado.getMensaje());
        }
        pausar();
    }

    private void flujoConsultarHistorialPagos() {
        if (usuarioActual.getRol() == Rol.CLIENTE) {
            flujoConsultarHistorialClienteAutenticado();
            return;
        }

        imprimirEncabezado("CONSULTAR HISTORIAL DE PAGOS");
        System.out.println("  1. Ver todos los pagos del parqueadero (Historial global)");
        System.out.println("  2. Consultar historial de un cliente o vehículo específico");
        System.out.println("  3. Buscar pago por N° de transacción");
        System.out.println("  0. Volver");
        System.out.println(LINEA_SIMPLE);

        int op = leerEnteroConPrompt("  Seleccione: ");
        switch (op) {
            case 1:
                mostrarTablaPagos(servicio.listarPagos(), "HISTORIAL COMPLETO DE PAGOS");
                break;
            case 2:
                String cr = leerTextoConPrompt("  Ingrese placa o documento del cliente: ").trim();
                if (cr.isEmpty()) {
                    System.out.println("  [!] Debe ingresar un criterio de búsqueda.");
                    pausar();
                    return;
                }
                List<Pago> lista = servicio.listarPagosPorCliente(cr);
                if (lista.isEmpty()) {
                    System.out.println("\n  [i] No se encontraron pagos registrados para: " + cr);
                    pausar();
                } else {
                    mostrarTablaPagos(lista, "HISTORIAL DE PAGOS — " + cr.toUpperCase());
                }
                break;
            case 3:
                String idTrans = leerTextoConPrompt("  Ingrese N° de Transacción (ej. PAG-0001): ").trim();
                Optional<Pago> opt = servicio.buscarPagoPorId(idTrans);
                if (opt.isPresent()) {
                    System.out.println();
                    System.out.println(opt.get().generarComprobante());
                    pausar();
                } else {
                    System.out.println("\n  [!] No se encontró ningún pago con la transacción: " + idTrans);
                    pausar();
                }
                break;
            case 0:
                break;
            default:
                System.out.println("  [!] Opción no válida.");
                pausar();
        }
    }

    private void flujoConsultarHistorialClienteAutenticado() {
        imprimirEncabezado("MI HISTORIAL DE PAGOS");
        List<Pago> misPagos = servicio.listarPagosPorCliente(usuarioActual.getDocumento());
        if (misPagos.isEmpty()) {
            misPagos = servicio.listarPagosPorCliente(usuarioActual.getNombre());
        }

        if (misPagos.isEmpty()) {
            System.out.println("  [i] El usuario no tiene pagos registrados.");
            pausar();
            return;
        }

        mostrarTablaPagos(misPagos, "MIS PAGOS REALIZADOS");
    }

    private void flujoConsultarPagosPendientesCliente() {
        imprimirEncabezado("MIS PAGOS PENDIENTES");
        List<PagoPendiente> todos = servicio.consultarPagosPendientes();
        List<PagoPendiente> mios = new ArrayList<>();
        for (PagoPendiente p : todos) {
            if (p.getNombreCliente().equalsIgnoreCase(usuarioActual.getNombre())) {
                mios.add(p);
            }
        }

        if (mios.isEmpty()) {
            System.out.println("  ✔  ¡Se encuentra al día! No tiene pagos pendientes.");
            pausar();
            return;
        }

        mostrarTablaPagosPendientes(mios);
    }

    private void flujoConsultarPagosPendientes() {
        imprimirEncabezado("CONSULTAR PAGOS PENDIENTES");

        List<PagoPendiente> pendientes = servicio.consultarPagosPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("  ✔  No hay mensualidades pendientes. Todos los clientes están al día.");
            pausar();
            return;
        }

        mostrarTablaPagosPendientes(pendientes);

        System.out.println();
        String placa = leerTextoConPrompt("  ¿Desea realizar el pago de alguna de estas placas? (Ingrese la placa o ENTER para volver): ")
                .toUpperCase().trim();
        if (!placa.isEmpty()) {
            flujoRealizarPagoConPlaca(placa);
        }
    }

    private void mostrarTablaPagosPendientes(List<PagoPendiente> lista) {
        System.out.println();
        System.out.printf("  %-8s │ %-20s │ %-9s │ %-12s │ %-10s │ %-14s │ %-15s%n",
                "PLACA", "CLIENTE", "CELDA", "VENCE", "RETRASO", "VALOR PEND.", "ESTADO");
        System.out.println("  " + "─".repeat(98));

        double total = 0;
        for (PagoPendiente p : lista) {
            System.out.println(p.toString());
            total += p.getValorPendiente();
        }
        System.out.println("  " + "─".repeat(98));
        System.out.printf("  Total clientes pendientes: %d  |  Total cartera por cobrar: $ %,.0f COP%n",
                lista.size(), total);
        pausar();
    }


    private void flujoGenerarComprobante() {
        imprimirEncabezado("GENERAR COMPROBANTE DE PAGO");

        String idTrans = leerTextoConPrompt("  Ingrese el N° de Transacción (ej. PAG-0001): ").trim().toUpperCase();
        if (idTrans.isEmpty()) {
            System.out.println("  [!] Debe ingresar un número de transacción.");
            pausar();
            return;
        }

        Optional<String> compOpt = servicio.generarComprobantePago(idTrans);
        if (compOpt.isPresent()) {
            System.out.println();
            System.out.println(compOpt.get());
        } else {
            System.out.println("\n  [!] No se encontró ningún pago con la transacción: " + idTrans);
        }
        pausar();
    }


    private void flujoActualizarPago() {
        imprimirEncabezado("ACTUALIZAR PAGO (DATOS AUTORIZADOS)");

        String idTrans = leerTextoConPrompt("  N° de Transacción del pago a actualizar: ").trim().toUpperCase();
        if (idTrans.isEmpty()) {
            System.out.println("  [!] Debe ingresar el N° de transacción.");
            pausar();
            return;
        }

        Optional<Pago> opt = servicio.buscarPagoPorId(idTrans);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró pago con N°: " + idTrans);
            pausar();
            return;
        }

        Pago p = opt.get();
        System.out.println("\n  Datos actuales del pago:");
        System.out.println("  • N° Transacción : " + p.getIdTransaccion());
        System.out.println("  • Placa          : " + p.getPlacaVehiculo());
        System.out.println("  • Cliente        : " + p.getNombreCliente());
        System.out.println("  • Fecha          : " + p.getFechaPago().format(Pago.FMT_DISPLAY));
        System.out.println("  • Valor          : " + p.getValorFormateado());
        System.out.println("  • Método actual  : " + p.getMetodoPagoNombre() + " (" + p.getMetodoPagoId() + ")");
        System.out.println("  • Estado actual  : " + p.getEstado());
        System.out.println("  • Observaciones  : " + p.getObservaciones());
        System.out.println(LINEA_SIMPLE);
        System.out.println("  (Presione ENTER en cualquier campo para conservar el valor actual)");

        System.out.println("\n  Métodos de pago disponibles:");
        for (MetodoPago mp : servicio.listarMetodosPagoHabilitados()) {
            System.out.printf("   [%s] %s%n", mp.getId(), mp.getNombre());
        }
        String nuevoMetodo = leerTextoConPrompt("  Nuevo método de pago (ID, ej. MP-1): ").trim().toUpperCase();

        System.out.println("  Estados válidos: APROBADO, PENDIENTE, ANULADO");
        String nuevoEstado = leerTextoConPrompt("  Nuevo estado [" + p.getEstado() + "]: ").trim().toUpperCase();

        String nuevasObs = leerTextoConPrompt("  Nuevas observaciones: ").trim();

        if (nuevoMetodo.isEmpty() && nuevoEstado.isEmpty() && nuevasObs.isEmpty()) {
            System.out.println("\n  No se ingresaron cambios. Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.actualizarPago(
                idTrans,
                nuevoMetodo.isEmpty() ? null : nuevoMetodo,
                nuevoEstado.isEmpty() ? null : nuevoEstado,
                nuevasObs.isEmpty() ? null : nuevasObs);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Pago actualizado correctamente en el sistema.");
            servicio.buscarPagoPorId(idTrans).ifPresent(act -> {
                System.out.println("     Nuevo método : " + act.getMetodoPagoNombre());
                System.out.println("     Nuevo estado : " + act.getEstado());
                System.out.println("     Observaciones: " + act.getObservaciones());
            });
        } else {
            System.out.println("  [!] No se pudo actualizar el pago. Verifique que los datos sean válidos y el método esté habilitado.");
        }
        pausar();
    }

    private void flujoEliminarPago() {
        imprimirEncabezado("ELIMINAR PAGO DEL SISTEMA");

        String idTrans = leerTextoConPrompt("  N° de Transacción del pago a eliminar: ").trim().toUpperCase();
        if (idTrans.isEmpty()) {
            System.out.println("  [!] Debe ingresar el N° de transacción.");
            pausar();
            return;
        }

        Optional<Pago> opt = servicio.buscarPagoPorId(idTrans);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró ningún pago con la transacción: " + idTrans);
            pausar();
            return;
        }

        Pago p = opt.get();
        System.out.println("\n  Pago a eliminar:");
        System.out.printf("  Transacción : %s  |  Placa: %s  |  Valor: %s  |  Fecha: %s%n",
                p.getIdTransaccion(), p.getPlacaVehiculo(), p.getValorFormateado(),
                p.getFechaPago().format(Pago.FMT_DISPLAY));

        String conf = leerTextoConPrompt("\n  ¿Está SEGURO de eliminar este pago? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.eliminarPago(idTrans);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Pago " + idTrans + " eliminado exitosamente del sistema.");
        } else {
            System.out.println("  [!] No se pudo eliminar el pago.");
        }
        pausar();
    }


    private void menuGestionMetodosPagoLoop() {
        boolean activo = true;
        while (activo) {
            imprimirEncabezado("GESTIÓN DE MÉTODOS DE PAGO — ADMINISTRADOR");
            mostrarTablaMetodosPago();

            System.out.println("  1. Agregar nuevo método de pago");
            System.out.println("  2. Habilitar método de pago");
            System.out.println("  3. Deshabilitar método de pago");
            System.out.println("  0. Volver a Gestión de Pagos");
            System.out.println(LINEA_SIMPLE);

            switch (leerEnteroConPrompt("  Seleccione: ")) {
                case 1: flujoAgregarMetodoPago();           break;
                case 2: flujoCambiarEstadoMetodoPago(true);  break;
                case 3: flujoCambiarEstadoMetodoPago(false); break;
                case 0: activo = false; break;
                default: System.out.println("\n  [!] Opción no válida."); pausar();
            }
        }
    }

    private void mostrarTablaMetodosPago() {
        List<MetodoPago> lista = servicio.listarMetodosPago();
        System.out.println();
        System.out.printf("  %-6s │  %-28s │  %-14s │  %s%n",
                "CÓDIGO", "MÉTODO DE PAGO", "ESTADO", "DESCRIPCIÓN");
        System.out.println("  " + "─".repeat(84));
        for (MetodoPago mp : lista) {
            System.out.println(mp.toString());
        }
        System.out.println("  " + "─".repeat(84));
        System.out.println();
    }

    private void flujoAgregarMetodoPago() {
        imprimirEncabezado("AGREGAR NUEVO MÉTODO DE PAGO");

        String nombre = leerTextoConPrompt("  Nombre del método de pago: ").trim();
        String desc   = leerTextoConPrompt("  Descripción               : ").trim();

        if (nombre.isEmpty()) {
            System.out.println("\n  [!] El nombre del método de pago es obligatorio.");
            pausar();
            return;
        }

        boolean ok = servicio.agregarMetodoPago(nombre, desc, true);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.println("  ✔  Método de pago agregado exitosamente.");
        } else {
            System.out.println("  [!] Ya existe un método de pago con ese nombre.");
        }
        pausar();
    }


    private void flujoCambiarEstadoMetodoPago(boolean habilitar) {
        String accion = habilitar ? "HABILITAR" : "DESHABILITAR";
        imprimirEncabezado(accion + " MÉTODO DE PAGO");

        String id = leerTextoConPrompt("  Código del método (ej. MP-1): ").trim().toUpperCase();
        if (id.isEmpty()) {
            System.out.println("  [!] Debe ingresar el código del método.");
            pausar();
            return;
        }

        Optional<MetodoPago> opt = servicio.buscarMetodoPagoPorId(id);
        if (opt.isEmpty()) {
            System.out.println("\n  [!] No se encontró ningún método de pago con código: " + id);
            pausar();
            return;
        }

        MetodoPago mp = opt.get();
        boolean ok = servicio.cambiarEstadoMetodoPago(id, habilitar);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            System.out.printf("  ✔  Método de pago [%s - %s] ha sido %s exitosamente.%n",
                    mp.getId(), mp.getNombre(), (habilitar ? "HABILITADO" : "DESHABILITADO"));
            if (!habilitar) {
                System.out.println("      A partir de ahora no podrá ser seleccionado para realizar pagos.");
            }
        } else {
            System.out.println("  [!] No se pudo cambiar el estado del método de pago.");
        }
        pausar();
    }

    private void mostrarTablaPagos(List<Pago> lista, String titulo) {
        imprimirEncabezado(titulo);
        if (lista.isEmpty()) {
            System.out.println("  No hay pagos registrados.");
            pausar();
            return;
        }

        System.out.println();
        System.out.printf("  %-10s │ %-16s │ %-8s │ %-15s │ %-18s │ %-10s%n",
                "N° TRANS.", "FECHA Y HORA", "PLACA", "VALOR", "MÉTODO DE PAGO", "ESTADO");
        System.out.println("  " + "─".repeat(88));

        double total = 0;
        for (Pago p : lista) {
            System.out.println(p.toString());
            if ("APROBADO".equalsIgnoreCase(p.getEstado())) {
                total += p.getValor();
            }
        }
        System.out.println("  " + "─".repeat(88));
        System.out.printf("  Total transacciones: %d  |  Total recaudado aprobado: $ %,.0f COP%n",
                lista.size(), total);
        pausar();
    }



    private void flujoRegistrarUsuario() {
        imprimirEncabezado("REGISTRAR USUARIO DEL SISTEMA");
        System.out.println("  Todos los campos son obligatorios.");
        System.out.println(LINEA_SIMPLE);

        String documento  = leerTextoConPrompt("  Documento    : ").trim();
        String nombre     = leerTextoConPrompt("  Nombre       : ").trim();
        String correo     = leerTextoConPrompt("  Correo       : ").trim();
        String contrasena = leerTextoConPrompt("  Contraseña   : ").trim();
        String telefono   = leerTextoConPrompt("  Teléfono     : ").trim();
        System.out.println("  Rol: 1) ADMIN   2) OPERARIO   3) CLIENTE");
        int    rolOp      = leerEnteroConPrompt("  Rol           : ");
        Rol    rol        = rolOp == 1 ? Rol.ADMIN : rolOp == 2 ? Rol.OPERARIO : rolOp == 3 ? Rol.CLIENTE : null;

        List<String> faltantes = new ArrayList<>();
        if (documento.isEmpty())  faltantes.add("Documento");
        if (nombre.isEmpty())     faltantes.add("Nombre");
        if (correo.isEmpty())     faltantes.add("Correo");
        if (contrasena.isEmpty()) faltantes.add("Contraseña");
        if (telefono.isEmpty())   faltantes.add("Teléfono");
        if (rol == null)          faltantes.add("Rol (selección inválida, elija 1, 2 o 3)");

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
            System.out.println("\n  [!] No puede desactivar su propio usuario mientras tiene la sesión activa.");
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
        System.out.println(ok
            ? "  ✔  Usuario desactivado correctamente. No podrá iniciar sesión."
            : "  [!] No se pudo desactivar el usuario.");
        pausar();
    }

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
        System.out.println("  Seleccione el nuevo rol:");
        System.out.println("  1) ADMIN   2) OPERARIO   3) CLIENTE");
        int opcion = leerEnteroConPrompt("  Nuevo rol: ");

        Rol nuevoRol = opcion == 1 ? Rol.ADMIN : opcion == 2 ? Rol.OPERARIO : opcion == 3 ? Rol.CLIENTE : null;
        if (nuevoRol == null) {
            System.out.println("\n  [!] Opción de rol inválida.");
            pausar();
            return;
        }

        if (nuevoRol == u.getRol()) {
            System.out.println("\n  El usuario ya posee el rol: " + nuevoRol.name());
            pausar();
            return;
        }

        boolean ok = servicio.asignarRol(documento, nuevoRol);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Rol actualizado a " + nuevoRol.name() + " para el usuario " + u.getNombre() + "."
            : "  [!] No se pudo actualizar el rol.");
        pausar();
    }

    private void flujoRegistrarCelda() {
        imprimirEncabezado("REGISTRAR NUEVA CELDA");

        String codigo = leerTextoConPrompt("  Código de la celda (ej. A1, B2): ").trim().toUpperCase();
        if (codigo.isEmpty()) {
            System.out.println("  [!] Debe ingresar un código.");
            pausar();
            return;
        }

        if (servicio.buscarCeldaPorCodigo(codigo).isPresent()) {
            System.out.println("\n  [!] Ya existe una celda con el código: " + codigo);
            pausar();
            return;
        }

        boolean ok = servicio.registrarCelda(codigo);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "  ✔  Celda [" + codigo + "] registrada exitosamente."
            : "  [!] No se pudo registrar la celda.");
        pausar();
    }

    private void flujoConsultarCeldas() {
        imprimirEncabezado("LISTA DE CELDAS");

        List<Celda> celdas = servicio.listarCeldas();
        if (celdas.isEmpty()) {
            System.out.println("  No hay celdas registradas.");
            pausar();
            return;
        }

        System.out.println();
        System.out.println("  CÓDIGO │  ESTADO        │  VEHÍCULO");
        System.out.println("  " + "─".repeat(38));
        for (Celda c : celdas) {
            System.out.println(c.toString());
        }
        System.out.println("  " + "─".repeat(38));

        long libres       = celdas.stream().filter(c -> c.isHabilitada() && c.isDisponible()).count();
        long ocupadas     = celdas.stream().filter(c -> c.isHabilitada() && !c.isDisponible()).count();
        long deshab       = celdas.stream().filter(c -> !c.isHabilitada()).count();

        System.out.printf("  Total: %d  │  Libres: %d  │  Ocupadas: %d  │  Deshabilitadas: %d%n",
                celdas.size(), libres, ocupadas, deshab);
        pausar();
    }

    private void flujoActualizarCelda() {
        imprimirEncabezado("ACTUALIZAR CELDA");

        String codigo = leerTextoConPrompt("  Código actual de la celda: ").toUpperCase().trim();
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
        System.out.println("\n  Celda encontrada: " + c.getCodigo()
                + "  [" + (c.isHabilitada() ? "HABILITADA" : "DESHABILITADA") + "]");
        System.out.println("  Deje en blanco para conservar el valor actual.");

        String nuevoCodigo = leerTextoConPrompt("  Nuevo código: ").trim().toUpperCase();

        System.out.println("  Estado: 1) Habilitada   2) Deshabilitada   [ENTER para conservar]");
        String estadoStr = leerTextoConPrompt("  Opción: ").trim();
        Boolean nuevoEstado = null;
        if ("1".equals(estadoStr)) nuevoEstado = true;
        else if ("2".equals(estadoStr)) nuevoEstado = false;

        if (nuevoCodigo.isEmpty() && nuevoEstado == null) {
            System.out.println("\n  No se realizaron cambios.");
            pausar();
            return;
        }

        boolean ok = servicio.actualizarCelda(codigo,
            nuevoCodigo.isEmpty() ? null : nuevoCodigo, nuevoEstado);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "   Celda actualizada correctamente."
            : "  No se pudo actualizar la celda (el nuevo código puede estar en uso).");
        pausar();
    }

    private void flujoCeldasDisponibles() {
        imprimirEncabezado("CELDAS DISPONIBLES");

        List<Celda> disponibles = servicio.listarCeldasDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("  No hay celdas disponibles en este momento.");
            pausar();
            return;
        }

        System.out.println();
        System.out.println("  CÓDIGO │  ESTADO        │  VEHÍCULO");
        System.out.println("  " + "─".repeat(38));
        for (Celda c : disponibles) {
            System.out.println(c.toString());
        }
        System.out.println("  " + "─".repeat(38));
        System.out.println("  Total disponibles: " + disponibles.size());
        pausar();
    }

    private void flujoDeshabilitarCelda() {
        imprimirEncabezado("DESHABILITAR CELDA");

        String codigo = leerTextoConPrompt("  Código de la celda a deshabilitar: ").toUpperCase().trim();
        if (codigo.isEmpty()) {
            System.out.println("   Debe ingresar un código.");
            pausar();
            return;
        }

        Optional<Celda> opt = servicio.buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("\n  No se encontró celda con código: " + codigo);
            pausar();
            return;
        }

        Celda c = opt.get();
        if (!c.isHabilitada()) {
            System.out.println("\n   La celda ya se encuentra DESHABILITADA.");
            pausar();
            return;
        }

        if (!c.isDisponible()) {
            System.out.println("\n   No se puede deshabilitar la celda: tiene un vehículo asignado ("
                    + c.getPlacaVehiculo() + ").");
            System.out.println("      Libere la celda antes de deshabilitarla.");
            pausar();
            return;
        }

        String conf = leerTextoConPrompt("\n  ¿Confirma deshabilitar la celda " + codigo + "? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.deshabilitarCelda(codigo);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "    Celda [" + codigo + "] deshabilitada. No podrá recibir asignaciones."
            : "   No se pudo deshabilitar la celda.");
        pausar();
    }

    private void flujoAsignarCelda() {
        imprimirEncabezado("ASIGNAR CELDA A VEHÍCULO");

        if (!servicio.hayCeldasDisponibles()) {
            System.out.println("  No hay celdas disponibles para asignar.");
            pausar();
            return;
        }

        String placa = leerTextoConPrompt("  Placa del vehículo: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("   Debe ingresar una placa.");
            pausar();
            return;
        }

        System.out.println("\n  Celdas disponibles:");
        List<Celda> disponibles = servicio.listarCeldasDisponibles();
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < disponibles.size(); i++) {
            sb.append(disponibles.get(i).getCodigo());
            if (i < disponibles.size() - 1) sb.append(", ");
        }
        System.out.println(sb);
        System.out.println();

        String codigo = leerTextoConPrompt("  Código de la celda a asignar: ").toUpperCase().trim();
        if (codigo.isEmpty()) {
            System.out.println("   Debe ingresar el código de la celda.");
            pausar();
            return;
        }

        boolean ok = servicio.asignarCelda(codigo, placa);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "    Celda [" + codigo + "] asignada exitosamente al vehículo [" + placa + "]."
            : "  No se pudo asignar la celda. Verifique que exista, esté habilitada y libre.");
        pausar();
    }

    private void flujoLiberarCelda() {
        imprimirEncabezado("LIBERAR CELDA");

        String codigo = leerTextoConPrompt("  Código de la celda a liberar: ").toUpperCase().trim();
        if (codigo.isEmpty()) {
            System.out.println("   Debe ingresar un código.");
            pausar();
            return;
        }

        Optional<Celda> opt = servicio.buscarCeldaPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("\n  No se encontró celda con código: " + codigo);
            pausar();
            return;
        }

        Celda c = opt.get();
        if (c.isDisponible()) {
            System.out.println("\n  La celda [" + codigo + "] ya está libre.");
            pausar();
            return;
        }

        System.out.println("\n  Celda [" + codigo + "] — Vehículo asignado: " + c.getPlacaVehiculo());
        String conf = leerTextoConPrompt("  ¿Confirma la liberación? (S/N): ").toUpperCase().trim();
        if (!"S".equals(conf)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = servicio.liberarCelda(codigo);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        System.out.println(ok
            ? "   Celda [" + codigo + "] liberada exitosamente."
            : "  No se pudo liberar la celda.");
        pausar();
    }

    private void flujoRegistrarCliente() {
        imprimirEncabezado("REGISTRAR NUEVO CLIENTE (MENSUALIDAD)");

        if (!servicio.hayCeldasDisponibles()) {
            System.out.println("   No hay celdas disponibles. El parqueadero está lleno.");
            pausar();
            return;
        }

        String placa     = leerTextoConPrompt("  Placa del vehículo     : ").toUpperCase().trim();
        String nombre    = leerTextoConPrompt("  Nombre del propietario : ").trim();
        String documento = leerTextoConPrompt("  Documento de identidad : ").trim();
        String telefono  = leerTextoConPrompt("  Teléfono de contacto   : ").trim();

        if (placa.isEmpty() || nombre.isEmpty()) {
            System.out.println("\n   La placa y el nombre son obligatorios.");
            pausar();
            return;
        }

        if (servicio.buscarClientePorPlaca(placa).isPresent()) {
            System.out.println("\n   La placa '" + placa + "' ya está registrada en el sistema.");
            pausar();
            return;
        }

        boolean ok = servicio.registrarNuevoCliente(placa, nombre, documento, telefono);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (ok) {
            Optional<Cliente> reg = servicio.buscarClientePorPlaca(placa);
            System.out.println("    Cliente registrado exitosamente.");
            reg.ifPresent(c -> {
                System.out.println("     Celda N°: " + c.getNumeroCelda()
                    + "  |  Vencimiento: " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
            });

            String pagar = leerTextoConPrompt("\n  ¿Desea realizar el pago de la primera mensualidad ahora? (S/N): ").toUpperCase().trim();
            if ("S".equals(pagar)) {
                flujoRealizarPagoConPlaca(placa);
                return;
            }
        } else {
            System.out.println("   No se pudo registrar el cliente.");
        }
        pausar();
    }

    private void flujoValidarIngreso() {
        imprimirEncabezado("VALIDAR INGRESO DE VEHÍCULO");

        String placa = leerTextoConPrompt("  Placa del vehículo: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("   Placa no puede estar vacía.");
            pausar();
            return;
        }

        System.out.println();
        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        if (opt.isEmpty()) {
            System.out.println("   VEHÍCULO NO REGISTRADO");
            System.out.println("     La placa [" + placa + "] no cuenta con suscripción en el sistema.");
            pausar();
            return;
        }

        Cliente c = opt.get();
        if (c.isVigente()) {
            System.out.println("    INGRESO PERMITIDO");
            System.out.println("     Propietario : " + c.getNombrePropietario());
            System.out.println("     Celda N°    : " + c.getNumeroCelda());
            System.out.println("     Vence el    : " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
            System.out.println("     Estado      : AL DÍA");
        } else {
            System.out.println("    INGRESO DENEGADO — MENSUALIDAD VENCIDA");
            System.out.println("     Propietario : " + c.getNombrePropietario());
            System.out.println("     Venció el   : " + c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));
            System.out.println("     Por favor renueve su mensualidad realizando el pago.");
        }
        pausar();
    }

    private void flujoConsultarClientes() {
        imprimirEncabezado("LISTA DE CLIENTES / MENSUALIDADES");

        List<Cliente> clientes = servicio.listarClientes();
        if (clientes.isEmpty()) {
            System.out.println("  No hay clientes registrados.");
            pausar();
            return;
        }

        System.out.println();
        String fmt = "  %-8s │ %-20s │ %-12s │ %-6s │ %-16s │ %-10s";
        System.out.printf(fmt, "PLACA", "PROPIETARIO", "TELÉFONO", "CELDA", "VENCIMIENTO", "ESTADO");
        System.out.println();
        System.out.println("  " + "─".repeat(90));

        for (Cliente c : clientes) {
            String nom = c.getNombrePropietario().length() > 19 ? c.getNombrePropietario().substring(0, 19) : c.getNombrePropietario();
            String ven = c.getFechaVencimiento() != null ? c.getFechaVencimiento().format(Cliente.FMT_DISPLAY) : "N/A";
            System.out.printf(fmt,
                c.getPlaca(), nom, c.getTelefono(),
                "N° " + c.getNumeroCelda(), ven,
                c.isVigente() ? "ACTIVO" : "VENCIDO");
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
            System.out.println("   Debe ingresar una placa.");
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
            System.out.println("   No se encontró ningún cliente con placa: " + placa);
        }
        pausar();
    }

    private void flujoRenovarCliente() {
        imprimirEncabezado("RENOVAR MENSUALIDAD");

        String placa = leerTextoConPrompt("  Placa del vehículo: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("   Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        if (opt.isEmpty()) {
            System.out.println("\n   No se encontró cliente con placa: " + placa);
            pausar();
            return;
        }

        Cliente c = opt.get();
        System.out.println("\n  Cliente encontrado:");
        System.out.printf("     Propietario  : %s%n", c.getNombrePropietario());
        System.out.printf("     Estado actual: %s%n", c.isVigente() ? "ACTIVO" : "INACTIVO / VENCIDO");
        System.out.printf("     Vencimiento  : %s%n", c.getFechaVencimiento().format(Cliente.FMT_DISPLAY));

        System.out.println("\n  Opciones de renovación:");
        System.out.println("  1. Registrar pago del servicio (Recomendado — genera comprobante)");
        System.out.println("  2. Extensión manual sin pago formal");
        System.out.println("  0. Cancelar");
        int op = leerEnteroConPrompt("  Seleccione: ");

        if (op == 1) {
            flujoRealizarPagoConPlaca(placa);
        } else if (op == 2) {
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
                System.out.println("    Mensualidad renovada exitosamente.");
                renovado.ifPresent(r ->
                    System.out.println("     Nuevo vencimiento: " + r.getFechaVencimiento().format(Cliente.FMT_DISPLAY))
                );
            } else {
                System.out.println("   No se pudo renovar la mensualidad.");
            }
            pausar();
        } else {
            System.out.println("  Operación cancelada.");
            pausar();
        }
    }

    private void flujoEliminarCliente() {
        imprimirEncabezado("ELIMINAR CLIENTE");

        String placa = leerTextoConPrompt("  Placa del cliente a eliminar: ").toUpperCase().trim();
        if (placa.isEmpty()) {
            System.out.println("   Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Cliente> opt = servicio.buscarClientePorPlaca(placa);
        if (opt.isEmpty()) {
            System.out.println("\n   No se encontró cliente con placa: " + placa);
            pausar();
            return;
        }

        Cliente c = opt.get();
        System.out.println("\n  Cliente encontrado:");
        System.out.println(c.toString());

        if (c.isVigente()) {
            System.out.println("\n  No se puede eliminar: la mensualidad aún está VIGENTE.");
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
            ? "    Cliente [" + placa + "] eliminado. La celda ha sido liberada."
            : "  No se pudo eliminar el cliente.");
        pausar();
    }

    private void mostrarBienvenida() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                         PARQUEADERO AUTOS COLOMBIA                           ║");
        System.out.println("  ║                Sistema de Gestión de Parqueadero                        ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════════════════════╝");
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
