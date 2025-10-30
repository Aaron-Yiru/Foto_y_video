package pe.edu.uni.demospring.model;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.repository.ServicioRepository;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final ServicioRepository servicioRepository;

    public DataLoader(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (servicioRepository.count() == 0) {
            // Usa los constructores de 4 argumentos que definiste
            Servicio s1 = new Servicio("Video y Fotografía de Eventos", "Cobertura completa de bodas, bautizos, etc.", 500.00, "/videos/video-boda (1).mp4");
            Servicio s2 = new Servicio("Producción de Video", "Creación de contenido audiovisual corporativo y publicitario.", 800.00, "/videos/eventos-2.mp4");
            Servicio s3 = new Servicio("Video y Fotografía con Drones", "Tomas aéreas impresionantes para eventos y propiedades.", 650.00, "/videos/DRON-1.mp4");

            servicioRepository.saveAll(List.of(s1, s2, s3));
            System.out.println("✅ Servicios iniciales cargados en la DB.");
        }
    }
}
