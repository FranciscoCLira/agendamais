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
	            .requestMatchers("/**").permitAll()
	            .requestMatchers("/h2-console/**").permitAll()
//	            .requestMatchers("/login", "/login/**", "/css/**", "/js/**", "/images/**").permitAll() // login e estáticos
//	            .requestMatchers("/cadastro-usuario", "/cadastro-pessoa", "/api/locais/**").permitAll()
//	            .requestMatchers("/cadastro-relacionamentos").permitAll()
//	            .requestMatchers("/menus/**").permitAll()
//	            .requestMatchers("/**").permitAll()
//	            .requestMatchers("/acesso/**", "/cadastro-usuario", "/css/**", "/js/**", "/images/**").permitAll()
//	            .requestMatchers("/*").permitAll()
//	            .requestMatchers("/login*").permitAll()
	            .anyRequest().authenticated()
	        )
	        .formLogin(form -> form
	            .loginPage("/login").permitAll()
	            .loginPage("/acesso").permitAll()
	            .defaultSuccessUrl("/", true)
	        )
	        .logout(logout -> logout.permitAll())
	        .csrf(csrf -> csrf
	            .ignoringRequestMatchers("/h2-console/**")
	        )
	        .headers(headers -> headers
	            .frameOptions(frame -> frame.disable())
	        )
	        .exceptionHandling(eh -> eh
	                .accessDeniedPage("/acesso-negado")
	            );	        
	    return http.build();
	}

}

// Desabilitado acima o login de segurança do Spring

//@Configuration
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//                .anyRequest().permitAll()
//            )
//            .formLogin();
//        return http.build();
//    }
//}
