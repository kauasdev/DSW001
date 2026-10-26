
package br.senac.tads.dsw.dados_pessoais;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.senac.tads.dsw.dados_pessoais.entidade.ConhecimentoEntity;
import br.senac.tads.dsw.dados_pessoais.entidade.PessoaEntity;
import br.senac.tads.dsw.dados_pessoais.repositorio.ConhecimentoRepository;
import br.senac.tads.dsw.dados_pessoais.repositorio.PessoaRepository;
import jakarta.annotation.PostConstruct;

// @Primary: quando ha mais de uma implementacao de PessoaService,
// o Spring injeta ESTA por padrao (sem precisar alterar o controller)
@Primary
@Service
public class PessoaServiceJpaImpl implements PessoaService {

	private final PessoaRepository pessoaRepository;
	private final ConhecimentoRepository conhecimentoRepository;

	public PessoaServiceJpaImpl(PessoaRepository pessoaRepository,
								ConhecimentoRepository conhecimentoRepository) {
		this.pessoaRepository = pessoaRepository;
		this.conhecimentoRepository = conhecimentoRepository;
	}

	// =========================================================
	// Metodos auxiliares: conversao entre Entity e DTO
	// =========================================================

	// PessoaEntity -> PessoaDto
	private PessoaDto toDto(PessoaEntity entity) {
		PessoaDto dto = new PessoaDto();
		dto.setId(entity.getId());
		dto.setUsername(entity.getUsername());
		dto.setNome(entity.getNome());
		dto.setEmail(entity.getEmail());
		dto.setTelefone(entity.getTelefone());
		dto.setDataNascimento(entity.getDataNascimento());
		// Extrai apenas os nomes (String) de cada ConhecimentoEntity
		// Versao imperativa - veja o Anexo A para a versao equivalente com streams
		List<String> nomes = new ArrayList<>();
		for (ConhecimentoEntity c : entity.getConhecimentos()) {
			nomes.add(c.getNome());
		}
		dto.setConhecimentos(nomes);
		return dto;
	}

	// PessoaDto -> PessoaEntity (para insercao - sem ID, o banco gera)
	private PessoaEntity toEntity(PessoaDto dto) {
		PessoaEntity entity = new PessoaEntity();
		entity.setUsername(dto.getUsername());
		entity.setNome(dto.getNome());
		entity.setEmail(dto.getEmail());
		entity.setTelefone(dto.getTelefone());
		entity.setDataNascimento(dto.getDataNascimento());
		entity.setSenha(dto.getSenha());
		if (dto.getConhecimentos() != null) {
			for (String nomeConhecimento : dto.getConhecimentos()) {
				Optional<ConhecimentoEntity> optConh =
					conhecimentoRepository.findByNomeIgnoreCase(nomeConhecimento);
				if (optConh.isPresent()) {
					entity.getConhecimentos().add(optConh.get());
				}
			}
		}
		return entity;
	}

	// =========================================================
	// Implementacao da nova logica do Service integrado aos Repositories
	// =========================================================

	@Override
	@Transactional(readOnly = true)
	public List<PessoaDto> buscarPessoas(String termo) {
		// Versao imperativa - veja o Anexo A para a versao equivalente com streams
		List<PessoaDto> resultado = new ArrayList<>();
		for (PessoaEntity entity : pessoaRepository.buscarPorTermo(termo)) {
			resultado.add(toDto(entity));
		}
		return resultado;
	}

	@Override
	public List<PessoaDto> obterPessoas() {
		List<PessoaDto> resultado = new ArrayList<>();
		for (PessoaEntity entity : pessoaRepository.findAll()){
			PessoaDto dto = toDto(entity);
			resultado.add(dto);
		}
		return resultado;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PessoaDto> obterPessoa(String username) {
		// Versao imperativa - veja o Anexo A para a versao equivalente com Optional.map()
		Optional<PessoaEntity> optEntity = pessoaRepository.findByUsername(username);
		if (optEntity.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(toDto(optEntity.get()));   // converte para DTO se encontrou
	}

	@Override
	@Transactional
	public PessoaDto incluirNovaPessoa(PessoaDto dto) {
		PessoaEntity entity = toEntity(dto);            // converte DTO para entidade
		PessoaEntity salva = pessoaRepository.save(entity);   // persiste no banco (INSERT)
		return toDto(salva);                            // retorna DTO com o ID gerado pelo banco
	}

	@Override
	@Transactional
	public PessoaDto alterarPessoa(String username, PessoaAlteracaoDto pessoaAlteracao) {
		// Busca a entidade - lanca NaoEncontradoException se nao existir
		// Versao imperativa - veja o Anexo A para a versao equivalente com orElseThrow()
		Optional<PessoaEntity> optEntity = pessoaRepository.findByUsername(username);
		if (optEntity.isEmpty()) {
			throw new NaoEncontradoException("Pessoa " + username + " não encontrada");
		}
		PessoaEntity entity = optEntity.get();

		// Atualiza os campos simples
		entity.setNome(pessoaAlteracao.getNome());
		entity.setEmail(pessoaAlteracao.getEmail());
		entity.setTelefone(pessoaAlteracao.getTelefone());
		entity.setDataNascimento(pessoaAlteracao.getDataNascimento());

		// Atualiza os conhecimentos:
		// 1. Limpa a lista atual - remove as entradas da tabela de juncao tb_pessoas_conhecimentos
		entity.getConhecimentos().clear();
		// 2. Adiciona os novos conhecimentos (reutilizando entidades ja existentes)
		if (pessoaAlteracao.getConhecimentos() != null) {
			for (String nomeConhecimento : pessoaAlteracao.getConhecimentos()) {
				Optional<ConhecimentoEntity> optConh =
					conhecimentoRepository.findByNomeIgnoreCase(nomeConhecimento);
				if (optConh.isPresent()) {
					entity.getConhecimentos().add(optConh.get());
				}
			}
		}

		PessoaEntity salva = pessoaRepository.save(entity);   // persiste as alteracoes (UPDATE)
		return toDto(salva);
	}

	@Override
	@Transactional
	public void removerPessoa(String username) {
		// Versao imperativa - veja o Anexo A para a versao equivalente com orElseThrow()
		Optional<PessoaEntity> optEntity = pessoaRepository.findByUsername(username);
		if (optEntity.isEmpty()) {
			throw new NaoEncontradoException("Pessoa " + username + " não encontrada");
		}
		PessoaEntity entity = optEntity.get();
		pessoaRepository.delete(entity);   // DELETE no banco (cascade remove os conhecimentos ta' mbem)
	}

}
