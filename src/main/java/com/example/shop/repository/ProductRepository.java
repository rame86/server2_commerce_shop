package com.example.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shop.entity.Product;
import com.example.shop.entity.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByGoodsTypeAndStatusTrue(ProductCategory goodsType);
}