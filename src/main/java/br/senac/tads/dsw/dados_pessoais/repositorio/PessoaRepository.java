package br.senac.tads.dsw.dados_pessoais.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.senac.tads.dsw.dados_pessoais.entidade.PessoaEntity;

@Repository
public interface PessoaRepository extends JpaRepository<PessoaEntity, Integer>{

	Optional<PessoaEntity> findByUsername(String username);

	boolean existsByUsername(String username);

}
