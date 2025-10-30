package pe.edu.uni.demospring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; // Importaciones de JPA

// ⭐ AÑADIR ESTAS ANOTACIONES
@Entity
@Table(name = "servicio")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private double precio;
    private String foto;

    // Constructor vacío (necesario para JPA)
    public Servicio() {
    }

    // ⭐ Es mejor simplificar los constructores y dejar solo el vacío,
    // y usar setters si estás trabajando con JPA/Servicios

    public Servicio(Long id, String nombre, String descripcion, double precio, String foto) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.foto = foto;
    }
    public Servicio(String nombre, String descripcion, double precio, String foto) {
        // El ID se gestionará automáticamente por la base de datos
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.foto = foto;
    }

    // Getters y Setters (sin cambios en la lógica)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    @Override
    public String toString() {
        return "Servicio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", foto='" + foto + '\'' +
                '}';
    }
}
