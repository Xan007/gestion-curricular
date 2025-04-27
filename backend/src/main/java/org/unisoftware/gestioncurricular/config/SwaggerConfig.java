package org.unisoftware.gestioncurricular.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        // Definir el esquema de seguridad
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(SECURITY_SCHEME_NAME)
                .description("Ingrese el token JWT en el campo 'Authorization'. Prefijo 'Bearer' no necesario.");


        // Agregar el esquema de seguridad a la configuración global
        return new OpenAPI()
                .info(new Info()
                        .title("Gestión Curricular API")
                        .version("1.0")
                        .description("API para la gestión de planes curriculares y programas académicos.")
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .schemaRequirement(SECURITY_SCHEME_NAME, securityScheme);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("org.unisoftware.gestioncurricular.controller", "org.unisoftware.gestioncurricular.security")
                .pathsToExclude("/repository/**", "/entity/**")
                .build();
    }
}