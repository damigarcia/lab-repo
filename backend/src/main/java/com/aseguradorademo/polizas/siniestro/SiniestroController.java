package com.aseguradorademo.polizas.siniestro;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aseguradorademo.polizas.common.NotFoundException;
import com.aseguradorademo.polizas.common.ReglaNegocioException;
import com.aseguradorademo.polizas.poliza.EstadoPoliza;
import com.aseguradorademo.polizas.poliza.Poliza;
import com.aseguradorademo.polizas.poliza.PolizaRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/siniestros")
public class SiniestroController {

    private final SiniestroService siniestroService;
    private final PolizaRepository polizaRepository;

    public SiniestroController(SiniestroService siniestroService, PolizaRepository polizaRepository) {
        this.siniestroService = siniestroService;
        this.polizaRepository = polizaRepository;
    }

    @GetMapping
    public List<SiniestroResponse> listar(@RequestParam Long polizaId) {
        return siniestroService.listarPorPoliza(polizaId);
    }

    @PostMapping
    public ResponseEntity<SiniestroResponse> registrar(@Valid @RequestBody CrearSiniestroRequest request) {
        Poliza poliza = polizaRepository.findById(request.polizaId())
                .orElseThrow(() -> new NotFoundException("Póliza", request.polizaId()));

        if (poliza.getEstado() != EstadoPoliza.VIGENTE) {
            throw new ReglaNegocioException("POLIZA_NO_VIGENTE",
                    "Solo se pueden registrar siniestros sobre pólizas vigentes");
        }
        if (request.fechaOcurrencia().isBefore(poliza.getFechaInicio())
                || request.fechaOcurrencia().isAfter(poliza.getFechaFin())) {
            throw new ReglaNegocioException("FUERA_DE_VIGENCIA",
                    "La fecha de ocurrencia está fuera de la vigencia de la póliza");
        }
        if (request.montoReclamado().compareTo(poliza.getSumaAsegurada()) > 0) {
            throw new ReglaNegocioException("MONTO_EXCEDE_SUMA",
                    "El monto reclamado no puede superar la suma asegurada de la póliza");
        }

        SiniestroResponse creado = siniestroService.registrar(poliza, request);
        return ResponseEntity.created(URI.create("/api/siniestros/" + creado.id())).body(creado);
    }
}
