package com.dlcorrea.agrosmart.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;

import com.dlcorrea.agrosmart.ia.AgroSmartAIService;

import reactor.test.StepVerifier;

/**
 * PublicidadServiceTest
 */
class PublicidadServiceTest {

    @Test
    void generarPublicidad_caminoFeliz_debeEmitirTextoGenerado() {
        // Arrange
        AgroSmartAIService ia = Mockito.mock(AgroSmartAIService.class);
        Mockito.when(ia.generarPublicidad(any(), any()))
                .thenReturn("¡Compra la mejor Quinua!");
        PublicidadService service = new PublicidadService(ia);

        // Act & Assert
        StepVerifier.create(service.generarPublicidad("Quinua", "clientes"))
                .expectNext("¡Compra la mejor Quinua!")
                .verifyComplete();
    }

    @Test
    void generarPublicidad_cuandoElProveedorFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        AgroSmartAIService ia = Mockito.mock(AgroSmartAIService.class);
        Mockito.when(ia.generarPublicidad(any(), any()))
                .thenThrow(new RuntimeException("429 Too Many Requests"));
        PublicidadService service = new PublicidadService(ia);

        // Act & Assert
        StepVerifier.create(service.generarPublicidad("Quinua", "clientes"))
                .expectNextMatches(texto -> texto.contains("no disponible"))
                .verifyComplete();
    }
}
