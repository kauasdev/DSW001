package br.senac.tads.dsw.dados_pessoais.seguranca;

 // Represnta o corpo da requsição POST /login
public class LoginDto {

	private  String username;
	private String senha;

	public	LoginDto() {}

	 public String getUsername() {return username;}
	 public void setUsername(String username) {this.username = username;}

	 public String getSenha() {return senha;}
	 public void setSenha(String senha) {this.senha = senha;}

}
