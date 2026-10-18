package modelo;
public class Persona {

    private String nombre;
    private String telefono;
    private String correo;
    private boolean favorita;
    private String categoria;

    public Persona(String nombre, String telefono,
                   String correo, boolean favorita,
                   String categoria) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.favorita = favorita;
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isFavorita() {
        return favorita;
    }

    public void setFavorita(boolean favorita) {
        this.favorita = favorita;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}