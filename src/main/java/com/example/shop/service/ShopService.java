package com.example.shop.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.model.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopService {
    // 1. 스프링이 만들어준 '소문자' productMapper를 사용해야 합니다.
    private final ProductMapper productMapper;

    public List<Product> getActiveProducts() {
        // ProductMapper.findAllActive() (X) -> productMapper.findAllActive() (O)
        return productMapper.findAllActive();
    }

    @Transactional
    public void addProduct(Product product) {
        // ProductMapper.insertProduct(product) (X) -> productMapper.insertProduct(product) (O)
        productMapper.insertProduct(product);
    }
}