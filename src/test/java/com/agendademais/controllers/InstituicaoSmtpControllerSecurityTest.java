package com.agendademais.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.entities.Instituicao;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "app.security.requireAdmin=true" })
public class InstituicaoSmtpControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Test
    public void adminEndpointForbiddenForAnonymous() throws Exception {
        // When security requires authentication, anonymous users are redirected to the
        // login page (302)
        mockMvc.perform(get("/admin/instituicao/smtp/1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void adminEndpointAllowedForAdmin() throws Exception {
        // ensure a record exists so the template can render
        Instituicao i = new Instituicao();
        i.setNomeInstituicao("Teste");
        i = instituicaoRepository.save(i);

        mockMvc.perform(get("/admin/instituicao/smtp/" + i.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
