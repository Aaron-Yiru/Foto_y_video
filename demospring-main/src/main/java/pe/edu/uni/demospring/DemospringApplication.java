package pe.edu.uni.demospring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.repository.ServicioRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class DemospringApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemospringApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ServicioRepository servicioRepository) {
        return args -> {

            // ✅ Crear carpeta de imágenes si no existe
            Path uploadDir = Paths.get("uploads/images");
            if (!Files.exists(uploadDir)) {
                try {
                    Files.createDirectories(uploadDir);
                    System.out.println("📁 Carpeta creada: " + uploadDir.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("⚠️ No se pudo crear la carpeta de imágenes: " + e.getMessage());
                }
            }

            // ✅ Insertar datos iniciales solo si la tabla está vacía
            if (servicioRepository.count() == 0) {
                servicioRepository.save(new Servicio(null, "Video y Fotografía de Eventos",
                        "Cobertura completa de bodas, bautizos, etc.", 500.00, "/images/default1.jpg"));

                servicioRepository.save(new Servicio(null, "Producción de Video",
                        "Creación de contenido audiovisual corporativo y publicitario.", 800.00, "/images/default2.jpg"));

                servicioRepository.save(new Servicio(null, "Video y Fotografía con Drones",
                        "Tomas aéreas impresionantes para eventos y propiedades.", 650.00, "/images/default3.jpg"));
            }
        };
    }
}
