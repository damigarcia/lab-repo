package com.aseguradorademo.polizas.poliza;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    @Query("""
            select p from Poliza p join fetch p.cliente
            where (:estado is null or p.estado = :estado)
              and (:tipo is null or p.tipo = :tipo)
            order by p.fechaInicio desc
            """)
    List<Poliza> buscar(@Param("estado") EstadoPoliza estado, @Param("tipo") TipoPoliza tipo);
}
