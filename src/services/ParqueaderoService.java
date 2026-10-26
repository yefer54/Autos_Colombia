package services;

import models.Celda;
import models.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ParqueaderoService {

    public static final String PIN_AUTENTICACION = "1234";

    public static final int TOTAL_CELDAS = 10;

    public static final String RUTA_USUARIOS = "data/usuarios.json";

    public static final String RUTA_CELDAS = "data/celdas.json";

    private List<Usuario> usuarios;

    private List<Celda> celdas;

    public ParqueaderoService() {
        usuarios = DataService.cargarUsuarios(RUTA_USUARIOS);
        celdas   = DataService.cargarCeldas(RUTA_CELDAS);

        if (celdas == null || celdas.isEmpty()) {
            celdas = inicializarCeldas();
            DataService.guardarCeldas(celdas, RUTA_CELDAS);
        }

        sincronizarCeldas();
    }

    public boolean autenticar(String pin) {
        return PIN_AUTENTICACION.equals(pin);
    }

    public boolean hayCeldasDisponibles() {
        return celdas.stream().anyMatch(Celda::isDisponible);
    }

    public Optional<Celda> obtenerCeldaDisponible() {
        return celdas.stream().filter(Celda::isDisponible).findFirst();
    }


    public boolean registrarNuevoUsuario(String placa, String nombre, String telefono) {
        if (placa == null || placa.isBlank()) return false;

        String placaMayus = placa.toUpperCase().trim();

        Optional<Usuario> existente = buscarPorPlaca(placaMayus);
        if (existente.isPresent()) return false;

        Optional<Celda> celdaOpt = obtenerCeldaDisponible();
        if (celdaOpt.isEmpty()) return false;

        Celda celda = celdaOpt.get();

        LocalDateTime ahora           = LocalDateTime.now();
        LocalDateTime vencimiento     = ahora.plusDays(30);
        Usuario nuevoUsuario = new Usuario(placaMayus, nombre, telefono, celda.getNumero(), ahora, vencimiento, true);

        celda.setDisponible(false);
        celda.setPlacaVehiculo(placaMayus);

        usuarios.add(nuevoUsuario);
        guardarDatos();
        return true;
    }

    public Optional<Usuario> buscarPorPlaca(String placa) {
        if (placa == null) return Optional.empty();
        String placaMayus = placa.toUpperCase().trim();
        return usuarios.stream()
                .filter(u -> u.getPlaca().equalsIgnoreCase(placaMayus))
                .findFirst();
    }

    public boolean validarIngreso(String placa) {
        Optional<Usuario> usuarioOpt = buscarPorPlaca(placa);
        return usuarioOpt.isPresent() && usuarioOpt.get().isVigente();
    }

    public boolean eliminarUsuario(String placa) {
        Optional<Usuario> usuarioOpt = buscarPorPlaca(placa);
        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();

        if (usuario.isVigente()) return false;

        liberarCelda(usuario.getNumeroCelda());

        usuarios.remove(usuario);
        guardarDatos();
        return true;
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public List<Celda> listarCeldas() {
        return new ArrayList<>(celdas);
    }

    public boolean renovarUsuario(String placa) {
        Optional<Usuario> usuarioOpt = buscarPorPlaca(placa);
        if (usuarioOpt.isEmpty()) return false;

        Usuario usuario = usuarioOpt.get();
        LocalDateTime nuevaFechaVencimiento = LocalDateTime.now().plusDays(30);
        usuario.setFechaVencimiento(nuevaFechaVencimiento);
        usuario.setActivo(true);
        guardarDatos();
        return true;
    }

    public void guardarDatos() {
        DataService.guardarUsuarios(usuarios, RUTA_USUARIOS);
        DataService.guardarCeldas(celdas, RUTA_CELDAS);
    }

    private List<Celda> inicializarCeldas() {
        List<Celda> lista = new ArrayList<>();
        for (int i = 1; i <= TOTAL_CELDAS; i++) {
            lista.add(new Celda(i, true, ""));
        }
        return lista;
    }

    private void sincronizarCeldas() {
        for (Celda c : celdas) {
            c.setDisponible(true);
            c.setPlacaVehiculo("");
        }

        for (Usuario u : usuarios) {
            int numCelda = u.getNumeroCelda();
            Optional<Celda> celdaOpt = celdas.stream()
                    .filter(c -> c.getNumero() == numCelda)
                    .findFirst();
            celdaOpt.ifPresent(c -> {
                c.setDisponible(false);
                c.setPlacaVehiculo(u.getPlaca());
            });
        }
    }

    private void liberarCelda(int numeroCelda) {
        celdas.stream()
              .filter(c -> c.getNumero() == numeroCelda)
              .findFirst()
              .ifPresent(c -> {
                  c.setDisponible(true);
                  c.setPlacaVehiculo("");
              });
    }
}
