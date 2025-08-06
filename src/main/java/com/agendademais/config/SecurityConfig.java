package com.agendademais.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/**").permitAll() // Permitir tudo - desabilitar segurança do Spring
				)
				.formLogin(form -> form.disable()) // Desabilitar completamente o form login do Spring
				.logout(logout -> logout.permitAll())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/h2-console/**"))
				.headers(headers -> headers
						.frameOptions(frame -> frame.disable()))
				.exceptionHandling(eh -> eh
						.accessDeniedPage("/acesso-negado"));
		return http.build();
	}

}

// Desabilitado acima o login de segurança do Spring

// @Configuration
// public class SecurityConfig {
// @Bean
// public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// http
// .authorizeHttpRequests(auth -> auth
// .requestMatchers("/admin/**").hasRole("ADMIN")
// .anyRequest().permitAll()
// )
// .formLogin();
// return http.build();
// }
// }
