package com.aseguradorademo.polizas.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aseguradorademo.polizas.common.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void listar_mapeaAResponse() {
        when(clienteRepository.findAllByOrderByNombreCompletoAsc()).thenReturn(List.of(cliente(1L)));

        List<ClienteResponse> resultado = clienteService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombreCompleto()).isEqualTo("Cliente Prueba");
        assertThat(resultado.get(0).documentoIdentidad()).isEqualTo("V-99999999");
    }

    @Test
    void obtener_existente_devuelveCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));

        assertThat(clienteService.obtener(1L).getId()).isEqualTo(1L);
    }

    @Test
    void obtener_inexistente_lanzaNotFound() {
        when(clienteRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtener(5L)).isInstanceOf(NotFoundException.class);
    }

    private Cliente cliente(Long id) {
        Cliente c = new Cliente("Cliente Prueba", "V-99999999", "prueba@ejemplo.com", "+58 212 555 0000",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1));
        c.setId(id);
        return c;
    }
}
