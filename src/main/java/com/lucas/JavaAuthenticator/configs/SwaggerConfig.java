package com.lucas.JavaAuthenticator.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("Registro e Autenticação de Usuários")
                    .version("v1.0")
                    .description("Sistema de registro e autenticação de usuários, desenvolvido com Spring Security 6, integrando autenticação baseada em JWT (JSON Web Tokens) e OAUTH 2.0.")
            );
  }
}
