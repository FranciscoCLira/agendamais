package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.InstituicaoRepository;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import com.agendademais.testutil.GreenMailTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "app.security.requireAdmin=true" })
public class InstituicaoSmtpControllerConnectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @RegisterExtension
    static GreenMailExtension greenMail = GreenMailTestUtil.createGreenMail();

    @Test
    public void testConnectionEndpointReturnsOkForValidCredentials() throws Exception {
        // Create an Instituicao with test SMTP settings matching GreenMail
        Instituicao i = new Instituicao();
        i.setNomeInstituicao("ConnTest");
        i.setSmtpHost(GreenMailTestUtil.getHost());
        i.setSmtpPort(GreenMailTestUtil.getPort());
        i.setSmtpUsername("user@local");
        i.setSmtpPassword("pass");
        i.setSmtpSsl(false);
        i = instituicaoRepository.save(i);

        // Ensure GreenMail knows the user so authentication succeeds
        greenMail.getUserManager().createUser(i.getSmtpUsername(), i.getSmtpUsername(), i.getSmtpPassword());

        mockMvc.perform(post("/admin/instituicao/smtp/" + i.getId() + "/test")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("smtpHost", i.getSmtpHost())
                .param("smtpPort", String.valueOf(i.getSmtpPort()))
                .param("smtpUsername", i.getSmtpUsername())
                .param("smtpPassword", i.getSmtpPassword())
                .param("smtpSsl", String.valueOf(i.getSmtpSsl())))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("OK")));
    }
}
