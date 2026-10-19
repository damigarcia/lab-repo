package com.aseguradorademo.polizas.poliza;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aseguradorademo.polizas.cliente.Cliente;
import com.aseguradorademo.polizas.cliente.ClienteRepository;
import com.aseguradorademo.polizas.common.NotFoundException;
import com.aseguradorademo.polizas.common.ReglaNegocioException;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2025, 6, 15);

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private PolizaService polizaService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(HOY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        polizaService = new PolizaService(polizaRepository, clienteRepository, clock);
        cliente = new Cliente("Cliente Prueba", "V-99999999", "prueba@ejemplo.com", null,
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1));
        cliente.setId(1L);
    }

    @Test
    void crear_calculaPrimaYVigenciaDeUnAnio() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(polizaRepository.count()).thenReturn(6L);
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> {
            Poliza p = inv.getArgument(0);
            p.setId(7L);
            return p;
        });

        PolizaResponse respuesta = polizaService.crear(
                new CrearPolizaRequest(1L, TipoPoliza.AUTO, new BigDecimal("20000.00"), HOY));

        assertThat(respuesta.id()).isEqualTo(7L);
        assertThat(respuesta.numero()).isEqualTo("POL-AUT-0007");
        assertThat(respuesta.estado()).isEqualTo(EstadoPoliza.VIGENTE);
        assertThat(respuesta.primaAnual()).isEqualByComparingTo("800.00");
        assertThat(respuesta.fechaFin()).isEqualTo(HOY.plusYears(1));
    }

    @Test
    void crear_aplicaPrimaMinima() {
        assertThat(polizaService.calcularPrima(TipoPoliza.HOGAR, new BigDecimal("5000.00")))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void crear_clienteInexistente_lanzaNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaService.crear(
                new CrearPolizaRequest(99L, TipoPoliza.VIDA, new BigDecimal("10000.00"), HOY)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void crear_fechaInicioPasada_lanzaReglaNegocio() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> polizaService.crear(
                new CrearPolizaRequest(1L, TipoPoliza.VIDA, new BigDecimal("10000.00"), HOY.minusDays(1))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("fecha de inicio");
    }

    @Test
    void cancelar_polizaVigente_quedaCancelada() {
        Poliza poliza = poliza(EstadoPoliza.VIGENTE, HOY.plusMonths(6));
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        PolizaResponse respuesta = polizaService.cancelar(1L, new CancelarPolizaRequest("Solicitud del cliente"));

        assertThat(respuesta.estado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(respuesta.motivoCancelacion()).isEqualTo("Solicitud del cliente");
    }

    @Test
    void cancelar_polizaVencida_lanzaReglaNegocio() {
        Poliza poliza = poliza(EstadoPoliza.VENCIDA, HOY.minusDays(5));
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        assertThatThrownBy(() -> polizaService.cancelar(1L, new CancelarPolizaRequest("x")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void obtener_polizaVencidaHace10Dias_estaEnPeriodoDeGracia() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza(EstadoPoliza.VENCIDA, HOY.minusDays(10))));

        assertThat(polizaService.obtener(1L).enPeriodoDeGracia()).isTrue();
    }

    @Test
    void obtener_polizaVencidaHace31Dias_noEstaEnPeriodoDeGracia() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza(EstadoPoliza.VENCIDA, HOY.minusDays(31))));

        assertThat(polizaService.obtener(1L).enPeriodoDeGracia()).isFalse();
    }

    @Test
    void obtener_inexistente_lanzaNotFound() {
        when(polizaRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaService.obtener(42L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listar_delegaFiltrosAlRepositorio() {
        when(polizaRepository.buscar(EstadoPoliza.VIGENTE, TipoPoliza.AUTO))
                .thenReturn(List.of(poliza(EstadoPoliza.VIGENTE, HOY.plusMonths(3))));

        assertThat(polizaService.listar(EstadoPoliza.VIGENTE, TipoPoliza.AUTO)).hasSize(1);
    }

    private Poliza poliza(EstadoPoliza estado, LocalDate fechaFin) {
        Poliza p = new Poliza("POL-AUT-0001", cliente, TipoPoliza.AUTO, new BigDecimal("20000.00"),
                new BigDecimal("800.00"), fechaFin.minusYears(1), fechaFin);
        p.setId(1L);
        p.setEstado(estado);
        return p;
    }
}
