package com.agendademais.controllers;

import com.agendademais.entities.Local;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.PessoaSubInstituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.SubInstituicao;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.PessoaSubInstituicaoRepository;
import com.agendademais.repositories.SubInstituicaoRepository;
import com.agendademais.services.LocalService;
import com.agendademais.utils.LocalFormUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

/**
 * Controller unificado para meus-dados - funciona para todos os níveis de
 * usuário
 * (participante, autor, administrador, super-usuario, controle-total)
 */
@Controller
public class MeusDadosController {

    private final PessoaRepository pessoaRepository;
    private final LocalService localService;
    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;

    public MeusDadosController(PessoaRepository pessoaRepository, LocalService localService, 
                              PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository,
                              SubInstituicaoRepository subInstituicaoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.localService = localService;
        this.pessoaSubInstituicaoRepository = pessoaSubInstituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
    }

    // Utilitario com.agendademais.utils/LocalFormUtil.java
    private String recarregarViewComListas(Model model, Pessoa pessoa, String tipoUsuario) {
        LocalFormUtil.preencherListasLocais(model, localService, pessoa);
        model.addAttribute("pessoa", pessoa);
        model.addAttribute("tipoUsuario", tipoUsuario);
        return "participante/meus-dados";
    }

    /**
     * Endpoint unificado para todos os tipos de usuário
     */
    @GetMapping("/meus-dados")
    public String exibirMeusDados(Model model, HttpSession session) {
        return processarMeusDados(model, session);
    }

    /**
     * Endpoint específico para participante (mantido para compatibilidade)
     * Redireciona para o endpoint unificado
     */
    @GetMapping("/participante/meus-dados")
    public String exibirMeusDadosParticipante(Model model, HttpSession session) {
        return "redirect:/meus-dados";
    }

    /**
     * Endpoint para salvar dados do participante (compatibilidade)
     * Redireciona para o endpoint unificado
     */
    @PostMapping("/participante/meus-dados/salvar")
    public String salvarMeusDadosParticipante(
            @ModelAttribute Pessoa pessoa,
            @RequestParam(required = false) String paisOutro,
            @RequestParam(required = false) String estadoOutro,
            @RequestParam(required = false) String cidadeOutro,
            @RequestParam String nomePaisPessoa,
            @RequestParam String nomeEstadoPessoa,
            @RequestParam String nomeCidadePessoa,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        return atualizarMeusDados(pessoa, paisOutro, estadoOutro, cidadeOutro,
                nomePaisPessoa, nomeEstadoPessoa, nomeCidadePessoa,
                null, null, false, false,
                session, model, redirectAttributes);
    }

    /**
     * Método principal que processa meus-dados para qualquer tipo de usuário
     */
    private String processarMeusDados(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null || usuario.getPessoa() == null) {
            model.addAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        Pessoa pessoa = usuario.getPessoa();
        String tipoUsuario = determinaTipoUsuario(session);

        // Carrega dados atuais da pessoa
        String nomePais = pessoa.getNomePais() != null ? pessoa.getNomePais().trim() : null;
        String nomeEstado = pessoa.getNomeEstado() != null ? pessoa.getNomeEstado().trim() : null;
        String nomeCidade = pessoa.getNomeCidade() != null ? pessoa.getNomeCidade().trim() : null;

        // Busca informações de sub-instituição
        try {
            // Primeiro tenta buscar pela instituição da sessão
            Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
            Optional<PessoaSubInstituicao> vinculoSubInstituicaoOpt = Optional.empty();
            
            if (instituicaoSelecionada != null && instituicaoSelecionada instanceof com.agendademais.entities.Instituicao) {
                com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) instituicaoSelecionada;
                vinculoSubInstituicaoOpt = pessoaSubInstituicaoRepository.findByPessoaAndInstituicao(pessoa, instituicao);
                System.out.println("Buscando vínculo para pessoa " + pessoa.getId() + " na instituição " + instituicao.getNomeInstituicao() + ": " + vinculoSubInstituicaoOpt.isPresent());
            } else {
                System.out.println("Instituição não encontrada na sessão para buscar vínculo de sub-instituição");
            }
            
            boolean possuiVinculoSubInstituicao = vinculoSubInstituicaoOpt.isPresent();
            
            if (possuiVinculoSubInstituicao) {
                model.addAttribute("vinculoSubInstituicao", vinculoSubInstituicaoOpt.get());
                System.out.println("Vínculo encontrado: " + vinculoSubInstituicaoOpt.get().getSubInstituicao().getNomeSubInstituicao());
            } else {
                System.out.println("Nenhum vínculo de sub-instituição encontrado para a instituição atual");
            }
            model.addAttribute("possuiVinculoSubInstituicao", possuiVinculoSubInstituicao);
        } catch (Exception e) {
            // Em caso de erro, apenas não mostra a informação de sub-instituição
            System.out.println("Erro ao buscar sub-instituição: " + e.getMessage());
            model.addAttribute("possuiVinculoSubInstituicao", false);
        }

