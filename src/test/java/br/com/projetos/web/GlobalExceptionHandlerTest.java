package br.com.projetos.web;

import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resourceNotFound_retorna_404() throws Exception {
        mockMvc.perform(get("/__probe/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("recurso ausente"))
                .andExpect(jsonPath("$.path").value("/__probe/not-found"));
    }

    @Test
    void businessRule_retorna_400() throws Exception {
        mockMvc.perform(get("/__probe/business").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("regra violada"));
    }

    @RestController
    static class ExceptionProbeController {

        @GetMapping("/__probe/not-found")
        void notFound() {
            throw new ResourceNotFoundException("recurso ausente");
        }

        @GetMapping("/__probe/business")
        void business() {
            throw new BusinessRuleException("regra violada");
        }
    }
}
