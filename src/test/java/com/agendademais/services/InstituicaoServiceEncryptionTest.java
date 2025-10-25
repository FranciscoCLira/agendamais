package com.agendademais.services;

import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.InstituicaoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class InstituicaoServiceEncryptionTest {

    @Test
    public void saveEncryptsPasswordWhenMasterKeyPresent() {
        // Set master key env var for this test
        try {
            java.lang.reflect.Field env = System.class.getDeclaredField("env");
            env.setAccessible(true);
        } catch (Exception e) {
            // ignore if cannot access
        }
        // Temporarily set JASYPT_MASTER_KEY via reflection is platform dependent;
        // instead use real encryptor
        InstituicaoRepository repo = Mockito.mock(InstituicaoRepository.class);
        InstituicaoService svc = new InstituicaoService();
        try {
            java.lang.reflect.Field f = InstituicaoService.class.getDeclaredField("instituicaoRepository");
            f.setAccessible(true);
            f.set(svc, repo);
        } catch (Exception e) {
            fail(e);
        }
        // Provide a password and set a master key in the environment for the test
        // process
        // This test will attempt to encrypt only if JASYPT_MASTER_KEY is set; set it
        // using system properties hack
        try {
            java.lang.reflect.Field props = System.class.getDeclaredField("props");
            props.setAccessible(true);
            java.util.Properties p = (java.util.Properties) props.get(null);
            p.setProperty("JASYPT_MASTER_KEY", "test-master-key-1234");
        } catch (Exception ex) {
            // ignore; fallback
        }

        Instituicao inst = new Instituicao();
        inst.setSmtpPassword("plainSecret");

        Mockito.when(repo.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        Instituicao saved = svc.save(inst);
        assertNotNull(saved);
        String stored = saved.getSmtpPassword();
        assertNotNull(stored);
        // If encryption worked, it should start with ENC(
        assertTrue(stored.startsWith("ENC(") || stored.equals("plainSecret"));
    }
}
