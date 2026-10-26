package br.senac.tads.dsw.dados_pessoais.repositorio;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.senac.tads.dsw.dados_pessoais.entidade.PessoaEntity;



@Repository
public interface PessoaRepository extends JpaRepository<PessoaEntity, Integer>{


	Optional<PessoaEntity> findByUsername(String username);

	boolean existsByUsername(String username);

	// Versao JPQL: opera sobre campos da entidade Java (nao sobre colunas SQL)
	@Query("""
    SELECT p FROM PessoaEntity p
    WHERE LOWER(p.username) LIKE LOWER(CONCAT('%', :termo, '%'))
       OR LOWER(p.nome)     LIKE LOWER(CONCAT('%', :termo, '%'))
       OR LOWER(p.email)    LIKE LOWER(CONCAT('%', :termo, '%'))
    """)
	List<PessoaEntity> buscarPorTermo(@Param("termo") String termo);



}
