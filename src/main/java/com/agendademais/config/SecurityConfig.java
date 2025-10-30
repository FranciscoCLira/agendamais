package com.agendademais.config;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class SecurityConfig {

	/**
	 * Development-only SecurityFilterChain for the H2 console.
	 *
	 * <p>
	 * This filter chain is intentionally permissive: it disables CSRF,
	 * allows framing, and permits all requests under <code>/h2-console/**</code>.
	 * It must only be active in the {@code dev} Spring profile. The bean is
	 * protected with {@code @Profile("dev")} to avoid accidental exposure in
	 * production or other environments where authentication and CSRF are
	 * required.
	 */

	@Bean
	@Profile("dev")
	@Order(0)
	public SecurityFilterChain h2ConsoleSecurity(HttpSecurity http) throws Exception {
		// Dedicated chain for H2 console: allow everything and disable CSRF/frames.
		http.securityMatcher(new AntPathRequestMatcher("/h2-console/**"))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frame -> frame.disable()));
		return http.build();
	}

	@Bean
	@Profile("dev")
	/**
	 * Development-only WebSecurityCustomizer to completely bypass Spring Security
	 * for H2 console static resources. This prevents the main filter chain from
	 * processing console requests when the {@code dev} profile is active.
	 *
	 * Note: keep this bean under {@code dev} profile only. Do not enable in
	 * production.
	 */
	public WebSecurityCustomizer webSecurityCustomizer() {
		// Completely bypass Spring Security for H2 console resources
		return (web) -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Value("${app.security.requireAdmin:true}")
	private boolean requireAdmin;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		if (requireAdmin) {
			http.authorizeHttpRequests(auth -> auth
					.requestMatchers(org.springframework.http.HttpMethod.GET,
							"/administrador/atividades/deletar/**")
					.permitAll()
					.requestMatchers("/admin/**").hasRole("ADMIN")
					.anyRequest().permitAll());
			// In secure mode, enable form login and keep CSRF enabled (best practice)
			http.formLogin();

		} else {
			http.authorizeHttpRequests(auth -> auth
					.anyRequest().permitAll());
			// In dev permissive mode, disable form login for convenience
			http.formLogin(form -> form.disable());
			// Keep CSRF disabled in permissive dev mode to avoid test friction for manual
			// requests
			http.csrf(csrf -> csrf.disable());
		}

		// Common config
		http.logout(logout -> logout.permitAll())
				.headers(headers -> headers.frameOptions(frame -> frame.disable()))
				.exceptionHandling(eh -> eh.accessDeniedPage("/acesso-negado"));

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
