package com.dlcorrea.agrosmart.exception;

/**
 * ProductoNoEncontradoException
 */
public class ProductoNoEncontradoException extends RuntimeException {

    private final ErrorCode errorCode;

    public ProductoNoEncontradoException(Long id) {
        super("El producto con ID " + id + " no fue encontrado en el catálogo.");
        this.errorCode = ErrorCode.PRODUCTO_NO_ENCONTRADO;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
