package br.senac.tads.dsw.dados_pessoais.seguranca;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.senac.tads.dsw.dados_pessoais.seguranca.UsuarioSistema;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioSistemaService implements UserDetailsService {

	private final Map<String, UserDetails> usuarios;

	public UsuarioSistemaService(PasswordEncoder passwordEncoder) {
		this.usuarios = new HashMap<>();

		// admin — acesso total: ROLE_ADMIN, ROLE_GERENTE e ROLE_USER
		this.usuarios.put("admin", new UsuarioSistema(
			"admin",
			passwordEncoder.encode("Abcd%12345"),
			List.of(
				new SimpleGrantedAuthority("ROLE_ADMIN"),
				new SimpleGrantedAuthority("ROLE_GERENTE"),
				new SimpleGrantedAuthority("ROLE_USER")
			)
		));


		this.usuarios.put("user1", new UsuarioSistema(
			"user1",
			passwordEncoder.encode("Abcd%12345"),
			List.of(
				new SimpleGrantedAuthority("ROLE_GERENTE"),
				new SimpleGrantedAuthority("ROLE_USER")
			)
		));

		this.usuarios.put("user2", new UsuarioSistema(
			"user2",
			passwordEncoder.encode("Abcd%12345"),
			List.of(
				new SimpleGrantedAuthority("ROLE_USER")
			)
		));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails usuario = usuarios.get(username);
		if (usuario == null) {
			throw new UsernameNotFoundException("Usuário não encontrado: " + username);
		}
		return usuario;
	}
}
