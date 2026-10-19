package com.aseguradorademo.polizas.poliza;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aseguradorademo.polizas.common.NotFoundException;
import com.aseguradorademo.polizas.common.ReglaNegocioException;

@WebMvcTest(PolizaController.class)
class PolizaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolizaService polizaService;

    @Test
    void listar_conFiltros_devuelve200() throws Exception {
        when(polizaService.listar(EstadoPoliza.VIGENTE, TipoPoliza.AUTO)).thenReturn(List.of(respuesta(1L)));

        mockMvc.perform(get("/api/polizas").param("estado", "VIGENTE").param("tipo", "AUTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numero").value("POL-AUT-0001"))
                .andExpect(jsonPath("$[0].fechaInicio").value("2025-01-01"));
    }

    @Test
    void listar_estadoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/polizas").param("estado", "INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(polizaService.obtener(99L)).thenThrow(new NotFoundException("Póliza", 99L));

        mockMvc.perform(get("/api/polizas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_valido_devuelve201ConLocation() throws Exception {
        when(polizaService.crear(any(CrearPolizaRequest.class))).thenReturn(respuesta(7L));

        mockMvc.perform(post("/api/polizas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 1, "tipo": "AUTO", "sumaAsegurada": 20000.00, "fechaInicio": "2025-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/polizas/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void crear_sinCamposObligatorios_devuelve400() throws Exception {
        mockMvc.perform(post("/api/polizas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles", hasSize(4)));
    }

    @Test
    void cancelar_polizaNoVigente_devuelve422() throws Exception {
        when(polizaService.cancelar(eq(3L), any(CancelarPolizaRequest.class)))
                .thenThrow(new ReglaNegocioException("ESTADO_INVALIDO", "Solo se pueden cancelar pólizas vigentes"));

        mockMvc.perform(post("/api/polizas/3/cancelacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Solicitud del cliente\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESTADO_INVALIDO"));
    }

    @Test
    void cancelar_sinMotivo_devuelve400() throws Exception {
        mockMvc.perform(post("/api/polizas/1/cancelacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    private PolizaResponse respuesta(Long id) {
        return new PolizaResponse(id, "POL-AUT-000" + id, 1L, "Cliente Prueba", TipoPoliza.AUTO,
                EstadoPoliza.VIGENTE, new BigDecimal("20000.00"), new BigDecimal("800.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), null, false);
    }
}
