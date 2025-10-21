package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.services.CryptoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InstituicaoSmtpControllerTest {

    @Test
    public void testEndpointReturnsString() {
        InstituicaoSmtpController ctrl = new InstituicaoSmtpController();
        Instituicao inst = new Instituicao();
        inst.setSmtpHost("smtp.example.com");
        inst.setSmtpPort(587);
        inst.setSmtpUsername("u");
        inst.setSmtpPassword("p");
        inst.setSmtpSsl(false);
        // inject crypto service
        try {
            java.lang.reflect.Field f = InstituicaoSmtpController.class.getDeclaredField("cryptoService");
            f.setAccessible(true);
            f.set(ctrl, new CryptoService());
        } catch (Exception e) {
            fail(e);
        }
        String res = ctrl.testSmtp(1L, inst);
        assertNotNull(res);
    }
}
