package controlador;
import modelo.Persona;
import modelo.PersonaDAO;
import vista.VentanaContactos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogicaContactos {

    private final VentanaContactos vista;
    private final PersonaDAO dao;
    private final List<Persona> contactos;

    public LogicaContactos(VentanaContactos vista) {
        this.vista = vista;
        this.dao = new PersonaDAO();
        this.contactos = new ArrayList<>();

        configurarEventos();
        cargarContactos();
    }

    private void configurarEventos() {
        vista.getBtnAgregar().addActionListener(e -> agregarContacto());
        vista.getBtnEditar().addActionListener(e -> editarContacto());
        vista.getBtnEliminar().addActionListener(e -> eliminarContacto());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnExportar().addActionListener(e -> exportarCSV());

        vista.getMenuEditar().addActionListener(e -> editarContacto());
        vista.getMenuEliminar().addActionListener(e -> eliminarContacto());
        vista.getMenuFavorita().addActionListener(e -> cambiarFavorita());

        configurarAtajos();
        configurarClicDerecho();
        
     vista.getTablaContactos().getSelectionModel()
    .addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int fila = vista.getFilaSeleccionada();

            if (fila != -1) {
                vista.mostrarDatosContacto(contactos.get(fila));
            }
        }
    });

    }

    // Cargar contactos usando una tarea en segundo plano
    private void cargarContactos() {
        vista.actualizarProgreso(0, "Cargando contactos...");

        SwingWorker<List<Persona>, Integer> trabajador =
            new SwingWorker<List<Persona>, Integer>() {

            @Override
            protected List<Persona> doInBackground() {
                return dao.listar();
            }

            @Override
            protected void done() {
                try {
                    contactos.clear();
                    contactos.addAll(get());

                    actualizarTabla();
                    actualizarEstadisticas();

                    vista.actualizarProgreso(100, "Carga completada");

                } catch (Exception e) {
                    vista.actualizarProgreso(0, "Error de carga");
                    vista.mostrarMensaje(
                        "No se pudieron cargar los contactos: "
                        + e.getMessage()
                    );
                }
            }
        };

        trabajador.execute();
    }

    private void agregarContacto() {
        if (!validarFormulario()) {
            return;
        }
        Persona persona = leerFormulario();

        contactos.add(persona);

        if (guardarCambios()) {
            actualizarTabla();
            actualizarEstadisticas();
            limpiarFormulario();
            vista.mostrarMensaje("Contacto agregado correctamente.");
        } else {
            contactos.remove(persona);
            actualizarTabla();
        }
    }

    private void editarContacto() {
        int fila = vista.getFilaSeleccionada();

        if (fila == -1) {
            vista.mostrarMensaje("Selecciona un contacto para editar.");
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        Persona persona = contactos.get(fila);

        String nombreAnterior = persona.getNombre();
        String telefonoAnterior = persona.getTelefono();
        String correoAnterior = persona.getCorreo();
        String categoriaAnterior = persona.getCategoria();
        boolean favoritaAnterior = persona.isFavorita();

        Persona editada = leerFormulario();

        persona.setNombre(editada.getNombre());
        persona.setTelefono(editada.getTelefono());
        persona.setCorreo(editada.getCorreo());
        persona.setCategoria(editada.getCategoria());
        persona.setFavorita(editada.isFavorita());

        if (guardarCambios()) {
            actualizarTabla();
            actualizarEstadisticas();
            limpiarFormulario();
            vista.mostrarMensaje("Contacto actualizado correctamente.");
        } else {
            persona.setNombre(nombreAnterior);
            persona.setTelefono(telefonoAnterior);
            persona.setCorreo(correoAnterior);
            persona.setCategoria(categoriaAnterior);
            persona.setFavorita(favoritaAnterior);
            actualizarTabla();
        }
    }

    private void eliminarContacto() {
        int fila = vista.getFilaSeleccionada();

        if (fila == -1) {
            vista.mostrarMensaje("Selecciona un contacto para eliminar.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
            vista,
            "¿Deseas eliminar este contacto?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        Persona eliminada = contactos.remove(fila);

        if (guardarCambios()) {
            actualizarTabla();
            actualizarEstadisticas();
            limpiarFormulario();
            vista.mostrarMensaje("Contacto eliminado correctamente.");
        } else {
            contactos.add(fila, eliminada);
            actualizarTabla();
        }
    }

    private void cambiarFavorita() {
        int fila = vista.getFilaSeleccionada();

        if (fila == -1) {
            vista.mostrarMensaje("Selecciona un contacto primero.");
            return;
        }

        Persona persona = contactos.get(fila);
        persona.setFavorita(!persona.isFavorita());

        if (guardarCambios()) {
            actualizarTabla();
            actualizarEstadisticas();
        } else {
            persona.setFavorita(!persona.isFavorita());
            actualizarTabla();
        }
    }

    private Persona leerFormulario() {
        String nombre = vista.getTxtNombre().getText().trim();
        String telefono = vista.getTxtTelefono().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();
        String categoria = vista.getTxtCategoria().getText().trim();

        if (categoria.isEmpty()) {
            categoria = "General";
        }

        boolean favorita = vista.getChkFavorita().isSelected();

        return new Persona(
            nombre, telefono, correo, favorita, categoria
        );
    }

    private boolean validarFormulario() {
        String nombre = vista.getTxtNombre().getText().trim();
        String telefono = vista.getTxtTelefono().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();

        if (nombre.isEmpty()) {
            vista.mostrarMensaje("El nombre es obligatorio.");
            vista.getTxtNombre().requestFocus();
            return false;
        }

        if (!telefono.isEmpty()
                && !telefono.matches("[0-9+()\\- ]+")) {
            vista.mostrarMensaje("Revisa el formato del teléfono.");
            vista.getTxtTelefono().requestFocus();
            return false;
        }

        if (!correo.isEmpty()
                && !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            vista.mostrarMensaje("Ingresa un correo válido.");
            vista.getTxtCorreo().requestFocus();
            return false;
        }

        return true;
    }

    private void actualizarTabla() {
        vista.limpiarTabla();

        for (Persona persona : contactos) {
            vista.agregarFila(persona);
        }
    }

    private void actualizarEstadisticas() {
        int total = contactos.size();
        int favoritas = 0;

        for (Persona persona : contactos) {
            if (persona.isFavorita()) {
                favoritas++;
            }
        }

        vista.actualizarEstadisticas(total, favoritas);
    }

    private void limpiarFormulario() {
        vista.getTxtNombre().setText("");
        vista.getTxtTelefono().setText("");
        vista.getTxtCorreo().setText("");
        vista.getTxtCategoria().setText("");
        vista.getChkFavorita().setSelected(false);

        vista.getTablaContactos().clearSelection();
        vista.getTxtNombre().requestFocus();
    }

    private boolean guardarCambios() {
        try {
            dao.guardarTodos(contactos);
            return true;

        } catch (RuntimeException e) {
            vista.mostrarMensaje(
                "No se pudieron guardar los cambios: " + e.getMessage()
            );
            return false;
        }
    }

    // Exportación a CSV
    private void exportarCSV() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar contactos a CSV");
        selector.setSelectedFile(
            new java.io.File("contactos.csv")
        );

        int resultado = selector.showSaveDialog(vista);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path archivo = selector.getSelectedFile().toPath();

        if (!archivo.toString().toLowerCase().endsWith(".csv")) {
            archivo = Path.of(archivo.toString() + ".csv");
        }

        Path destino = archivo;

        vista.actualizarProgreso(0, "Exportando...");

        SwingWorker<Void, Void> trabajador = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws IOException {
                try (BufferedWriter escritor = Files.newBufferedWriter(
                        destino, StandardCharsets.UTF_8)) {

                    escritor.write(
                        "Nombre,Telefono,Correo,Favorita,Categoria"
                    );
                    escritor.newLine();

                    for (Persona persona : contactos) {
                        escritor.write(
                            csv(persona.getNombre()) + ","
                            + csv(persona.getTelefono()) + ","
                            + csv(persona.getCorreo()) + ","
                            + csv(persona.isFavorita() ? "Sí" : "No") + ","
                            + csv(persona.getCategoria())
                        );
                        escritor.newLine();
                    }
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    vista.actualizarProgreso(100, "Exportación completada");
                    vista.mostrarMensaje(
                        "Contactos exportados en:\n" + destino
                    );
                } catch (Exception e) {
                    vista.actualizarProgreso(0, "Error de exportación");
                    vista.mostrarMensaje(
                        "No se pudo exportar el archivo: " + e.getMessage()
                    );
                }
            }
        };

        trabajador.execute();
    }

    private String csv(String texto) {
        if (texto == null) {
            texto = "";
        }

        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    // Atajos de teclado
    private void configurarAtajos() {
        JRootPane raiz = vista.getRootPane();

        agregarAtajo(raiz, "control N", "nuevo", this::limpiarFormulario);
        agregarAtajo(raiz, "control E", "editar", this::editarContacto);
        agregarAtajo(raiz, "DELETE", "eliminar", this::eliminarContacto);
        agregarAtajo(raiz, "control S", "exportar", this::exportarCSV);
        agregarAtajo(raiz, "control F", "buscar",
            () -> vista.getTxtBuscar().requestFocus());
    }

    private void agregarAtajo(
            JRootPane raiz, String tecla, String nombre, Runnable accion) {

        KeyStroke teclaRapida = KeyStroke.getKeyStroke(tecla);

        raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(teclaRapida, nombre);

        raiz.getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.run();
            }
        });
    }

    // Seleccionar la fila sobre la que se hace clic derecho
    private void configurarClicDerecho() {
        vista.getTablaContactos().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                seleccionarFilaClicDerecho(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                seleccionarFilaClicDerecho(e);
            }

            private void seleccionarFilaClicDerecho(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int filaVista = vista.getTablaContactos()
                        .rowAtPoint(e.getPoint());

                    if (filaVista >= 0) {
                        vista.getTablaContactos()
                            .setRowSelectionInterval(filaVista, filaVista);
                    }
                }
            }
        });
    }
}

