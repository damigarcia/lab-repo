package com.aseguradorademo.polizas.cliente;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aseguradorademo.polizas.common.NotFoundException;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @Test
    void listar_devuelve200() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(
                new ClienteResponse(1L, "Cliente Prueba", "V-99999999", "prueba@ejemplo.com", null,
                        LocalDate.of(1990, 1, 1))));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Cliente Prueba"));
    }

    @Test
    void obtener_existente_devuelve200() throws Exception {
        Cliente cliente = new Cliente("Cliente Prueba", "V-99999999", "prueba@ejemplo.com", null,
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1));
        cliente.setId(1L);
        when(clienteService.obtener(1L)).thenReturn(cliente);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("prueba@ejemplo.com"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(clienteService.obtener(9L)).thenThrow(new NotFoundException("Cliente", 9L));

        mockMvc.perform(get("/api/clientes/9"))
                .andExpect(status().isNotFound());
    }
}
