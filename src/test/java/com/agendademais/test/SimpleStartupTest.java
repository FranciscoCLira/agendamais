package com.agendademais.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class SimpleStartupTest {

    @Test
    public void contextLoads() {
        // Teste simples para verificar se o contexto carrega
        System.out.println("=== CONTEXTO CARREGADO COM SUCESSO ===");
    }
}
