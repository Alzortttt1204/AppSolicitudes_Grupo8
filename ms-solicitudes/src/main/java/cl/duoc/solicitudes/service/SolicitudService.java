package cl.duoc.solicitudes.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cl.duoc.solicitudes.model.Solicitud;
import cl.duoc.solicitudes.repository.SolicitudRepository;

// Lógica de negocio: el controlador llama al servicio y el servicio al repositorio
@Service
public class SolicitudService {

    private final SolicitudRepository repository;

    public SolicitudService(SolicitudRepository repository) {
        this.repository = repository;
    }

    // Todas las solicitudes, las más nuevas primero
    public List<Solicitud> listar() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Optional<Solicitud> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Registra una solicitud nueva: el servidor pone el código, el estado inicial y la fecha
    public Solicitud crear(Solicitud solicitud) {
        solicitud.setId(null);
        solicitud.setCodigo("SOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        solicitud.setEstado("Recibida");
        solicitud.setFechaHora(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return repository.save(solicitud);
    }

    // Edita los datos de una solicitud; el código, el estado y la fecha no cambian
    public Optional<Solicitud> actualizar(Long id, Solicitud datos) {
        return repository.findById(id).map(existente -> {
            existente.setTitulo(datos.getTitulo());
            existente.setDescripcion(datos.getDescripcion());
            existente.setCategoria(datos.getCategoria());
            existente.setServicio(datos.getServicio());
            existente.setEvidenciaUri(datos.getEvidenciaUri());
            return repository.save(existente);
        });
    }

    // Devuelve false si la solicitud no existe
    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
