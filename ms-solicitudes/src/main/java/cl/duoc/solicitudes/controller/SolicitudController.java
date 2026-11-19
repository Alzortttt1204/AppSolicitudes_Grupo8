package cl.duoc.solicitudes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.solicitudes.model.Solicitud;
import cl.duoc.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;

// Endpoints REST que consume la app móvil con Retrofit
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    // GET /api/solicitudes -> lista todas
    @GetMapping
    public List<Solicitud> listar() {
        return service.listar();
    }

    // GET /api/solicitudes/{id} -> una solicitud, o 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/solicitudes -> crea una solicitud (201 Created). @Valid revisa las validaciones
    @PostMapping
    public ResponseEntity<Solicitud> crear(@Valid @RequestBody Solicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(solicitud));
    }

    // PUT /api/solicitudes/{id} -> edita una solicitud, o 404 si no existe
    @PutMapping("/{id}")
    public ResponseEntity<Solicitud> actualizar(@PathVariable Long id, @Valid @RequestBody Solicitud solicitud) {
        return service.actualizar(id, solicitud)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/solicitudes/{id} -> elimina (204 No Content), o 404 si no existe
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
