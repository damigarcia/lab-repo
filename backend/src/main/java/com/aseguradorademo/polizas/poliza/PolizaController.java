package com.aseguradorademo.polizas.poliza;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    @GetMapping
    public List<PolizaResponse> listar(@RequestParam(required = false) EstadoPoliza estado,
                                       @RequestParam(required = false) TipoPoliza tipo) {
        return polizaService.listar(estado, tipo);
    }

    @GetMapping("/{id}")
    public PolizaResponse obtener(@PathVariable Long id) {
        return polizaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<PolizaResponse> crear(@Valid @RequestBody CrearPolizaRequest request) {
        PolizaResponse creada = polizaService.crear(request);
        return ResponseEntity.created(URI.create("/api/polizas/" + creada.id())).body(creada);
    }

    @PostMapping("/{id}/cancelacion")
    public PolizaResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarPolizaRequest request) {
        return polizaService.cancelar(id, request);
    }
}
