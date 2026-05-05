package br.com.projetos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Info", description = "Informações básicas da API")
public class RootController {

    @Value("${spring.application.name:portfolio-projetos}")
    private String applicationName;

    @GetMapping("/")
    @Operation(summary = "Informações da API (sem autenticação)")
    public Map<String, Object> root() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", applicationName);
        body.put("swaggerUi", "/swagger-ui.html");
        body.put("openApi", "/api-docs");
        body.put("health", "/actuator/health");
        return body;
    }
}