        // Preenche listas para os selects
        LocalFormUtil.preencherListasLocais(model, localService, pessoa);

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("tipoUsuario", tipoUsuario);
        model.addAttribute("nomePaisPessoa", nomePais);
        model.addAttribute("nomeEstadoPessoa", nomeEstado);
        model.addAttribute("nomeCidadePessoa", nomeCidade);

        return "participante/meus-dados";
    }

    @PostMapping("/meus-dados")
    public String atualizarMeusDados(
            @ModelAttribute Pessoa pessoa,
            @RequestParam(required = false) String paisOutro,
            @RequestParam(required = false) String estadoOutro,
            @RequestParam(required = false) String cidadeOutro,
            @RequestParam String nomePaisPessoa,
            @RequestParam String nomeEstadoPessoa,
            @RequestParam String nomeCidadePessoa,
            @RequestParam(required = false) String subInstituicaoNome,
            @RequestParam(required = false) String identificacaoSubInstituicao,
            @RequestParam(required = false) Boolean excluirSubInstituicao,
            @RequestParam(required = false) Boolean possuiaVinculo,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null || usuario.getPessoa() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        String tipoUsuario = determinaTipoUsuario(session);
        Pessoa pessoaAtual = usuario.getPessoa();

        // Processa campos "Outro"
        String paisNome = "Outro".equals(nomePaisPessoa) && paisOutro != null && !paisOutro.isBlank()
                ? paisOutro.trim()
                : nomePaisPessoa;
        String estadoNome = "Outro".equals(nomeEstadoPessoa) && estadoOutro != null && !estadoOutro.isBlank()
                ? estadoOutro.trim()
                : nomeEstadoPessoa;
        String cidadeNome = "Outro".equals(nomeCidadePessoa) && cidadeOutro != null && !cidadeOutro.isBlank()
                ? cidadeOutro.trim()
                : nomeCidadePessoa;

        // Validação de campos obrigatórios
        if (pessoa.getNomePessoa() == null || pessoa.getNomePessoa().isBlank()) {
            model.addAttribute("mensagemErro", "Nome é obrigatório.");
            return recarregarViewComListas(model, pessoa, tipoUsuario);
        }

        // Validações de país, estado, cidade
        if (paisNome == null || paisNome.isBlank() || "Outro".equals(paisNome)) {
            model.addAttribute("mensagemErro", "Informe o País.");
            return recarregarViewComListas(model, pessoa, tipoUsuario);
        }
        if (estadoNome == null || estadoNome.isBlank() || "Outro".equals(estadoNome)) {
            model.addAttribute("mensagemErro", "Informe o Estado.");
            return recarregarViewComListas(model, pessoa, tipoUsuario);
        }
        if (cidadeNome == null || cidadeNome.isBlank() || "Outro".equals(cidadeNome)) {
            model.addAttribute("mensagemErro", "Informe a Cidade.");
            return recarregarViewComListas(model, pessoa, tipoUsuario);
        }

        try {
            // Busca ou cria os locais
            Local paisLocal = localService.buscarOuCriar(1, paisNome, null);
            Local estadoLocal = localService.buscarOuCriar(2, estadoNome, paisLocal);
            Local cidadeLocal = localService.buscarOuCriar(3, cidadeNome, estadoLocal);

            // Debug: Log dos dados recebidos
            System.out.println("=== DEBUG MEUS DADOS ===");
            System.out.println("Nome: " + pessoa.getNomePessoa());
            System.out.println("Email: " + pessoa.getEmailPessoa());
            System.out.println("Celular: " + pessoa.getCelularPessoa());
            System.out.println("Currículo: " + pessoa.getCurriculoPessoal());
            System.out.println("Comentários recebidos: '" + pessoa.getComentarios() + "'");
            System.out.println("Comentários da pessoa atual: '" + pessoaAtual.getComentarios() + "'");
            System.out.println("========================");

            // Atualiza dados da pessoa
            pessoaAtual.setNomePessoa(pessoa.getNomePessoa());
            pessoaAtual.setEmailPessoa(pessoa.getEmailPessoa());
            pessoaAtual.setCelularPessoa(pessoa.getCelularPessoa());
            pessoaAtual.setCurriculoPessoal(pessoa.getCurriculoPessoal());
            pessoaAtual.setComentarios(pessoa.getComentarios());

            // Define referências de local
            pessoaAtual.setPais(paisLocal);
            pessoaAtual.setEstado(estadoLocal);
            pessoaAtual.setCidade(cidadeLocal);

            pessoaRepository.save(pessoaAtual);

            // Processa informações de Sub-Instituição
            try {
                processarSubInstituicao(pessoaAtual, subInstituicaoNome, identificacaoSubInstituicao, 
                                      excluirSubInstituicao, possuiaVinculo, session);
            } catch (Exception e) {
                System.err.println("Erro específico ao processar sub-instituição: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("mensagemErro", 
                    "Dados pessoais salvos, mas houve erro ao processar sub-instituição: " + e.getMessage());
                return "redirect:/meus-dados";
            }

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados atualizados com sucesso!");

            // Redireciona de volta para /meus-dados em modo readonly
            return "redirect:/meus-dados";

        } catch (Exception e) {
            model.addAttribute("mensagemErro", "Erro ao atualizar dados: " + e.getMessage());
            return recarregarViewComListas(model, pessoa, tipoUsuario);
        }
    }

    /**
     * Processa as informações de Sub-Instituição (inclusão, alteração ou exclusão)
     */
    @Transactional
    private void processarSubInstituicao(Pessoa pessoa, String subInstituicaoNome, 
                                       String identificacao, Boolean excluir, 
                                       Boolean possuiaVinculo, HttpSession session) {
        try {
            System.out.println("=== PROCESSANDO SUB-INSTITUIÇÃO ===");
            System.out.println("Pessoa ID: " + pessoa.getId());
            System.out.println("Sub-Instituição Nome: " + subInstituicaoNome);
            System.out.println("Identificação: " + identificacao);
            System.out.println("Excluir: " + excluir);
            System.out.println("Possuía Vínculo: " + possuiaVinculo);
            
            Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
            if (!(instituicaoSelecionada instanceof Instituicao)) {
                System.err.println("ERRO: Instituição não encontrada na sessão");
                throw new RuntimeException("Instituição não encontrada na sessão");
            }
            
            Instituicao instituicao = (Instituicao) instituicaoSelecionada;
            System.out.println("Instituição: " + instituicao.getNomeInstituicao());
            
            // Busca vínculo existente
            Optional<PessoaSubInstituicao> vinculoExistente = pessoaSubInstituicaoRepository
                .findByPessoaAndInstituicao(pessoa, instituicao);
            
            System.out.println("Vínculo existente encontrado: " + vinculoExistente.isPresent());
            
            // Caso 1: Exclusão de vínculo existente
            if (Boolean.TRUE.equals(excluir) && vinculoExistente.isPresent()) {
                System.out.println("Executando exclusão do vínculo...");
                PessoaSubInstituicao vinculo = vinculoExistente.get();
                System.out.println("Vínculo a ser excluído - ID: " + vinculo.getId());
                pessoaSubInstituicaoRepository.delete(vinculo);
                System.out.println("Vínculo com sub-instituição removido com sucesso");
                return;
            }
            
            // Caso 2: Exclusão marcada mas sem vínculo existente
            if (Boolean.TRUE.equals(excluir) && !vinculoExistente.isPresent()) {
                System.out.println("Exclusão marcada mas não há vínculo para remover");
                return;
            }
            
            // Caso 3: Não há dados de sub-instituição para processar
            if ((subInstituicaoNome == null || subInstituicaoNome.trim().isEmpty()) && 
                (identificacao == null || identificacao.trim().isEmpty())) {
                System.out.println("Não há dados de sub-instituição para processar");
                return;
            }
            
            // Caso 4: Busca ou cria a sub-instituição
            String nomeSubInst = subInstituicaoNome.trim();
            List<SubInstituicao> subInstituicoesExistentes = subInstituicaoRepository
                .findByNomeSubInstituicaoAndInstituicao(nomeSubInst, instituicao);
            
            SubInstituicao subInstituicao;
            if (subInstituicoesExistentes.isEmpty()) {
                // Cria nova sub-instituição
                System.out.println("Criando nova sub-instituição: " + nomeSubInst);
                subInstituicao = new SubInstituicao();
                subInstituicao.setNomeSubInstituicao(nomeSubInst);
                subInstituicao.setInstituicao(instituicao);
                subInstituicao.setSituacaoSubInstituicao("A"); // Ativa
                subInstituicao = subInstituicaoRepository.save(subInstituicao);
                System.out.println("Nova sub-instituição criada com ID: " + subInstituicao.getId());
            } else {
                subInstituicao = subInstituicoesExistentes.get(0);
                System.out.println("Sub-instituição existente encontrada com ID: " + subInstituicao.getId());
            }
            
            // Caso 5: Atualiza ou cria vínculo
            PessoaSubInstituicao vinculo;
            if (vinculoExistente.isPresent()) {
                // Atualiza vínculo existente
                System.out.println("Atualizando vínculo existente");
                vinculo = vinculoExistente.get();
                vinculo.setSubInstituicao(subInstituicao);
                vinculo.setIdentificacaoPessoaSubInstituicao(identificacao != null ? identificacao.trim() : null);
                System.out.println("Vínculo com sub-instituição atualizado");
            } else {
                // Cria novo vínculo
                System.out.println("Criando novo vínculo");
                vinculo = new PessoaSubInstituicao();
                vinculo.setPessoa(pessoa);
                vinculo.setInstituicao(instituicao);
                vinculo.setSubInstituicao(subInstituicao);
                vinculo.setIdentificacaoPessoaSubInstituicao(identificacao != null ? identificacao.trim() : null);
                vinculo.setDataAfiliacao(LocalDate.now());
                System.out.println("Novo vínculo com sub-instituição criado");
            }
            
            PessoaSubInstituicao vinculoSalvo = pessoaSubInstituicaoRepository.save(vinculo);
            System.out.println("Vínculo salvo com ID: " + vinculoSalvo.getId());
            System.out.println("=== PROCESSAMENTO CONCLUÍDO ===");
            
        } catch (Exception e) {
            System.err.println("ERRO ao processar sub-instituição: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar sub-instituição: " + e.getMessage(), e);
        }
    }

    /**
     * Determina o tipo de usuário baseado no nível de acesso da sessão
     */
    private String determinaTipoUsuario(HttpSession session) {
        Integer nivelAtual = (Integer) session.getAttribute("nivelAcessoAtual");
        int nivel = (nivelAtual != null) ? nivelAtual : 1; // Default: Participante

        switch (nivel) {
            case 2:
                return "autor";
            case 5:
                return "administrador";
            case 9:
                return "super-usuario";
            case 0:
                return "controle-total";
            default:
                return "participante";
        }
    }

    /**
     * Endpoint REST para autocomplete de sub-instituições
     */
    @GetMapping("/api/sub-instituicoes")
    @ResponseBody
    public ResponseEntity<List<String>> buscarSubInstituicoes(
            @RequestParam(required = false) String q,
            HttpSession session) {
        
        try {
            Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
            if (!(instituicaoSelecionada instanceof Instituicao)) {
                return ResponseEntity.ok(List.of());
            }
            
            Instituicao instituicao = (Instituicao) instituicaoSelecionada;
            
            // Se não há termo de busca, retorna todas as sub-instituições ativas da instituição
            List<SubInstituicao> subInstituicoes;
            if (q == null || q.trim().isEmpty()) {
                subInstituicoes = subInstituicaoRepository
                    .findByNomeSubInstituicaoContainingIgnoreCaseAndSituacaoSubInstituicao("", "A")
                    .stream()
                    .filter(si -> si.getInstituicao().getId().equals(instituicao.getId()))
                    .toList();
            } else {
                subInstituicoes = subInstituicaoRepository
                    .findByNomeSubInstituicaoContainingIgnoreCaseAndSituacaoSubInstituicao(q.trim(), "A")
                    .stream()
                    .filter(si -> si.getInstituicao().getId().equals(instituicao.getId()))
                    .toList();
            }
            
            // Converte para lista de nomes
            List<String> nomes = subInstituicoes.stream()
                .map(SubInstituicao::getNomeSubInstituicao)
                .distinct()
                .sorted()
                .toList();
            
            return ResponseEntity.ok(nomes);
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar sub-instituições: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
}
