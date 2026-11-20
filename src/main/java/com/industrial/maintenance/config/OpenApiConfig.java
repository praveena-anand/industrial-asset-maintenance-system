package com.industrial.maintenance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maintenanceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Industrial Asset & Maintenance Management API")
                .description("Backend for managing industrial equipment, preventive maintenance, "
                        + "breakdown service requests, maintenance history and analytics.")
                .version("1.0.0"));
    }
}