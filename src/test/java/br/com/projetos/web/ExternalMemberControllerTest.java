package br.com.projetos.web;

import br.com.projetos.config.SecurityConfig;
import br.com.projetos.domain.MemberRole;
import br.com.projetos.dto.MemberCreateRequest;
import br.com.projetos.dto.MemberResponse;
import br.com.projetos.service.MemberExternalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExternalMemberController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class ExternalMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberExternalService memberExternalService;

    @MockBean
    private UserDetailsService userDetailsService;

    private static final MemberResponse SAMPLE =
            new MemberResponse(1L, "João", MemberRole.FUNCIONARIO, Instant.parse("2025-01-01T00:00:00Z"));

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_retorna_201() throws Exception {
        when(memberExternalService.create(any(MemberCreateRequest.class))).thenReturn(SAMPLE);

        mockMvc.perform(post("/api/externo/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"João\",\"atribuicao\":\"FUNCIONARIO\"}".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"));

        verify(memberExternalService).create(any(MemberCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_com_user_negado() throws Exception {
        mockMvc.perform(post("/api/externo/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"atribuicao\":\"FUNCIONARIO\"}".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void get_por_id() throws Exception {
        when(memberExternalService.get(7L)).thenReturn(SAMPLE);

        mockMvc.perform(get("/api/externo/membros/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atribuicao").value("FUNCIONARIO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_retorna_array() throws Exception {
        when(memberExternalService.listAll()).thenReturn(List.of(SAMPLE));

        mockMvc.perform(get("/api/externo/membros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(memberExternalService).listAll();
    }
}
