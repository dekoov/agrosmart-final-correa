package com.dlcorrea.agrosmart.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.dlcorrea.agrosmart.entity.ProductoEntity;

/**
 * ProductoMapper
 */
public class ProductoMapper {

    public static Producto toDominio(ProductoEntity entity) {
        if (entity == null) {
            return null;
        }

        String correosString = entity.getCorreoNotificacion();

        List<String> correosList;
        if (correosString != null && !correosString.trim().isEmpty()) {
            correosList = Arrays.stream(correosString.split(","))
                    .map(String::trim)
                    .filter(correo -> !correo.isEmpty())
                    .collect(Collectors.toList());
        } else {
            correosList = List.of();
        }

        return new Producto(
                entity.getIdProducto(),
                entity.getNombreProducto(),
                entity.getCategoria(),
                entity.getPrecioUsd(),
                correosList);
    }
}
