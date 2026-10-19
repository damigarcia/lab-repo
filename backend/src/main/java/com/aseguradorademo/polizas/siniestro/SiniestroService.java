package com.aseguradorademo.polizas.siniestro;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aseguradorademo.polizas.poliza.Poliza;

@Service
@Transactional(readOnly = true)
public class SiniestroService {

    private final SiniestroRepository siniestroRepository;
    private final Clock clock;

    public SiniestroService(SiniestroRepository siniestroRepository, Clock clock) {
        this.siniestroRepository = siniestroRepository;
        this.clock = clock;
    }

    public List<SiniestroResponse> listarPorPoliza(Long polizaId) {
        return siniestroRepository.findByPolizaIdOrderByFechaOcurrenciaDesc(polizaId).stream()
                .map(SiniestroMapper::toResponse)
                .toList();
    }

    /** Cantidad de siniestros de una póliza ocurridos en los últimos 12 meses. */
    public long contarUltimos12Meses(Long polizaId) {
        LocalDate desde = LocalDate.now(clock).minusMonths(12);
        return siniestroRepository.countByPolizaIdAndFechaOcurrenciaAfter(polizaId, desde);
    }

    @Transactional
    public SiniestroResponse registrar(Poliza poliza, CrearSiniestroRequest request) {
        Siniestro siniestro = new Siniestro(
                poliza,
                request.fechaOcurrencia(),
                LocalDate.now(clock),
                request.descripcion(),
                request.montoReclamado());
        return SiniestroMapper.toResponse(siniestroRepository.save(siniestro));
    }
}
