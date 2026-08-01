package com.dlcorrea.agrosmart.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.dlcorrea.agrosmart.entity.ProductoEntity;
import com.dlcorrea.agrosmart.repository.ProductoRepository;

/**
 * DatabaseSeeder
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductoRepository repository;

    public DatabaseSeeder(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            // Productos validos
            ProductoEntity p1 = new ProductoEntity();
            p1.setNombreProducto("Quinua Blanca Organica");
            p1.setPrecioUsd(new BigDecimal("3.50"));
            p1.setStockkg(500);
            p1.setCategoria("Quinua");
            p1.setCorreoNotificacion("exportaciones@agrosmart.com.ec");

            ProductoEntity p2 = new ProductoEntity();
            p2.setNombreProducto("Quinua Roja de Altura");
            p2.setPrecioUsd(new BigDecimal("4.20"));
            p2.setStockkg(300);
            p2.setCategoria("Quinua");
            p2.setCorreoNotificacion("exportaciones@agrosmart.com.ec");

            ProductoEntity p3 = new ProductoEntity();
            p3.setNombreProducto("Quinua Negra Premium");
            p3.setPrecioUsd(new BigDecimal("5.00"));
            p3.setStockkg(150);
            p3.setCategoria("Quinua");
            p3.setCorreoNotificacion("premium@agrosmart.com.ec");

            // Productos Invalidos
            // Precio es < 0
            ProductoEntity p4 = new ProductoEntity();
            p4.setNombreProducto("Muestra Gratis de Quinua");
            p4.setPrecioUsd(new BigDecimal("0.00"));
            p4.setStockkg(50);
            p4.setCategoria("Quinua");
            p4.setCorreoNotificacion("marketing@agrosmart.com.ec");

            // Lista de correo vacia
            ProductoEntity p5 = new ProductoEntity();
            p5.setNombreProducto("Marina de Quinua a Granel");
            p5.setPrecioUsd(new BigDecimal("2.00"));
            p5.setStockkg(1000);
            p5.setCategoria("Quinua");
            p5.setCorreoNotificacion("");

            repository.saveAll(List.of(p1, p2, p3, p4, p5));
            System.out.println("Datos de Quinua sembrados exitosamente");
        }
    }
}
