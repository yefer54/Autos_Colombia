package ui;

import models.Celda;
import models.Usuario;
import services.ParqueaderoService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Menu {

    private static final String LINEA_DOBLE  = "══════════════════════════════════════════";
    private static final String LINEA_SIMPLE = "------------------------------------------";

    private final ParqueaderoService servicio;

    private final Scanner scanner;

    public Menu(ParqueaderoService servicio) {
        this.servicio = servicio;
        this.scanner  = new Scanner(System.in, "UTF-8");
    }

    public void iniciar() {
        int opcion = -1;
        do {
            mostrarMenuPrincipal();
            opcion = leerEnteroConPrompt("  Seleccione una opción: ");
            ejecutarOpcion(opcion);
        } while (opcion != 0);
    }

    public void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║    PARQUEADERO AUTOS COLOMBIA        ║");
        System.out.println("║         Sistema de Gestión           ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  1. Registrar nuevo vehículo         ║");
        System.out.println("║  2. Validar ingreso de vehículo      ║");
        System.out.println("║  3. Consultar lista de usuarios      ║");
        System.out.println("║  4. Buscar vehículo por placa        ║");
        System.out.println("║  5. Eliminar usuario                 ║");
        System.out.println("║  6. Ver estado de celdas             ║");
        System.out.println("║  7. Renovar mensualidad              ║");
        System.out.println("║  0. Salir                            ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    public void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1:
                flujoRegistroNuevoUsuario();
                break;
            case 2:
                flujoValidarIngreso();
                break;
            case 3:
                flujoConsultarUsuarios();
                break;
            case 4:
                flujoBuscarPorPlaca();
                break;
            case 5:
                flujoEliminarUsuario();
                break;
            case 6:
                flujoVerCeldas();
                break;
            case 7:
                flujoRenovarUsuario();
                break;
            case 0:
                mostrarDespedida();
                break;
            default:
                System.out.println("\n  [!] Opción no válida. Intente nuevamente.");
                break;
        }
    }

    public void flujoRegistroNuevoUsuario() {
        imprimirEncabezado("REGISTRAR NUEVO VEHÍCULO");

        if (!servicio.hayCeldasDisponibles()) {
            System.out.println("  [!] No hay celdas disponibles. El parqueadero está lleno.");
            pausar();
            return;
        }

        System.out.println("  Ingrese los datos del vehículo:");
        System.out.println(LINEA_SIMPLE);

        String placa    = leerTextoConPrompt("  Placa del vehículo    : ").toUpperCase().trim();
        String nombre   = leerTextoConPrompt("  Nombre del propietario: ");
        String telefono = leerTextoConPrompt("  Teléfono de contacto  : ");

        if (placa.isEmpty() || nombre.isEmpty()) {
            System.out.println("\n  [!] La placa y el nombre son obligatorios. Operación cancelada.");
            pausar();
            return;
        }

        boolean exito = servicio.registrarNuevoUsuario(placa, nombre, telefono);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (exito) {
            Optional<Usuario> registrado = servicio.buscarPorPlaca(placa);
            System.out.println("  ✔  Vehículo registrado exitosamente.");
            registrado.ifPresent(u ->
                System.out.println("     Celda asignada: " + u.getNumeroCelda()
                    + "  |  Vencimiento: " + u.getFechaVencimiento().format(Usuario.FMT_DISPLAY))
            );
        } else {
            System.out.println("  [!] No se pudo registrar. La placa ya existe en el sistema.");
        }
        pausar();
    }

    public void flujoValidarIngreso() {
        imprimirEncabezado("VALIDAR INGRESO DE VEHÍCULO");

        String placa = leerTextoConPrompt("  Ingrese la placa del vehículo: ").toUpperCase().trim();

        if (placa.isEmpty()) {
            System.out.println("  [!] Placa no puede estar vacía.");
            pausar();
            return;
        }

        System.out.println();
        System.out.println(LINEA_SIMPLE);

        Optional<Usuario> usuarioOpt = servicio.buscarPorPlaca(placa);

        if (usuarioOpt.isEmpty()) {
            System.out.println("  ✘  Placa [" + placa + "] NO encontrada en el sistema.");
            System.out.println("     El vehículo no está registrado en el parqueadero.");
        } else {
            Usuario u = usuarioOpt.get();
            if (u.isVigente()) {
                System.out.println("  ✔  ACCESO PERMITIDO");
                System.out.println("     Placa        : " + u.getPlaca());
                System.out.println("     Propietario  : " + u.getNombrePropietario());
                System.out.println("     Celda        : " + u.getNumeroCelda());
                System.out.println("     Vence        : " + u.getFechaVencimiento().format(Usuario.FMT_DISPLAY));
            } else {
                System.out.println("  ✘  ACCESO DENEGADO — Mensualidad vencida.");
                System.out.println("     Placa        : " + u.getPlaca());
                System.out.println("     Propietario  : " + u.getNombrePropietario());
                System.out.println("     Venció el    : " + u.getFechaVencimiento().format(Usuario.FMT_DISPLAY));
                System.out.println("     Por favor renueve su mensualidad (opción 7).");
            }
        }
        pausar();
    }

    public void flujoConsultarUsuarios() {
        imprimirEncabezado("LISTA DE USUARIOS REGISTRADOS");

        List<Usuario> usuarios = servicio.listarUsuarios();

        if (usuarios.isEmpty()) {
            System.out.println("  No hay usuarios registrados en el sistema.");
            pausar();
            return;
        }

        System.out.println();
        String fmtEncabezado = "  %-10s %-20s %-6s %-17s %-17s %-8s";
        String fmtFila       = "  %-10s %-20s %-6d %-17s %-17s %-8s";

        System.out.printf(fmtEncabezado,
            "PLACA", "PROPIETARIO", "CELDA", "INGRESO", "VENCIMIENTO", "ESTADO");
        System.out.println();
        System.out.println("  " + "─".repeat(84));

        for (Usuario u : usuarios) {
            String ingreso     = u.getFechaIngreso()     != null ? u.getFechaIngreso().format(Usuario.FMT_DISPLAY)     : "N/A";
            String vencimiento = u.getFechaVencimiento() != null ? u.getFechaVencimiento().format(Usuario.FMT_DISPLAY) : "N/A";
            String estado      = u.isVigente() ? "ACTIVO" : "INACTIVO";
            String nombre      = u.getNombrePropietario().length() > 19
                                 ? u.getNombrePropietario().substring(0, 19)
                                 : u.getNombrePropietario();

            System.out.printf(fmtFila,
                u.getPlaca(), nombre, u.getNumeroCelda(), ingreso, vencimiento, estado);
            System.out.println();
        }

        System.out.println("  " + "─".repeat(84));
        System.out.println("  Total de usuarios: " + usuarios.size());
        pausar();
    }

    public void flujoBuscarPorPlaca() {
        imprimirEncabezado("BUSCAR VEHÍCULO POR PLACA");

        String placa = leerTextoConPrompt("  Ingrese la placa a buscar: ").toUpperCase().trim();

        if (placa.isEmpty()) {
            System.out.println("  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Usuario> usuarioOpt = servicio.buscarPorPlaca(placa);

        System.out.println();
        if (usuarioOpt.isPresent()) {
            System.out.println("  Información del vehículo encontrado:");
            System.out.println();
            System.out.println(usuarioOpt.get().toString());
        } else {
            System.out.println("  [!] No se encontró ningún vehículo con la placa: " + placa);
        }
        pausar();
    }

    public void flujoEliminarUsuario() {
        imprimirEncabezado("ELIMINAR USUARIO");

        String placa = leerTextoConPrompt("  Ingrese la placa del usuario a eliminar: ").toUpperCase().trim();

        if (placa.isEmpty()) {
            System.out.println("  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Usuario> usuarioOpt = servicio.buscarPorPlaca(placa);

        if (usuarioOpt.isEmpty()) {
            System.out.println("\n  [!] No se encontró ningún usuario con la placa: " + placa);
            pausar();
            return;
        }

        Usuario usuario = usuarioOpt.get();
        System.out.println("\n  Usuario encontrado:");
        System.out.println(usuario.toString());

        if (usuario.isVigente()) {
            System.out.println("\n  [!] No se puede eliminar: la mensualidad aún está VIGENTE.");
            System.out.println("      Vence el: " + usuario.getFechaVencimiento().format(Usuario.FMT_DISPLAY));
            System.out.println("      Si desea eliminar, espere a que venza o use la opción 7 de renovación.");
            pausar();
            return;
        }

        System.out.println("\n  La mensualidad de este usuario está VENCIDA.");
        String confirmacion = leerTextoConPrompt("  ¿Desea eliminar este usuario? (S/N): ").toUpperCase().trim();

        if (!"S".equals(confirmacion)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean exito = servicio.eliminarUsuario(placa);
        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (exito) {
            System.out.println("  ✔  Usuario [" + placa + "] eliminado exitosamente.");
            System.out.println("     La celda ha sido liberada.");
        } else {
            System.out.println("  [!] No se pudo eliminar el usuario. Intente nuevamente.");
        }
        pausar();
    }

    public void flujoVerCeldas() {
        imprimirEncabezado("ESTADO DE CELDAS DEL PARQUEADERO");

        List<Celda> celdas = servicio.listarCeldas();

        long libres  = celdas.stream().filter(Celda::isDisponible).count();
        long ocupadas = celdas.size() - libres;

        System.out.println();
        System.out.println("  ┌──────────┬────────────┬─────────────┐");
        System.out.println("  │  CELDA   │   ESTADO   │    PLACA    │");
        System.out.println("  ├──────────┼────────────┼─────────────┤");
        for (Celda c : celdas) {
            String estado = c.isDisponible() ? "  LIBRE   " : "  OCUPADA ";
            String placa  = (c.getPlacaVehiculo() == null || c.getPlacaVehiculo().isEmpty())
                            ? "     ---    " : "  " + padRight(c.getPlacaVehiculo(), 10);
            System.out.printf("  │  Nro %2d  │%s│%s│%n",
                c.getNumero(), estado, placa);
        }
        System.out.println("  └──────────┴────────────┴─────────────┘");
        System.out.println();
        System.out.println("  Resumen → Libres: " + libres + "  |  Ocupadas: " + ocupadas + "  |  Total: " + celdas.size());
        pausar();
    }

    public void flujoRenovarUsuario() {
        imprimirEncabezado("RENOVAR MENSUALIDAD");

        String placa = leerTextoConPrompt("  Ingrese la placa del vehículo: ").toUpperCase().trim();

        if (placa.isEmpty()) {
            System.out.println("  [!] Debe ingresar una placa.");
            pausar();
            return;
        }

        Optional<Usuario> usuarioOpt = servicio.buscarPorPlaca(placa);

        if (usuarioOpt.isEmpty()) {
            System.out.println("\n  [!] No se encontró ningún usuario con la placa: " + placa);
            pausar();
            return;
        }

        Usuario antes = usuarioOpt.get();
        System.out.println("\n  Usuario encontrado:");
        System.out.printf("     Propietario  : %s%n", antes.getNombrePropietario());
        System.out.printf("     Estado actual: %s%n", antes.isVigente() ? "ACTIVO" : "INACTIVO");
        System.out.printf("     Vencimiento  : %s%n", antes.getFechaVencimiento().format(Usuario.FMT_DISPLAY));

        String confirmacion = leerTextoConPrompt("\n  ¿Confirma la renovación por 30 días? (S/N): ").toUpperCase().trim();

        if (!"S".equals(confirmacion)) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean exito = servicio.renovarUsuario(placa);

        System.out.println();
        System.out.println(LINEA_SIMPLE);
        if (exito) {
            Optional<Usuario> renovado = servicio.buscarPorPlaca(placa);
            System.out.println("  Mensualidad renovada exitosamente.");
            renovado.ifPresent(u ->
                System.out.println("     Nuevo vencimiento: " + u.getFechaVencimiento().format(Usuario.FMT_DISPLAY))
            );
        } else {
            System.out.println("  [!] No se pudo renovar la mensualidad. Intente nuevamente.");
        }
        pausar();
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

    private String padRight(String texto, int longitud) {
        if (texto == null) texto = "";
        if (texto.length() >= longitud) return texto.substring(0, longitud);
        StringBuilder sb = new StringBuilder(texto);
        while (sb.length() < longitud) sb.append(' ');
        return sb.toString();
    }
}
