package com.dlcorrea.agrosmart.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dlcorrea.agrosmart.domain.Producto;
import com.dlcorrea.agrosmart.domain.ProductoFilters;
import com.dlcorrea.agrosmart.domain.ProductoMapper;
import com.dlcorrea.agrosmart.exception.ProductoNoEncontradoException;
import com.dlcorrea.agrosmart.repository.ProductoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * ProductoService
 */
@Service
public class ProductoService {

    private final ProductoRepository repository;

    private static final Producto PRODUCTO_GENERICO = new Producto(0L, "PRODUCTO GENERICO DE RESPALDO", "NA",
            new BigDecimal("1.00"), List.of("soporte@agrosmart.com.ec"));

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Flux<Producto> obtenerProductosComercializables() {
        return Mono.fromCallable(repository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(ProductoMapper::toDominio)
                .map(ProductoFilters.A_MAYUSCULAS)
                .filter(ProductoFilters.IS_VALID)
                .doOnNext(ProductoFilters.LOG_PRODUCTO)
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {
        return Mono.fromCallable(() -> repository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .map(ProductoMapper::toDominio)
                .switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id)));
    }
}
