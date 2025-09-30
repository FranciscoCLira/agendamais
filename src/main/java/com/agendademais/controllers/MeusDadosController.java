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
        // Sempre recarrega o usuário e pessoa do banco para garantir dados atualizados
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null && usuario.getPessoa() != null) {
            Long pessoaId = usuario.getPessoa().getId();
            Pessoa pessoaAtualizada = pessoaRepository.findById(pessoaId).orElse(null);
            if (pessoaAtualizada != null) {
                usuario.setPessoa(pessoaAtualizada);
                session.setAttribute("usuarioLogado", usuario);
                if (pessoaAtualizada.getCelularPessoa() != null && pessoaAtualizada.getCelularPessoa().length() == 13) {
                    pessoaAtualizada.setCelularPessoa(
                            com.agendademais.utils.StringUtils
                                    .formatarCelularParaExibicao(pessoaAtualizada.getCelularPessoa()));
                }
            }
        }
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
                null, null, null, false, false,
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
        // Formatar celular para exibição sem alterar o valor salvo
        if (pessoa != null && pessoa.getCelularPessoa() != null && pessoa.getCelularPessoa().length() == 13) {
            String celularFormatado = com.agendademais.utils.StringUtils
                    .formatarCelularParaExibicao(pessoa.getCelularPessoa());
            model.addAttribute("celularPessoaFormatado", celularFormatado);
        } else {
            model.addAttribute("celularPessoaFormatado", pessoa != null ? pessoa.getCelularPessoa() : "");
        }
        String tipoUsuario = determinaTipoUsuario(session);

        // Carrega dados atuais da pessoa, evitando NPE
        String nomePais = null;
        String nomeEstado = null;
        String nomeCidade = null;
        if (pessoa != null) {
            nomePais = pessoa.getNomePais() != null ? pessoa.getNomePais().trim() : null;
            nomeEstado = pessoa.getNomeEstado() != null ? pessoa.getNomeEstado().trim() : null;
            nomeCidade = pessoa.getNomeCidade() != null ? pessoa.getNomeCidade().trim() : null;
        }

        // Busca informações de sub-instituição
        try {
            // Primeiro tenta buscar pela instituição da sessão
            Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
            Optional<PessoaSubInstituicao> vinculoSubInstituicaoOpt = Optional.empty();

            if (instituicaoSelecionada != null
                    && instituicaoSelecionada instanceof com.agendademais.entities.Instituicao && pessoa != null) {
                com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) instituicaoSelecionada;
                vinculoSubInstituicaoOpt = pessoaSubInstituicaoRepository.findByPessoaAndInstituicao(pessoa,
                        instituicao);
                System.out.println("Buscando vínculo para pessoa " + pessoa.getId() + " na instituição "
                        + instituicao.getNomeInstituicao() + ": " + vinculoSubInstituicaoOpt.isPresent());
            } else {
                System.out.println(
                        "Instituição não encontrada na sessão para buscar vínculo de sub-instituição ou pessoa nula");
            }

            boolean possuiVinculoSubInstituicao = vinculoSubInstituicaoOpt.isPresent();

            if (possuiVinculoSubInstituicao) {
                model.addAttribute("vinculoSubInstituicao", vinculoSubInstituicaoOpt.get());
                System.out.println("Vínculo encontrado: "
                        + vinculoSubInstituicaoOpt.get().getSubInstituicao().getNomeSubInstituicao());
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
            @RequestParam(required = false) String dataAfiliacaoSubInstituicao,
            @RequestParam(required = false) String excluirSubInstituicao,
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

        // Converter campos em branco para null
        if (pessoa.getComentarios() != null && pessoa.getComentarios().isBlank()) {
            pessoa.setComentarios(null);
        }
        if (pessoa.getCurriculoPessoal() != null && pessoa.getCurriculoPessoal().isBlank()) {
            pessoa.setCurriculoPessoal(null);
        }

        // Limpar celular: salvar apenas números
        if (pessoa.getCelularPessoa() != null && !pessoa.getCelularPessoa().isBlank()) {
            pessoa.setCelularPessoa(com.agendademais.utils.StringUtils.somenteNumeros(pessoa.getCelularPessoa()));
        }

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
                Boolean excluirBool = "true".equalsIgnoreCase(excluirSubInstituicao);
                System.out.println("[DEBUG] excluirSubInstituicao recebido: '" + excluirSubInstituicao
                        + "' (convertido: " + excluirBool + ")");
                processarSubInstituicao(pessoaAtual, subInstituicaoNome, identificacaoSubInstituicao,
                        excluirBool, possuiaVinculo, session, dataAfiliacaoSubInstituicao);
            } catch (Exception e) {
                System.err.println("Erro específico ao processar sub-instituição: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Dados pessoais salvos, mas houve erro ao processar sub-instituição: " + e.getMessage());
                return "redirect:/meus-dados";
            }

            // RELOAD pessoaAtual from DB and update session
            Pessoa pessoaAtualizada = pessoaRepository.findById(pessoaAtual.getId()).orElse(pessoaAtual);
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado != null) {
                usuarioLogado.setPessoa(pessoaAtualizada);
                session.setAttribute("usuarioLogado", usuarioLogado);
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
            Boolean possuiaVinculo, HttpSession session,
            String dataAfiliacaoStr) {
        // Always fetch Pessoa from repository to ensure it is managed and attached to
        // the session
        Pessoa pessoaManaged = pessoaRepository.findById(pessoa.getId())
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada ao atualizar sub-instituição"));

        java.time.LocalDate dataAfiliacaoInformada = null;
        if (dataAfiliacaoStr != null && !dataAfiliacaoStr.isBlank()) {
            try {
                dataAfiliacaoInformada = java.time.LocalDate.parse(dataAfiliacaoStr);
                if (dataAfiliacaoInformada.isAfter(java.time.LocalDate.now())) {
                    throw new RuntimeException("A data de afiliação não pode ser posterior à data atual.");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Data de afiliação inválida.");
            }
        }
        System.out.println("[DEBUG] dataAfiliacaoStr (raw): '" + dataAfiliacaoStr + "'");
        try {
            System.out.println("=== ");
            System.out.println("=== PROCESSANDO SUB-INSTITUIÇÃO ===");
            System.out.println("Pessoa ID: " + pessoa.getId());
            System.out.println("[DEBUG] subInstituicaoNome (raw): '" + subInstituicaoNome + "'");
            System.out.println("[DEBUG] identificacao (raw): '" + identificacao + "'");
            System.out.println("[DEBUG] excluir (raw): '" + excluir + "' (type: "
                    + (excluir == null ? "null" : excluir.getClass().getName()) + ")");
            System.out.println("[DEBUG] possuiaVinculo (raw): '" + possuiaVinculo + "'");
            System.out.println("=== ");

            Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
            if (!(instituicaoSelecionada instanceof Instituicao)) {
                System.err.println("ERRO: Instituição não encontrada na sessão");
                throw new RuntimeException("Instituição não encontrada na sessão");
            }

            Instituicao instituicao = (Instituicao) instituicaoSelecionada;
            System.out.println("Instituição: " + instituicao.getNomeInstituicao());

            // Busca vínculo existente
            Optional<PessoaSubInstituicao> vinculoExistente = pessoaSubInstituicaoRepository
                    .findByPessoaAndInstituicao(pessoaManaged, instituicao);

            System.out.println("Vínculo existente encontrado: " + vinculoExistente.isPresent());

            // Caso 1: Exclusão de vínculo existente
            if (Boolean.TRUE.equals(excluir) && vinculoExistente.isPresent()) {
                System.out.println("Executando exclusão do vínculo...");
                PessoaSubInstituicao vinculo = vinculoExistente.get();
                System.out.println("Vínculo a ser excluído - ID: " + vinculo.getId());
                // Remove from Pessoa's list to trigger orphanRemoval
                if (pessoaManaged.getPessoaSubInstituicao() != null) {
                    pessoaManaged.getPessoaSubInstituicao().removeIf(v -> v.getId().equals(vinculo.getId()));
                }
                // Save Pessoa to persist orphan removal
                pessoaRepository.save(pessoaManaged);
                pessoaSubInstituicaoRepository.flush();
                boolean aindaExiste = pessoaSubInstituicaoRepository.findById(vinculo.getId()).isPresent();
                System.out.println("[DEBUG] Pós-delete: vínculo existe? " + aindaExiste);
                // Busca por pessoa e instituição para garantir que não há mais vínculo
                Optional<PessoaSubInstituicao> checkVinculo = pessoaSubInstituicaoRepository
                        .findByPessoaAndInstituicao(pessoa, instituicao);
                System.out.println("[DEBUG] Pós-delete: findByPessoaAndInstituicao: " + checkVinculo.isPresent());
                if (aindaExiste || checkVinculo.isPresent()) {
                    System.err.println("[ALERTA] Vínculo NÃO removido do banco!");
                } else {
                    System.out.println("Vínculo com sub-instituição removido com sucesso");
                }
                return;
            }

            // Declaração única das variáveis de controle
            boolean subInstituicaoVazia = (subInstituicaoNome == null || subInstituicaoNome.trim().isEmpty());
            boolean identificacaoVazia = (identificacao == null || identificacao.trim().isEmpty());

            // Caso 1: Exclusão de vínculo existente (checkbox ou ambos campos em branco)
            if ((Boolean.TRUE.equals(excluir) || (subInstituicaoVazia && identificacaoVazia))
                    && vinculoExistente.isPresent()) {
                System.out.println("Executando exclusão do vínculo (checkbox ou ambos campos em branco)...");
                PessoaSubInstituicao vinculo = vinculoExistente.get();
                System.out.println("Vínculo a ser excluído - ID: " + vinculo.getId());
                // Remove from Pessoa's list to trigger orphanRemoval
                if (pessoaManaged.getPessoaSubInstituicao() != null) {
                    pessoaManaged.getPessoaSubInstituicao().removeIf(v -> v.getId().equals(vinculo.getId()));
                }
                // Save Pessoa to persist orphan removal
                pessoaRepository.save(pessoaManaged);
                pessoaSubInstituicaoRepository.flush();
                boolean aindaExiste = pessoaSubInstituicaoRepository.findById(vinculo.getId()).isPresent();
                System.out.println("[DEBUG] Pós-delete: vínculo existe? " + aindaExiste);
                System.out.println("Vínculo com sub-instituição removido com sucesso");
                return;
            }

            // Caso 2: Exclusão marcada mas sem vínculo existente
            if ((Boolean.TRUE.equals(excluir) || (subInstituicaoVazia && identificacaoVazia))
                    && !vinculoExistente.isPresent()) {
                System.out.println("Exclusão marcada ou ambos campos em branco, mas não há vínculo para remover");
                return;
            }

            // Caso 3: Não há dados de sub-instituição para processar (já tratado acima)

            // Se identificacao preenchida sem subInstituicao, erro
            if (!subInstituicaoVazia && identificacaoVazia) {
                // subInstituicao preenchida, identificacao vazia: PERMITIDO
                // Não faz nada, segue o fluxo
            } else if (subInstituicaoVazia && !identificacaoVazia) {
                // identificacao preenchida, subInstituicao vazia: ERRO
                throw new RuntimeException("Por favor, preencha a sub-instituição antes de informar a identificação.");
            }

            // Caso 4: Busca ou cria a sub-instituição
            String nomeSubInst = subInstituicaoNome != null ? subInstituicaoNome.trim() : "";
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
            PessoaSubInstituicao vinculoToSave;
            if (vinculoExistente.isPresent()) {
                // Atualiza vínculo existente
                System.out.println("Atualizando vínculo existente");
                vinculoToSave = vinculoExistente.get();
                vinculoToSave.setSubInstituicao(subInstituicao);
                vinculoToSave.setIdentificacaoPessoaSubInstituicao(identificacao != null ? identificacao.trim() : null);
                vinculoToSave.setDataAfiliacao(dataAfiliacaoInformada);
                vinculoToSave.setDataUltimaAtualizacao(java.time.LocalDate.now());
                System.out.println("Vínculo com sub-instituição atualizado");
            } else {
                // Cria novo vínculo
                System.out.println("Criando novo vínculo");
                vinculoToSave = new PessoaSubInstituicao();
                vinculoToSave.setPessoa(pessoa);
                vinculoToSave.setInstituicao(instituicao);
                vinculoToSave.setSubInstituicao(subInstituicao);
                vinculoToSave.setIdentificacaoPessoaSubInstituicao(identificacao != null ? identificacao.trim() : null);
                vinculoToSave.setDataAfiliacao(dataAfiliacaoInformada);
                vinculoToSave.setDataUltimaAtualizacao(java.time.LocalDate.now());
                System.out.println("Novo vínculo com sub-instituição criado");
            }

            PessoaSubInstituicao vinculoSalvo = pessoaSubInstituicaoRepository.save(vinculoToSave);
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
                System.out.println("[DEBUG] /api/sub-instituicoes: instituicaoSelecionada ausente ou inválida");
                return ResponseEntity.ok(List.of());
            }

            Instituicao instituicao = (Instituicao) instituicaoSelecionada;
            System.out.println("[DEBUG] /api/sub-instituicoes: instituicaoSelecionada=" + instituicao.getId()
                    + ", nome=" + instituicao.getNomeInstituicao() + ", q='" + q + "'");

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

            System.out.println("[DEBUG] /api/sub-instituicoes: encontrados " + subInstituicoes.size() + " resultados");

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
