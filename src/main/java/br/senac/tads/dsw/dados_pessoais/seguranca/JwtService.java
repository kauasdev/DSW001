package br.senac.tads.dsw.dados_pessoais.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiracao-segundos:3600}")
	private long expiracaoSegundos;


	public String gerarToken(UsuarioSistema usuario) {
		List<String> roles = new ArrayList<>();
		for (GrantedAuthority authority : usuario.getAuthorities()) {
			roles.add(authority.getAuthority());
		}

		Instant agora = Instant.now();
		Instant expiracao = agora.plusSeconds(expiracaoSegundos);

		// JWTClaimsSet representa o payload do token — o conjunto de "claims" (afirmações)
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.subject(usuario.getUsername())
			.issuer("dados-pessoais-api")
			.issueTime(Date.from(agora))
			.expirationTime(Date.from(expiracao))
			.claim("roles", roles)
			.build();

		return jwtEncode(claims);
	}

	private String jwtEncode(JWTClaimsSet claims) {
		try {

			byte[] keyBytes = MessageDigest.getInstance("SHA-256")
				.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
			MACSigner signer = new MACSigner(keyBytes);


			SignedJWT jwt = new SignedJWT(
				new JWSHeader(JWSAlgorithm.HS256),  // HS256 = HMAC-SHA256
				claims
			);


			jwt.sign(signer);


			return jwt.serialize();

		} catch (JOSEException | NoSuchAlgorithmException e) {
			throw new RuntimeException("Erro ao assinar token JWT", e);
		}
	}
}
