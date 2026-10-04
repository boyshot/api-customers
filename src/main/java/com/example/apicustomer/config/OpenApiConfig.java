package com.example.apicustomer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String serverUrl = System.getenv("SERVER_URL");
        if (serverUrl == null) {
            serverUrl = "http://localhost:9090";
        }

        return new OpenAPI()
            .info(new Info()
                .title("API Customer")
                .version("1.0")
                .description("API REST para gerenciamento de clientes")
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org")))
            .servers(List.of(
                new Server().url(serverUrl).description("Default Server")
            ));
    }
}
