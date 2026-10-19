package com.aseguradorademo.polizas.poliza;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aseguradorademo.polizas.cliente.Cliente;
import com.aseguradorademo.polizas.cliente.ClienteRepository;
import com.aseguradorademo.polizas.common.NotFoundException;
import com.aseguradorademo.polizas.common.ReglaNegocioException;

@Service
@Transactional(readOnly = true)
public class PolizaService {

    private static final BigDecimal PRIMA_MINIMA = new BigDecimal("100.00");

    private final PolizaRepository polizaRepository;
    private final ClienteRepository clienteRepository;
    private final Clock clock;

    public PolizaService(PolizaRepository polizaRepository, ClienteRepository clienteRepository, Clock clock) {
        this.polizaRepository = polizaRepository;
        this.clienteRepository = clienteRepository;
        this.clock = clock;
    }

    public List<PolizaResponse> listar(EstadoPoliza estado, TipoPoliza tipo) {
        return polizaRepository.buscar(estado, tipo).stream()
                .map(p -> PolizaMapper.toResponse(p, estaEnPeriodoDeGracia(p)))
                .toList();
    }

    public PolizaResponse obtener(Long id) {
        Poliza poliza = buscarPoliza(id);
        return PolizaMapper.toResponse(poliza, estaEnPeriodoDeGracia(poliza));
    }

    @Transactional
    public PolizaResponse crear(CrearPolizaRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new NotFoundException("Cliente", request.clienteId()));

        if (request.fechaInicio().isBefore(LocalDate.now(clock))) {
            throw new ReglaNegocioException("FECHA_INICIO_INVALIDA",
                    "La fecha de inicio no puede ser anterior a hoy");
        }

        BigDecimal prima = calcularPrima(request.tipo(), request.sumaAsegurada());
        Poliza poliza = new Poliza(
                generarNumero(request.tipo()),
                cliente,
                request.tipo(),
                request.sumaAsegurada(),
                prima,
                request.fechaInicio(),
                request.fechaInicio().plusYears(1));

        Poliza guardada = polizaRepository.save(poliza);
        return PolizaMapper.toResponse(guardada, false);
    }

    @Transactional
    public PolizaResponse cancelar(Long id, CancelarPolizaRequest request) {
        Poliza poliza = buscarPoliza(id);
        if (poliza.getEstado() != EstadoPoliza.VIGENTE) {
            throw new ReglaNegocioException("ESTADO_INVALIDO",
                    "Solo se pueden cancelar pólizas en estado VIGENTE (estado actual: " + poliza.getEstado() + ")");
        }
        poliza.cancelar(request.motivo());
        return PolizaMapper.toResponse(poliza, false);
    }

    /**
     * Prima anual = suma asegurada x tasa base del tipo, con un mínimo de {@link #PRIMA_MINIMA}.
     */
    BigDecimal calcularPrima(TipoPoliza tipo, BigDecimal sumaAsegurada) {
        BigDecimal prima = sumaAsegurada.multiply(tipo.getTasaBase()).setScale(2, RoundingMode.HALF_UP);
        return prima.max(PRIMA_MINIMA);
    }

    /**
     * Una póliza vencida sigue en período de gracia durante los días posteriores a su fecha de fin.
     */
    boolean estaEnPeriodoDeGracia(Poliza poliza) {
        if (poliza.getEstado() != EstadoPoliza.VENCIDA) {
            return false;
        }
        LocalDate hoy = LocalDate.now(clock);
        return !hoy.isAfter(poliza.getFechaFin().plusDays(30));
    }

    private Poliza buscarPoliza(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Póliza", id));
    }

    private String generarNumero(TipoPoliza tipo) {
        long siguiente = polizaRepository.count() + 1;
        return "POL-" + tipo.name().substring(0, 3) + "-" + String.format("%04d", siguiente);
    }
}
