package com.dlcorrea.agrosmart.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * ProductoFiltersTest
 */
class ProductoFiltersTest {

    @Test
    void IS_VALID_conProductoValido_debeRetornarTrue() {
        // Arrange
        Producto producto = new Producto(1L, "Quinua", "Cereales", new BigDecimal("5.50"), List.of("test@test.com"));

        // Act y Assert
        assertTrue(ProductoFilters.IS_VALID.test(producto));
    }

    @Test
    void IS_VALID_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(1L, "Quinua", "Cereales", BigDecimal.ZERO, List.of("test@test.com"));

        // Act y Assert
        assertFalse(ProductoFilters.IS_VALID.test(producto));
    }

    @Test
    void IS_VALID_conListaCorreosVacia_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(1L, "Quinua", "Cereales", new BigDecimal("5.50"), List.of());

        // Act y Assert
        assertFalse(ProductoFilters.IS_VALID.test(producto));
    }
}
