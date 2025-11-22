package com.SwitchBoard.WorkspaceService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@Slf4j
public class SwaggerConfig {

    @Value("${app.swagger.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${app.swagger.prod-url:https://your-production-url.com}")
    private String prodUrl;

    @Bean
    public OpenAPI openAPI() {
        log.info("SwaggerConfig :: openAPI :: Initializing Swagger OpenAPI configuration");

        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact()
                .email("support@switchboard.com")
                .name("SwitchBoard Team")
                .url("https://www.switchboard.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Workspace Service API")
                .version("1.0.0")
                .contact(contact)
                .description("This API provides endpoints for managing workspaces, tasks, users, and learning roadmaps. " +
                           "It's designed as a comprehensive learning management system similar to Notion/Jira for educational purposes.")
                .termsOfService("https://www.switchboard.com/terms")
                .license(mitLicense);

        log.info("SwaggerConfig :: openAPI :: OpenAPI configuration initialized successfully");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}