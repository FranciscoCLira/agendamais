package com.agendademais.test;

import com.agendademais.AgendaMaisApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Classe de teste para verificar problemas na inicialização
 */
public class TestStartup {
    public static void main(String[] args) {
        try {
            System.out.println("=== INICIANDO TESTE DE STARTUP ===");
            ConfigurableApplicationContext context = SpringApplication.run(AgendaMaisApplication.class, args);
            System.out.println("=== APLICAÇÃO INICIADA COM SUCESSO ===");
            context.close();
        } catch (Exception e) {
            System.err.println("=== ERRO NA INICIALIZAÇÃO ===");
            e.printStackTrace();
            System.err.println("=== FIM DO ERRO ===");
        }
    }
}
