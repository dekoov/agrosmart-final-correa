package com.dlcorrea.agrosmart.service;

import java.time.Duration;
import org.springframework.stereotype.Service;
import com.dlcorrea.agrosmart.ia.AgroSmartAIService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * PublicidadService
 */
@Service
public class PublicidadService {

    private final AgroSmartAIService aiService;

    public PublicidadService(AgroSmartAIService aiService) {
        this.aiService = aiService;
    }

    public Mono<String> generarPublicidad(String producto, String audiencia) {
        return Mono.fromCallable(() -> aiService.generarPublicidad(producto, audiencia))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.just(
                        "Publicidad no disponible en este momento (" + e.getClass().getSimpleName() + ")"));
    }
}
