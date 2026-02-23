package com.example.shop.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.shop.model.Product;

@Mapper
public interface ProductMapper {
    List<Product> findAllActive();
    void insertProduct(Product product);
}