package com.dlcorrea.agrosmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dlcorrea.agrosmart.entity.ProductoEntity;

/**
 * ProductoRepository
 */
@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
}
