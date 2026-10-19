package com.aseguradorademo.polizas.siniestro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {

    List<Siniestro> findByPolizaIdOrderByFechaOcurrenciaDesc(Long polizaId);

    long countByPolizaIdAndFechaOcurrenciaAfter(Long polizaId, LocalDate fecha);
}
