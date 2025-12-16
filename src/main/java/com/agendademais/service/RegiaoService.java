package com.agendademais.service;

import com.agendademais.entities.Regiao;
import com.agendademais.entities.Local;
import com.agendademais.entities.Instituicao;
import com.agendademais.repository.RegiaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service para Regiao
 * Fornece operações CRUD e métodos de negócio relacionados a regiões
 */
@Service
@Transactional
public class RegiaoService {

    @Autowired
    private RegiaoRepository regiaoRepository;

    /**
     * Cria uma nova região
     * Valida o formato do código
     * Verifica unicidade por Instituição + codRegiao
     */
    public Regiao criarRegiao(Regiao regiao) {
        if (!regiao.isCodigoValido()) {
            throw new IllegalArgumentException(
                    "Código da região inválido. Formato esperado: PPEENN (4 letras + 2 números)");
        }

        // Unicidade por instituição + código da região
        if (regiao.getInstituicao() == null) {
            throw new IllegalArgumentException("Instituição obrigatória para criar Região");
        }
        boolean jaExiste = regiaoRepository.existsByCodRegiaoAndInstituicao(
                regiao.getCodRegiao(), regiao.getInstituicao());
        if (jaExiste) {
            throw new IllegalArgumentException(
                    "Já existe uma região com o código " + regiao.getCodRegiao() +
                            " para: " + regiao.getInstituicao().getNomeInstituicao());
        }

        regiao.setDataUltimaAtualizacao(LocalDate.now());
        return regiaoRepository.save(regiao);
    }

    /**
     * Atualiza uma região existente
     * Valida o formato do código (mudanças) e unicidade por Instituição + codRegiao
     */
    public Regiao atualizarRegiao(Long id, Regiao regiaoAtualizada) {
        Regiao regiao = regiaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Região não encontrada com ID: " + id));

        // Valida código apenas se foi alterado
        if (!regiao.getCodRegiao().equals(regiaoAtualizada.getCodRegiao())) {
            if (!regiaoAtualizada.isCodigoValido()) {
                throw new IllegalArgumentException(
                        "Código da região inválido. Formato esperado: PPEENN (4 letras + 2 números)");
            }

            // Unicidade por instituição + código da região
            if (regiaoAtualizada.getInstituicao() == null) {
                throw new IllegalArgumentException("Instituição obrigatória para atualizar Região");
            }
            Optional<Regiao> existente = regiaoRepository.findByCodRegiaoAndInstituicao(
                    regiaoAtualizada.getCodRegiao(), regiaoAtualizada.getInstituicao());
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Já existe uma região com o código " + regiaoAtualizada.getCodRegiao() +
                                " para: " + regiaoAtualizada.getInstituicao().getNomeInstituicao());
            }

            regiao.setCodRegiao(regiaoAtualizada.getCodRegiao());
        }

        regiao.setNomeRegiao(regiaoAtualizada.getNomeRegiao());
        regiao.setPais(regiaoAtualizada.getPais());
        regiao.setEstado(regiaoAtualizada.getEstado());
        regiao.setCidades(regiaoAtualizada.getCidades());
        regiao.setDataUltimaAtualizacao(LocalDate.now());

        return regiaoRepository.save(regiao);
    }

    /**
     * Obtém uma região pelo ID
     */
    public Regiao obterPorId(Long id) {
        return regiaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Região não encontrada com ID: " + id));
    }

    /**
     * Obtém uma região pelo código
     */
    public Optional<Regiao> obterPorCodigo(String codRegiao) {
        return regiaoRepository.findByCodRegiao(codRegiao);
    }

    /**
     * Lista todas as regiões
     */
    public List<Regiao> listarTodas() {
        return regiaoRepository.findAllOrdenado();
    }

    public List<Regiao> listarPorInstituicao(Instituicao instituicao) {
        return regiaoRepository.findByInstituicaoOrderByPaisNomeLocalAscEstadoNomeLocalAscNomeRegiaoAsc(instituicao);
    }

    /**
     * Lista regiões de um país específico
     */
    public List<Regiao> listarPorPais(Local pais) {
        return regiaoRepository.findByPais(pais);
    }

    /**
     * Lista regiões de um estado específico
     */
    public List<Regiao> listarPorEstado(Local estado) {
        return regiaoRepository.findByEstado(estado);
    }

    /**
     * Lista regiões de um país e estado específicos
     */
    public List<Regiao> listarPorPaisEEstado(Local pais, Local estado) {
        return regiaoRepository.findByPaisAndEstado(pais, estado);
    }

    /**
     * Busca regiões por padrão no nome
     */
    public List<Regiao> buscarPorNome(String nomeRegiao) {
        return regiaoRepository.findByNomeRegiaoContainingIgnoreCase(nomeRegiao);
    }

    /**
     * Adiciona uma cidade à região
     */
    public Regiao adicionarCidade(Long idRegiao, Local cidade) {
        Regiao regiao = obterPorId(idRegiao);
        regiao.adicionarCidade(cidade);
        regiao.setDataUltimaAtualizacao(LocalDate.now());
        return regiaoRepository.save(regiao);
    }

    /**
     * Remove uma cidade da região
     */
    public Regiao removerCidade(Long idRegiao, Long idCidade) {
        Regiao regiao = obterPorId(idRegiao);
        Local cidade = regiao.getCidades().stream()
                .filter(c -> c.getId().equals(idCidade))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada na região"));

        regiao.removerCidade(cidade);
        regiao.setDataUltimaAtualizacao(LocalDate.now());
        return regiaoRepository.save(regiao);
    }

    /**
     * Deleta uma região
     */
    public void deletarRegiao(Long id) {
        if (!regiaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Região não encontrada com ID: " + id);
        }
        regiaoRepository.deleteById(id);
    }

    /**
     * Verifica se uma pessoa pertence a uma região
     * Uma pessoa pertence a uma região se sua cidade está incluída na região
     */
    public boolean pessoaPertenceRegiao(Local cidadePessoa, Regiao regiao) {
        if (cidadePessoa == null || regiao == null) {
            return false;
        }
        // Evita acessar coleção lazy fora de sessão/tx: usa consulta no repository
        try {
            return regiaoRepository.existsCidadeInRegiao(regiao.getId(), cidadePessoa.getId());
        } catch (Exception e) {
            return false;
        }
    }
}
