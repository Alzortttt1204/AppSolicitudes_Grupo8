package cl.duoc.solicitudes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.solicitudes.model.Solicitud;

// JpaRepository ya trae findAll, findById, save, existsById y deleteById
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
}
