package com.example.apicustomer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "API Customer", version = "1.0", description = "API REST para gerenciamento de clientes"))
public class ApicustomerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApicustomerApplication.class, args);
	}

}
