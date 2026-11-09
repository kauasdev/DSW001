package br.senac.tads.dsw.dados_pessoais.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize e @PostAuthorize nos controllers
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}


	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// Origens permitidas — substitua pela URL real do frontend em produção
		config.setAllowedOrigins(List.of("http://localhost:3000", "https://meu-frontend.com"));

		// Métodos HTTP que o cliente pode usar
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		// Headers que o cliente pode enviar — "Authorization" é necessário para o Bearer token
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);  // aplica a todos os caminhos
		return source;
	}


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http

			.csrf(csrf -> csrf.disable())


			.headers(headers -> headers
				.frameOptions(frame -> frame.disable()))

			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authorizeHttpRequests(auth -> auth

					.requestMatchers(HttpMethod.POST, "/login").permitAll()

					.requestMatchers("/h2-console/**").permitAll()

					.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
					.permitAll()
					// 2) ADICIONAR ESTAS LINHAS NO CÓDIGO EXISTENTE
					// /me e os endpoints de acesso exigem autenticação (cobertos por anyRequest)
					.requestMatchers("/me").authenticated()
					// Permitir acesso aos arquivos das telas - antecipando config da etapa 7.7
					.requestMatchers("/*.html", "/*.css", "/*.js").permitAll()



					// Leitura de pessoas: público
					.requestMatchers(HttpMethod.GET, "/pessoas", "/pessoas/**").permitAll()
					// Escrita exige autenticação (o @PreAuthorize no controller exigirá ROLE_ADMIN)
					.requestMatchers(HttpMethod.POST, "/pessoas", "/pessoas/**").authenticated()
					.requestMatchers(HttpMethod.PUT, "/pessoas/**").authenticated()
					.requestMatchers(HttpMethod.DELETE, "/pessoas/**").authenticated()
					.requestMatchers("/error").permitAll()
					.anyRequest().authenticated()
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


		return http.build();
	}


	@Bean
	public AuthenticationManager authenticationManager(
		UserDetailsService userDetailsService,
		PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
