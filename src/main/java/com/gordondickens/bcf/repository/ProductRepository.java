package com.gordondickens.bcf.repository;

import com.gordondickens.bcf.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ProductRepository extends JpaSpecificationExecutor<Product>, JpaRepository<Product, Long> {

        //public interface ProductRepository extends CrudRepository<Product, Long> {
        @Query("FROM Product")
        List<Product> findAllProducts();

        // @Query("FROM Product p where p.productId = :productId")
        // Product findByProductId(@Param("productId") String productId);
        @Query
        Product findByProductId(String productId);

        @Modifying
        @Query("update Product p set p.description = :description where p.productId = :productId")
        Integer setNewDescriptionForProduct(@Param("productId") String productId,
                                            @Param("description") String description);

        // @Modifying
        // @Query("update Product p set p.description = ?2 where p.productId = ?1")
        // Integer setNewDescriptionForProduct(String productId, String
        // description);

}
