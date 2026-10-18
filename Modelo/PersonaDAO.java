package modelo;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {
    private final Path archivo = Paths.get(
        System.getProperty("user.home"),
        "GestionContactos",
        "datosContactos.csv"
    );
    public PersonaDAO() {
        try {
            Files.createDirectories(archivo.getParent());

            if (Files.notExists(archivo)) {
                Files.createFile(archivo);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "No se pudo preparar el archivo de contactos.", e
            );
        }
    }
    public List<Persona> listar() {
        List<Persona> personas = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(
                archivo, StandardCharsets.UTF_8)) {

            String linea;

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";", -1);

                if (datos.length == 5) {
                    Persona persona = new Persona(
                        datos[0],
                        datos[1],
                        datos[2],
                        Boolean.parseBoolean(datos[3]),
                        datos[4]
                    );

                    personas.add(persona);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                "No se pudieron leer los contactos.", e
            );
        }

        return personas;
    }
    public void guardarTodos(List<Persona> personas) {
        try (BufferedWriter bw = Files.newBufferedWriter(
                archivo, StandardCharsets.UTF_8)) {

            for (Persona persona : personas) {
                bw.write(limpiar(persona.getNombre()) + ";"
                    + limpiar(persona.getTelefono()) + ";"
                    + limpiar(persona.getCorreo()) + ";"
                    + persona.isFavorita() + ";"
                    + limpiar(persona.getCategoria()));

                bw.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(
                "No se pudieron guardar los contactos.", e
            );
        }
    }
    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace(";", ",")
                    .replace("\n", " ")
                    .replace("\r", " ");
    }
}
