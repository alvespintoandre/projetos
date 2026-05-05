package br.com.projetos.web;

import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.domain.RiskLevel;
import br.com.projetos.dto.ProjectCreateRequest;
import br.com.projetos.dto.ProjectResponse;
import br.com.projetos.dto.StatusTransitionRequest;
import br.com.projetos.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectController.class)
@Import(SpringDataWebAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    private static final ProjectResponse SAMPLE = new ProjectResponse(
            1L,
            "Projeto",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 1),
            null,
            BigDecimal.TEN,
            "descricao",
            new ProjectResponse.MemberSummary(10L, "Geraldo", "GERENTE"),
            ProjectStatus.EM_ANALISE,
            RiskLevel.BAIXO,
            Set.of(new ProjectResponse.MemberSummary(20L, "Fernando", "FUNCIONARIO"))
    );

    @Test
    void list_retorna_pagina_json() throws Exception {
        when(projectService.list(isNull(), isNull(), any())).thenReturn(new PageImpl<>(List.of(SAMPLE)));

        mockMvc.perform(get("/api/projetos").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Projeto"));
    }

    @Test
    void get_por_id() throws Exception {
        when(projectService.get(1L)).thenReturn(SAMPLE);

        mockMvc.perform(get("/api/projetos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    @Test
    void create_retorna_201() throws Exception {
        when(projectService.create(any(ProjectCreateRequest.class))).thenReturn(SAMPLE);

        String body = """
                {
                  "nome": "Novo",
                  "dataInicio": "2025-01-15",
                  "previsaoTermino": "2025-12-01",
                  "orcamentoTotal": 1000,
                  "descricao": "texto",
                  "gerenteId": 10,
                  "membrosAlocadosIds": [20]
                }
                """;

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto"));

        verify(projectService).create(any(ProjectCreateRequest.class));
    }

    @Test
    void transition_patch() throws Exception {
        when(projectService.transitionStatus(eq(5L), any(StatusTransitionRequest.class))).thenReturn(SAMPLE);

        mockMvc.perform(patch("/api/projetos/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoStatus\":\"ANALISE_REALIZADA\"}"))
                .andExpect(status().isOk());

        verify(projectService).transitionStatus(eq(5L), any(StatusTransitionRequest.class));
    }

    @Test
    void delete_retorna_204() throws Exception {
        mockMvc.perform(delete("/api/projetos/9"))
                .andExpect(status().isNoContent());

        verify(projectService).delete(9L);
    }

    @Test
    void update_put() throws Exception {
        when(projectService.update(eq(3L), any())).thenReturn(SAMPLE);

        mockMvc.perform(put("/api/projetos/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Carlos",
                                  "dataInicio": "2025-02-01",
                                  "previsaoTermino": "2025-08-01",
                                  "orcamentoTotal": 500,
                                  "descricao": "descricao",
                                  "gerenteId": 1
                                }
                                """))
                .andExpect(status().isOk());

        verify(projectService).update(eq(3L), any());
    }

    @Test
    void updateAllocacoes_put() throws Exception {
        when(projectService.updateAllocations(eq(4L), any())).thenReturn(SAMPLE);

        mockMvc.perform(put("/api/projetos/4/alocacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"membrosIds\":[2,3]}"))
                .andExpect(status().isOk());

        verify(projectService).updateAllocations(eq(4L), any());
    }
}
