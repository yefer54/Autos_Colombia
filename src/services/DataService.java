package services;

import models.Celda;
import models.Usuario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataService {

    public static List<Usuario> cargarUsuarios(String rutaArchivo) {
        List<Usuario> lista = new ArrayList<>();
        String contenido = leerArchivo(rutaArchivo);
        if (contenido == null || contenido.isBlank()) return lista;

        List<Map<String, String>> objetos = parsearArrayJson(contenido);
        for (Map<String, String> obj : objetos) {
            Usuario u = Usuario.fromJsonObject(obj);
            if (u != null) lista.add(u);
        }
        return lista;
    }

    public static void guardarUsuarios(List<Usuario> usuarios, String rutaArchivo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < usuarios.size(); i++) {
            sb.append("  ").append(usuarios.get(i).toJsonString());
            if (i < usuarios.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        escribirArchivo(rutaArchivo, sb.toString());
    }

    public static List<Celda> cargarCeldas(String rutaArchivo) {
        List<Celda> lista = new ArrayList<>();
        String contenido = leerArchivo(rutaArchivo);
        if (contenido == null || contenido.isBlank()) return lista;

        List<Map<String, String>> objetos = parsearArrayJson(contenido);
        for (Map<String, String> obj : objetos) {
            Celda c = Celda.fromJsonObject(obj);
            if (c != null) lista.add(c);
        }
        return lista;
    }

    public static void guardarCeldas(List<Celda> celdas, String rutaArchivo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < celdas.size(); i++) {
            sb.append("  ").append(celdas.get(i).toJsonString());
            if (i < celdas.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        escribirArchivo(rutaArchivo, sb.toString());
    }

    static List<Map<String, String>> parsearArrayJson(String json) {
        List<Map<String, String>> resultado = new ArrayList<>();
        if (json == null) return resultado;

        int inicioArray = json.indexOf('[');
        int finArray    = json.lastIndexOf(']');
        if (inicioArray == -1 || finArray == -1 || inicioArray >= finArray) return resultado;

        String contenidoArray = json.substring(inicioArray + 1, finArray);

        List<String> objetos = extraerObjetos(contenidoArray);
        for (String obj : objetos) {
            Map<String, String> mapa = parsearObjeto(obj);
            if (!mapa.isEmpty()) resultado.add(mapa);
        }
        return resultado;
    }

    private static List<String> extraerObjetos(String texto) {
        List<String> objetos = new ArrayList<>();
        int profundidad = 0;
        int inicio      = -1;

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '{') {
                if (profundidad == 0) inicio = i;
                profundidad++;
            } else if (c == '}') {
                profundidad--;
                if (profundidad == 0 && inicio != -1) {
                    objetos.add(texto.substring(inicio, i + 1));
                    inicio = -1;
                }
            }
        }
        return objetos;
    }

    private static Map<String, String> parsearObjeto(String objeto) {
        Map<String, String> mapa = new LinkedHashMap<>();
        if (objeto == null || objeto.isBlank()) return mapa;

        String contenido = objeto.trim();
        if (contenido.startsWith("{")) contenido = contenido.substring(1);
        if (contenido.endsWith("}"))  contenido = contenido.substring(0, contenido.length() - 1);

        List<String> pares = dividirPorComaFueraDeComillas(contenido);

        for (String par : pares) {
            int dospuntos = indiceDospuntosDeKeyValue(par);
            if (dospuntos == -1) continue;

            String clave = par.substring(0, dospuntos).trim();
            String valor = par.substring(dospuntos + 1).trim();

            clave = eliminarComillas(clave);
            valor = eliminarComillas(valor);

            if (!clave.isEmpty()) {
                mapa.put(clave, valor);
            }
        }
        return mapa;
    }

    private static List<String> dividirPorComaFueraDeComillas(String texto) {
        List<String> partes   = new ArrayList<>();
        StringBuilder actual  = new StringBuilder();
        boolean enComillas    = false;
        boolean escapado      = false;

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (escapado) {
                actual.append(c);
                escapado = false;
            } else if (c == '\\') {
                actual.append(c);
                escapado = true;
            } else if (c == '"') {
                enComillas = !enComillas;
                actual.append(c);
            } else if (c == ',' && !enComillas) {
                partes.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        if (actual.length() > 0) partes.add(actual.toString());
        return partes;
    }

    private static int indiceDospuntosDeKeyValue(String par) {
        boolean enComillas = false;
        boolean escapado   = false;
        for (int i = 0; i < par.length(); i++) {
            char c = par.charAt(i);
            if (escapado) { escapado = false; continue; }
            if (c == '\\') { escapado = true; continue; }
            if (c == '"')  { enComillas = !enComillas; continue; }
            if (c == ':' && !enComillas) return i;
        }
        return -1;
    }

    private static String eliminarComillas(String valor) {
        if (valor == null) return "";
        valor = valor.trim();
        if (valor.startsWith("\"") && valor.endsWith("\"") && valor.length() >= 2) {
            valor = valor.substring(1, valor.length() - 1);
        }

        valor = valor.replace("\\\"", "\"").replace("\\\\", "\\");
        return valor;
    }

    private static String leerArchivo(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo leer el archivo: " + rutaArchivo + " → " + e.getMessage());
            return null;
        }
        return sb.toString();
    }

    private static void escribirArchivo(String rutaArchivo, String contenido) {
        File archivo = new File(rutaArchivo);
        File directorio = archivo.getParentFile();
        if (directorio != null && !directorio.exists()) {
            directorio.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo, false), StandardCharsets.UTF_8))) {
            bw.write(contenido);
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo escribir el archivo: " + rutaArchivo + " → " + e.getMessage());
        }
    }
}
