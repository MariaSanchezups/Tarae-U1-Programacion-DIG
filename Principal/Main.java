package principal;
import controlador.LogicaContactos;
import vista.VentanaContactos;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                System.out.println(
                    "No se pudo aplicar el estilo del sistema."
                );
            }

            VentanaContactos ventana = new VentanaContactos();

            new LogicaContactos(ventana);

            ventana.setVisible(true);
        });
    }
}

