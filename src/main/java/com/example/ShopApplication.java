package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// @SpringBootApplication
// public class ShopApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(ShopApplication.class, args);
// 	}

// }

// ShopApplication.java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // DB 자동설정 제외
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
