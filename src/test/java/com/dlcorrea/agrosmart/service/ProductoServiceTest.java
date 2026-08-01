package com.dlcorrea.agrosmart.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.dlcorrea.agrosmart.domain.Producto;
import com.dlcorrea.agrosmart.entity.ProductoEntity;
import com.dlcorrea.agrosmart.exception.ProductoNoEncontradoException;
import com.dlcorrea.agrosmart.repository.ProductoRepository;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * ProductoServiceTest
 */
class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirSoloLosValidos() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);
        Mockito.when(repo.findAll()).thenReturn(List.of(
                crearEntidad(1L, "P1", new BigDecimal("10.0"), "a@a.com"),
                crearEntidad(2L, "P2", new BigDecimal("10.0"), "b@b.com"),
                crearEntidad(3L, "P3", new BigDecimal("10.0"), "c@c.com"),
                crearEntidad(4L, "P4", BigDecimal.ZERO, "d@d.com"),
                crearEntidad(5L, "P5", new BigDecimal("10.0"), "")));

        ProductoService service = new ProductoService(repo);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);
        Mockito.when(repo.findAll()).thenReturn(List.of(
                crearEntidad(1L, "P1", BigDecimal.ZERO, "a@a.com") // Inválido
        ));
        ProductoService service = new ProductoService(repo);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextMatches(p -> p.getNombre().equals("PRODUCTO GENERICO DE RESPALDO"))
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);
        Mockito.when(repo.findById(999L)).thenReturn(Optional.empty());
        ProductoService service = new ProductoService(repo);

        // Act y Assert
        StepVerifier.create(service.buscarPorId(999L))
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    // Helper para crear entidades falsas
    private ProductoEntity crearEntidad(Long id, String nombre, BigDecimal precio, String correo) {
        ProductoEntity entity = new ProductoEntity();
        entity.setIdProducto(id);
        entity.setNombreProducto(nombre);
        entity.setCategoria("Test");
        entity.setPrecioUsd(precio);
        entity.setCorreoNotificacion(correo);

        return entity;
    }
}
