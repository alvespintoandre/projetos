package br.com.projetos.web;

import br.com.projetos.config.SecurityConfig;
import br.com.projetos.domain.AppUserRole;
import br.com.projetos.dto.AppUserCreateRequest;
import br.com.projetos.dto.AppUserResponse;
import br.com.projetos.service.AppUserService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserService appUserService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_ok() throws Exception {
        when(appUserService.listAll()).thenReturn(List.of(new AppUserResponse(1L, "admin", AppUserRole.ADMIN)));

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"));
    }

    @Test
    void list_sem_autenticacao_retorna_401() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void list_com_user_comum_retorna_403() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_retorna_201() throws Exception {
        when(appUserService.create(any(AppUserCreateRequest.class)))
                .thenReturn(new AppUserResponse(2L, "novo", AppUserRole.USER));

        mockMvc.perform(post("/api/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"username\":\"novo\",\"password\":\"senha1234\",\"role\":\"USER\"}")
                                .getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("novo"));

        verify(appUserService).create(any(AppUserCreateRequest.class));
    }
}
