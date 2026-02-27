package com.example.shop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopController {

    @GetMapping("/")
    public String shoplist() {
        return "전체목록 요청받음";
    }

      @GetMapping("/detail")
    public String detail() {
        return "상세보기 요청받음";
    }

      @GetMapping("/updateWishlist")
    public String updateWishlist() {
        return "찜추가 요청받음";
    }

      @GetMapping("/wishlist")
    public String wishlist() {
        return "찜목록 요청받음";
    }

     @PostMapping("/updateCart")
     public String updateCart() {
         return "장바구니 담기 요청받음";
     }
   
    @GetMapping("/cartlist")
     public String cartlist() {
         return "장바구니목록 요청받음";
     }
    
      @PostMapping("/order")
     public String order() {
         return "주문추가 요청받음";
     }

     @GetMapping("/orderlist")
     public String orderlist() {
         return "주문목록 요청받음";
     }
     
      @PostMapping("/checkout")
     public String checkout() {
         return "주문하기 요청받음";
     }



    
    
    
    
}
