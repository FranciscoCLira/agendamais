package com.agendademais.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Test configuration to provide simple beans that are normally
 * environment-dependent.
 */
@Configuration
public class TestMailConfiguration {

    @Bean
    public JavaMailSender javaMailSender() {
        // Provide a no-op JavaMailSender for tests. Tests that need to assert mail
        // behavior
        // should provide their own mocks if necessary.
        return new JavaMailSenderImpl();
    }
}
