package vista;
import modelo.Persona;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;

public class VentanaContactos extends JFrame {

    private JTabbedPane pestañas;

    private JTable tablaContactos;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> ordenador;

    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtCategoria;
    private JTextField txtBuscar;
    private JCheckBox chkFavorita;

    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnExportar;

    private JProgressBar barraProgreso;

    private JLabel lblTotal;
    private JLabel lblFavoritos;

    private JMenuItem menuEditar;
    private JMenuItem menuEliminar;
    private JMenuItem menuFavorita;

    public VentanaContactos() {
        setTitle("Gestión de Contactos");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        crearInterfaz();
    }
    private void crearInterfaz() {
        pestañas = new JTabbedPane();

        pestañas.addTab("Contactos", crearPanelContactos());
        pestañas.addTab("Estadísticas", crearPanelEstadisticas());

        add(pestañas, BorderLayout.CENTER);

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setValue(0);
        barraProgreso.setString("Listo");

        add(barraProgreso, BorderLayout.SOUTH);
    }
    private JPanel crearPanelContactos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulario
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(
            BorderFactory.createTitledBorder("Datos del contacto")
        );

        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();
        txtCategoria = new JTextField();
        chkFavorita = new JCheckBox("Marcar como favorita");

        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);

        formulario.add(new JLabel("Teléfono:"));
        formulario.add(txtTelefono);

        formulario.add(new JLabel("Correo:"));
        formulario.add(txtCorreo);

        formulario.add(new JLabel("Categoría:"));
        formulario.add(txtCategoria);

        formulario.add(new JLabel("Favorita:"));
        formulario.add(chkFavorita);

        btnAgregar = new JButton("Agregar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");
        btnExportar = new JButton("Exportar CSV");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnExportar);

        JPanel zonaSuperior = new JPanel(new BorderLayout(5, 5));
        zonaSuperior.add(formulario, BorderLayout.CENTER);
        zonaSuperior.add(botones, BorderLayout.SOUTH);

        panel.add(zonaSuperior, BorderLayout.NORTH);

        // Búsqueda
        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));
        panelBusqueda.add(new JLabel("Buscar contacto:"), BorderLayout.WEST);

        txtBuscar = new JTextField();
        txtBuscar.setToolTipText(
            "Busca por nombre, teléfono, correo o categoría"
        );

        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        // Tabla
        modeloTabla = new DefaultTableModel(
            new Object[]{
                "Nombre", "Teléfono", "Correo",
                "Favorita", "Categoría"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaContactos = new JTable(modeloTabla);
        tablaContactos.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        tablaContactos.setAutoCreateRowSorter(true);

        ordenador = new TableRowSorter<>(modeloTabla);
        tablaContactos.setRowSorter(ordenador);

        JScrollPane scroll = new JScrollPane(tablaContactos);

        JPanel zonaTabla = new JPanel(new BorderLayout(5, 5));
        zonaTabla.add(panelBusqueda, BorderLayout.NORTH);
        zonaTabla.add(scroll, BorderLayout.CENTER);

        panel.add(zonaTabla, BorderLayout.CENTER);

        // Menú contextual: clic derecho
        JPopupMenu menuContextual = new JPopupMenu();

        menuEditar = new JMenuItem("Editar contacto");
        menuEliminar = new JMenuItem("Eliminar contacto");
        menuFavorita = new JMenuItem("Cambiar estado de favorita");

        menuContextual.add(menuEditar);
        menuContextual.add(menuEliminar);
        menuContextual.addSeparator();
        menuContextual.add(menuFavorita);

        tablaContactos.setComponentPopupMenu(menuContextual);

        // Filtrado mientras se escribe
        txtBuscar.getDocument().addDocumentListener(
            new DocumentListener() {

                private void filtrar() {
                    String texto = txtBuscar.getText().trim();

                    if (texto.isEmpty()) {
                        ordenador.setRowFilter(null);
                    } else {
                        ordenador.setRowFilter(
                            RowFilter.regexFilter(
                                "(?i)" + java.util.regex.Pattern.quote(texto)
                            )
                        );
                    }
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    filtrar();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filtrar();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filtrar();
                }
            }
        );

        return panel;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        lblTotal = new JLabel("Total de contactos: 0");
        lblFavoritos = new JLabel("Contactos favoritos: 0");

        lblTotal.setFont(new Font("Arial", Font.BOLD, 20));
        lblFavoritos.setFont(new Font("Arial", Font.BOLD, 20));

        panel.add(lblTotal);
        panel.add(lblFavoritos);

        return panel;
    }

    // Métodos para actualizar la tabla

    public void agregarFila(Persona persona) {
        modeloTabla.addRow(new Object[]{
            persona.getNombre(),
            persona.getTelefono(),
            persona.getCorreo(),
            persona.isFavorita() ? "Sí" : "No",
            persona.getCategoria()
        });
    }

    public void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    public void actualizarEstadisticas(int total, int favoritas) {
        lblTotal.setText("Total de contactos: " + total);
        lblFavoritos.setText("Contactos favoritos: " + favoritas);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    public void actualizarProgreso(int valor, String mensaje) {
        barraProgreso.setValue(valor);
        barraProgreso.setString(mensaje);
    }

    // Acceso a los componentes desde el controlador

    public JTable getTablaContactos() {
        return tablaContactos;
    }

    public JTextField getTxtNombre() {
        return txtNombre;
    }

    public JTextField getTxtTelefono() {
        return txtTelefono;
    }

    public JTextField getTxtCorreo() {
        return txtCorreo;
    }

    public JTextField getTxtCategoria() {
        return txtCategoria;
    }

    public JTextField getTxtBuscar() {
        return txtBuscar;
    }

    public JCheckBox getChkFavorita() {
        return chkFavorita;
    }

    public JButton getBtnAgregar() {
        return btnAgregar;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JButton getBtnLimpiar() {
        return btnLimpiar;
    }

    public JButton getBtnExportar() {
        return btnExportar;
    }

    public JMenuItem getMenuEditar() {
        return menuEditar;
    }

    public JMenuItem getMenuEliminar() {
        return menuEliminar;
    }

    public JMenuItem getMenuFavorita() {
        return menuFavorita;
    }

    public int getFilaSeleccionada() {
        int fila = tablaContactos.getSelectedRow();

        if (fila == -1) {
            return -1;
        }

        return tablaContactos.convertRowIndexToModel(fila);
    }

    public void seleccionarFila(int filaModelo) {
        int filaVista = tablaContactos.convertRowIndexToView(filaModelo);

        if (filaVista >= 0) {
            tablaContactos.setRowSelectionInterval(
                filaVista, filaVista
            );
        }
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }   
public void mostrarDatosContacto(Persona persona) {
    txtNombre.setText(persona.getNombre());
    txtTelefono.setText(persona.getTelefono());
    txtCorreo.setText(persona.getCorreo());
    txtCategoria.setText(persona.getCategoria());
    chkFavorita.setSelected(persona.isFavorita());
}

}
