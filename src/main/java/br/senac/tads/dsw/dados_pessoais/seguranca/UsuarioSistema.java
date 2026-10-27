package br.senac.tads.dsw.dados_pessoais.seguranca;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

//Representa um usuário no contexto do Spinrg Security.
//UserDatails é a interface padrão que o framework usa para carregar e inspecionar usuários.
//Ao implentá-la, esta classe "fala a língua" do Spring Security.
public class UsuarioSistema implements UserDetails{

	private final String username;
	private final String password;
	private final List<GrantedAuthority> authorities;

	public UsuarioSistema(String username, String password, List<GrantedAuthority> authorities){
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}

	@Override
	public String getUsername() {return username;}

	@Override
	public String getPassword() {return password;}

	//getAuthorities() retorna a lista de permissões (roles) do usuário.
	// GrantedAuthority é uma interface; SimpleGrantedAuthority é a implementação padrão.

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {return authorities;}

	@Override public boolean isAccountNonExpired() {return true;}
	@Override public boolean isAccountNonLocked()  {return  true;}
	@Override public boolean isCredentialsNonExpired()  {return  true;}
	@Override public boolean isEnabled()  {return  true;}


















}


