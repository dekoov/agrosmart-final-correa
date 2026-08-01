package com.dlcorrea.agrosmart.exception;

import java.time.LocalDateTime;

/**
 * ErrorResponse
 */
public record ErrorResponse(
        ErrorCode codigo,
        String mensaje,
        LocalDateTime marcaDeTiempo
) {
    public ErrorResponse {
        if (marcaDeTiempo == null) {
            marcaDeTiempo = LocalDateTime.now();
        }
    }
}
