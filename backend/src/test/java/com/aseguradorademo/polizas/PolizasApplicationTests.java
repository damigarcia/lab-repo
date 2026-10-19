package com.aseguradorademo.polizas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.aseguradorademo.polizas.poliza.PolizaRepository;

/** Verifica que el contexto levanta y que schema.sql / data.sql son válidos. */
@SpringBootTest
class PolizasApplicationTests {

    @Autowired
    private PolizaRepository polizaRepository;

    @Test
    void contextoLevantaYCargaDatosDeDemo() {
        assertThat(polizaRepository.count()).isEqualTo(6);
    }
}
