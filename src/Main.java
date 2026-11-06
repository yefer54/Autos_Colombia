import services.ParqueaderoService;
import ui.Menu;

public class Main {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("  Iniciando sistema y cargando datos...");

        ParqueaderoService servicio;
        try {
            servicio = new ParqueaderoService();
        } catch (Exception e) {
            System.err.println(" No se pudo inicializar el servicio: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            return; 
        }

        System.out.println("  Sistema listo.");
        System.out.println("  Celdas cargadas      : " + servicio.listarCeldas().size());
        System.out.println("  Usuarios del sistema : " + servicio.listarUsuarios().size());
        System.out.println("  Clientes registrados : " + servicio.listarClientes().size());

        Menu menu = new Menu(servicio);
        menu.iniciar();
    }
}
