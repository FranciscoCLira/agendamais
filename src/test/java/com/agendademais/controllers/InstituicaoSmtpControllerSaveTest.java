package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.InstituicaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "app.security.requireAdmin=true" })
public class InstituicaoSmtpControllerSaveTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Test
    public void saveEncryptsPassword() throws Exception {
        // Ensure env master key exists for this test process
        // Set via system property fallback if not present (best-effort):
        try {
            if (System.getenv("JASYPT_MASTER_KEY") == null) {
                System.setProperty("JASYPT_MASTER_KEY", "test-master-key-abc");
            }
        } catch (Exception ignored) {
        }

        Instituicao inst = new Instituicao();
        inst.setNomeInstituicao("Salvar Teste");
        inst = instituicaoRepository.save(inst);

        mockMvc.perform(post("/admin/instituicao/smtp/" + inst.getId())
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("smtpHost", "smtp.test.local")
                .param("smtpPort", "25")
                .param("smtpUsername", "user@test")
                .param("smtpPassword", "plainpass")
                .param("smtpSsl", "false")).andExpect(status().is3xxRedirection());

        Instituicao saved = instituicaoRepository.findById(inst.getId()).orElseThrow();
        assertThat(saved.getSmtpPassword()).isNotNull();
        assertThat(saved.getSmtpPassword()).startsWith("ENC(")
                .as("Password should be stored encrypted when master key present");
    }
}
