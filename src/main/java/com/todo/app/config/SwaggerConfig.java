package com.todo.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${swagger.auto-open:true}")
    private boolean autoOpen;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Todo App API")
                        .description("API Documentation for Todo Application")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openSwaggerUi() {
        if (!autoOpen) {
            return;
        }

        String formattedContextPath = contextPath == null ? "" : contextPath.trim();
        if (!formattedContextPath.isEmpty() && !formattedContextPath.startsWith("/")) {
            formattedContextPath = "/" + formattedContextPath;
        }
        if (formattedContextPath.endsWith("/")) {
            formattedContextPath = formattedContextPath.substring(0, formattedContextPath.length() - 1);
        }

        String url = "http://localhost:" + port + formattedContextPath + "/swagger-ui/index.html";
        System.out.println("Application started. Opening Swagger UI at: " + url);

        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.contains("win")) {
                rt.exec(new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                rt.exec(new String[]{"open", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                rt.exec(new String[]{"xdg-open", url});
            } else {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to auto-open Swagger UI: " + e.getMessage());
        }
    }
}
