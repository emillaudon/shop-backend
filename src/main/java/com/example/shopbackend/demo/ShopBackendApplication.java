package com.example.shopbackend.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopBackendApplication.class, args);
		System.out.println("DB_PASSWORD = " + System.getenv("DB_PASSWORD"));

	}

}
