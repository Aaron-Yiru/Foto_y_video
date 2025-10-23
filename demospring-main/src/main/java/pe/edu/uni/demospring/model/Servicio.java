package pe.edu.uni.demospring.model;

// Esta clase NO necesita @Entity, ya que solo se usa en la sesión (carrito)
public class Servicio {
    private Long id;
    private String nombre;
    private String descripcion;
    private double precio;

    public Servicio() {
    }

    public Servicio(Long id, String nombre, String descripcion, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override
    public String toString() {
        return "Servicio{" + "id=" + id + ", nombre='" + nombre + "', precio=" + precio + '}';
    }
}