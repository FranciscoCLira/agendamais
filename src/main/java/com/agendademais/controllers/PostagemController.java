package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.repositories.LogPostagemRepository;
import com.agendademais.repositories.AutorRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.services.DisparoEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/administrador/postagens")
public class PostagemController {
    @GetMapping("/exportar-inscritos-csv/{ocorrenciaId}")
    @ResponseBody
    public void exportarInscritosCsv(@PathVariable Long ocorrenciaId, jakarta.servlet.http.HttpServletResponse response)
            throws java.io.IOException {
        OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
        if (ocorrencia == null || ocorrencia.getIdAtividade() == null
                || ocorrencia.getIdAtividade().getTipoAtividade() == null) {
            response.setStatus(404);
            response.getWriter().write("Ocorrência não encontrada ou inválida");
            return;
        }
        Long tipoAtividadeId = ocorrencia.getIdAtividade().getTipoAtividade().getId();
        Long instituicaoId = ocorrencia.getIdAtividade().getInstituicao().getId();
        java.util.List<com.agendademais.entities.InscricaoTipoAtividade> inscricoes = inscricaoTipoAtividadeRepository
                .findAll();
        java.util.List<com.agendademais.entities.InscricaoTipoAtividade> filtradas = inscricoes.stream()
                .filter(ita -> ita.getTipoAtividade() != null && ita.getTipoAtividade().getId().equals(tipoAtividadeId))
                .filter(ita -> ita.getInscricao() != null && ita.getInscricao().getIdInstituicao() != null
                        && ita.getInscricao().getIdInstituicao().getId().equals(instituicaoId))
                .filter(ita -> ita.getInscricao() != null && ita.getInscricao().getPessoa() != null)
                .sorted((a, b) -> {
                    String sitA = a.getInscricao().getPessoa().getSituacaoPessoa() != null
                            ? a.getInscricao().getPessoa().getSituacaoPessoa()
                            : "";
                    String sitB = b.getInscricao().getPessoa().getSituacaoPessoa() != null
                            ? b.getInscricao().getPessoa().getSituacaoPessoa()
                            : "";
                    int cmp = sitA.compareToIgnoreCase(sitB);
                    if (cmp != 0)
                        return cmp;
                    String nomeA = a.getInscricao().getPessoa().getNomePessoa() != null
                            ? a.getInscricao().getPessoa().getNomePessoa()
                            : "";
                    String nomeB = b.getInscricao().getPessoa().getNomePessoa() != null
                            ? b.getInscricao().getPessoa().getNomePessoa()
                            : "";
                    return nomeA.compareToIgnoreCase(nomeB);
                })
                .toList();

        response.setContentType("text/csv; charset=UTF-8");
        String filename = "inscritos_ocorrencia_" + ocorrenciaId + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        java.io.PrintWriter writer = response.getWriter();
        writer.println(
                "idInstituicao;nomeInstituicao;idTipoAtividade;tituloTipoAtividade;id_Pessoa;SituacaoPessoa;Usuario;Nome;Email;Telefone;DataUltimaAtualizacao;Cidade;Estado;Pais");
        for (var ita : filtradas) {
            var pessoa = ita.getInscricao().getPessoa();
            var instituicaoObj = ita.getInscricao().getIdInstituicao();
            var tipoAtividadeObj = ita.getTipoAtividade();
            String idPessoa = pessoa != null && pessoa.getId() != null ? pessoa.getId().toString() : "";
            String nome = pessoa != null ? removerAcentos(pessoa.getNomePessoa()) : "";
            String email = pessoa != null ? pessoa.getEmailPessoa() : "";
            String usuario = "";
            if (email != null && !email.isBlank()) {
                var usuarios = usuarioRepository.findAllByPessoaEmailPessoa(email);
                if (usuarios != null && !usuarios.isEmpty()) {
                    usuario = usuarios.get(0).getUsername();
                }
            }
            String telefone = pessoa != null ? pessoa.getCelularPessoa() : "";
            // Força formato numérico para Excel: prefixa com tabulação se só dígitos, senão
            // mantém
            if (telefone != null && telefone.matches("\\d+")) {
                telefone = "\t" + telefone;
            }
            String situacaoPessoa = pessoa != null ? pessoa.getSituacaoPessoa() : "";
            String dataUltimaAtualizacao = pessoa != null && pessoa.getDataUltimaAtualizacao() != null
                    ? java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy").format(pessoa.getDataUltimaAtualizacao())
                    : "";
            String cidade = pessoa != null && pessoa.getCidade() != null
                    ? removerAcentos(pessoa.getCidade().getNomeLocal())
                    : "";
            String estado = pessoa != null && pessoa.getEstado() != null
                    ? removerAcentos(pessoa.getEstado().getNomeLocal())
                    : "";
            String pais = pessoa != null && pessoa.getPais() != null ? removerAcentos(pessoa.getPais().getNomeLocal())
                    : "";
            String idInstituicao = instituicaoObj != null ? String.valueOf(instituicaoObj.getId()) : "";
            String nomeInstituicao = instituicaoObj != null ? instituicaoObj.getNomeInstituicao() : "";
            String idTipoAtividade = tipoAtividadeObj != null ? String.valueOf(tipoAtividadeObj.getId()) : "";
            String tituloTipoAtividade = tipoAtividadeObj != null ? tipoAtividadeObj.getTituloTipoAtividade() : "";
            writer.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n",
                    idInstituicao, nomeInstituicao, idTipoAtividade, tituloTipoAtividade, idPessoa, situacaoPessoa,
                    usuario, nome, email,
                    telefone, dataUltimaAtualizacao, cidade, estado, pais);
        }
        writer.flush();
    }

    // Remove acentos de uma string (para exportação CSV)
    private static String removerAcentos(String texto) {
        if (texto == null)
            return "";
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    // Utilitário para checar sessão ativa
    private boolean isSessaoInvalida(jakarta.servlet.http.HttpSession session) {
        return session == null || session.getAttribute("usuarioLogado") == null;
    }

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;
    @Autowired
    private LogPostagemRepository logPostagemRepository;
    @Autowired
    private DisparoEmailService disparoEmailService;
    @Autowired
    private com.agendademais.repositories.InscricaoTipoAtividadeRepository inscricaoTipoAtividadeRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/nova")
    public String previewPostagem(
            @RequestParam("ocorrenciaId") Long ocorrenciaId,
            @RequestParam(value = "origem", required = false) String origem,
            Model model,
            jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
        if (ocorrencia == null) {
            model.addAttribute("msgErro", "Ocorrência não encontrada.");
            return "redirect:/administrador/ocorrencias";
        }
        // Corrige origem se vier nula, vazia ou "null"
        if (origem == null || origem.isBlank() || origem.equals("null")) {
            // Monta origem padrão para voltar à tela de ocorrências com filtros mínimos
            StringBuilder origemPadrao = new StringBuilder("/administrador/ocorrencias?atividadeId=");
            if (ocorrencia.getIdAtividade() != null) {
                origemPadrao.append(ocorrencia.getIdAtividade().getId());
            } else {
                origemPadrao.append("");
            }
            origem = origemPadrao.toString();
        }

        Instituicao instituicaoLogada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        String emailInstituicaoLogada = instituicaoLogada != null ? instituicaoLogada.getEmailInstituicao() : "";

        System.out.println("DEBUG: Instituição logada: " + instituicaoLogada);
        System.out.println("DEBUG: Email da instituição: " + emailInstituicaoLogada);

        model.addAttribute("instituicaoLogada", instituicaoLogada);
        model.addAttribute("emailInstituicaoLogada", emailInstituicaoLogada);

        model.addAttribute("ocorrencia", ocorrencia);
        model.addAttribute("origem", origem);
        return "administrador/postagem-preview";
    }

    @GetMapping("/lista")
    public String listaPostagens(
            @RequestParam(value = "dataInicio", required = false) String dataInicio,
            @RequestParam(value = "dataFim", required = false) String dataFim,
            @RequestParam(value = "tituloAtividade", required = false) String tituloAtividade,
            @RequestParam(value = "assuntoDivulgacao", required = false) String assuntoDivulgacao,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "statusFalha", required = false) String statusFalha,
            @RequestParam(value = "itensPorPagina", required = false, defaultValue = "10") int itensPorPagina,
            @RequestParam(value = "pagina", required = false, defaultValue = "0") int pagina,
            @RequestParam(value = "origem", required = false) String origem,
            Model model, jakarta.servlet.http.HttpSession session) {
        // Corrige origem para o fluxo de nova postagem, propagando ocorrenciaId e
        // filtros
        if (origem == null || origem.isBlank() || origem.equals("null")) {
            origem = "/administrador";
        } else if (origem.equals("/administrador/postagens/nova")) {
            // Se veio de nova postagem mas sem ocorrenciaId, tenta reconstruir
            StringBuilder novaOrigem = new StringBuilder("/administrador/postagens/nova");
            boolean temParam = false;
            // Busca ocorrenciaId do último log exibido, se possível
            Long ocorrenciaId = null;
            java.util.List<com.agendademais.entities.LogPostagem> postagensTemp = null;
            try {
                org.springframework.data.domain.Pageable tempPageable = org.springframework.data.domain.PageRequest
                        .of(0, 1, org.springframework.data.domain.Sort.Direction.DESC, "dataHoraPostagem");
                org.springframework.data.jpa.domain.Specification<com.agendademais.entities.LogPostagem> specTemp = (
                        root, query, cb) -> cb.conjunction();
                org.springframework.data.domain.Page<com.agendademais.entities.LogPostagem> pageTemp = logPostagemRepository
                        .findAll(specTemp, tempPageable);
                postagensTemp = pageTemp.getContent();
                if (!postagensTemp.isEmpty()) {
                    ocorrenciaId = postagensTemp.get(0).getOcorrenciaAtividadeId();
                }
            } catch (Exception e) {
            }
            if (ocorrenciaId != null) {
                novaOrigem.append(temParam ? "&" : "?").append("ocorrenciaId=").append(ocorrenciaId);
                temParam = true;
            }
            if (dataInicio != null && !dataInicio.isBlank()) {
                novaOrigem.append(temParam ? "&" : "?").append("dataInicio=").append(dataInicio);
                temParam = true;
            }
            if (dataFim != null && !dataFim.isBlank()) {
                novaOrigem.append(temParam ? "&" : "?").append("dataFim=").append(dataFim);
                temParam = true;
            }
            if (tituloAtividade != null && !tituloAtividade.isBlank()) {
                novaOrigem.append(temParam ? "&" : "?").append("tituloAtividade=").append(tituloAtividade);
                temParam = true;
            }
            if (assuntoDivulgacao != null && !assuntoDivulgacao.isBlank()) {
                novaOrigem.append(temParam ? "&" : "?").append("assuntoDivulgacao=").append(assuntoDivulgacao);
                temParam = true;
            }
            if (autor != null && !autor.isBlank()) {
                novaOrigem.append(temParam ? "&" : "?").append("autor=").append(autor);
                temParam = true;
            }
            if (statusFalha != null && !statusFalha.isBlank()) {
                novaOrigem.append(temParam ? "&" : "?").append("statusFalha=").append(statusFalha);
                temParam = true;
            }
            if (itensPorPagina != 10) {
                novaOrigem.append(temParam ? "&" : "?").append("itensPorPagina=").append(itensPorPagina);
                temParam = true;
            }
            origem = novaOrigem.toString();
        }
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        // Validação de datas
        if (dataInicio != null && !dataInicio.isEmpty() && dataFim != null && !dataFim.isEmpty()) {
            java.time.LocalDate ini = java.time.LocalDate.parse(dataInicio);
            java.time.LocalDate fim = java.time.LocalDate.parse(dataFim);
            if (fim.isBefore(ini)) {
                model.addAttribute("erroFiltroData", "A data final não pode ser anterior à data inicial.");
                // Mantém filtros preenchidos
                model.addAttribute("filtroDataInicio", dataInicio);
                model.addAttribute("filtroDataFim", dataFim);
                model.addAttribute("filtroTituloAtividade", tituloAtividade);
                model.addAttribute("filtroAssuntoDivulgacao", assuntoDivulgacao);
                model.addAttribute("filtroAutor", autor);
                model.addAttribute("filtroStatusFalha", statusFalha);
                model.addAttribute("filtroItensPorPagina", itensPorPagina);
                model.addAttribute("postagens", java.util.Collections.emptyList());
                model.addAttribute("nomesAutores", java.util.Collections.emptyMap());
                model.addAttribute("paginaAtual", 0);
                model.addAttribute("totalPaginas", 1);
                model.addAttribute("totalItens", 0);
                return "administrador/lista-postagem";
            }
        }
        // Filtro por instituição logada
        var instituicao = session.getAttribute("instituicaoSelecionada");
        Long instituicaoId = null;
        if (instituicao != null && instituicao instanceof com.agendademais.entities.Instituicao) {
            instituicaoId = ((com.agendademais.entities.Instituicao) instituicao).getId();
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pagina, itensPorPagina, org.springframework.data.domain.Sort.Direction.DESC, "dataHoraPostagem");

        // Monta Specification dinâmica
        org.springframework.data.jpa.domain.Specification<com.agendademais.entities.LogPostagem> spec = com.agendademais.specs.LogPostagemSpecs
                .porInstituicao(instituicaoId, ocorrenciaAtividadeRepository);
        if (dataInicio != null && !dataInicio.isEmpty()) {
            java.time.LocalDate dataIni = java.time.LocalDate.parse(dataInicio);
            spec = spec.and(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataHoraPostagem"), dataIni.atStartOfDay()));
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            java.time.LocalDate dataF = java.time.LocalDate.parse(dataFim);
            spec = spec.and(
                    (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataHoraPostagem"), dataF.atTime(23, 59, 59)));
        }
        // Se vier filtro de título de atividade, já filtra ao carregar a view
        if (tituloAtividade != null && !tituloAtividade.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("tituloAtividade")),
                    "%" + tituloAtividade.toLowerCase() + "%"));
        }
        if (assuntoDivulgacao != null && !assuntoDivulgacao.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("assuntoDivulgacao")),
                    "%" + assuntoDivulgacao.toLowerCase() + "%"));
        }
        if (autor != null && !autor.isEmpty()) {
            // Busca autorId pelo nome/email parcial, apenas dos autores presentes nos logs
            java.util.List<Long> autorIds = logPostagemRepository.findDistinctAutorIds();
            java.util.List<com.agendademais.entities.Autor> autores = autorRepository.findAllById(autorIds);
            java.util.Set<Long> autorIdsFiltro = new java.util.HashSet<>();
            String autorLower = autor.toLowerCase();
            for (var a : autores) {
                if (a.getPessoa() != null) {
                    String nome = a.getPessoa().getNomePessoa();
                    String email = a.getPessoa().getEmailPessoa();
                    String nomeEmail = nome + " <" + email + ">";
                    if (nome.toLowerCase().contains(autorLower) || email.toLowerCase().contains(autorLower)
                            || nomeEmail.toLowerCase().contains(autorLower)) {
                        autorIdsFiltro.add(a.getId());
                    }
                }
            }
            if (!autorIdsFiltro.isEmpty()) {
                spec = spec.and((root, query, cb) -> root.get("autorId").in(autorIdsFiltro));
            }
        }
        if (statusFalha != null && !statusFalha.isEmpty()) {
            if (statusFalha.equals("ok")) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("qtFalhas"), 0));
            } else if (statusFalha.equals("falha")) {
                spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("qtFalhas"), 0));
            }
        }

        org.springframework.data.domain.Page<com.agendademais.entities.LogPostagem> page = logPostagemRepository
                .findAll(spec, pageable);
        java.util.List<com.agendademais.entities.LogPostagem> postagens = page.getContent();
        // Map autorId -> nomePessoa (busca correta via Autor)
        java.util.Map<Long, String> nomesAutores = new java.util.HashMap<>();
        java.util.Set<Long> autorIds = new java.util.HashSet<>();
        for (var log : postagens) {
            if (log.getAutorId() != null)
                autorIds.add(log.getAutorId());
        }
        if (!autorIds.isEmpty()) {
            autorRepository.findAllById(autorIds).forEach(a -> {
                if (a.getPessoa() != null) {
                    nomesAutores.put(a.getId(), a.getPessoa().getNomePessoa());
                }
            });
        }
        model.addAttribute("postagens", postagens);
        model.addAttribute("nomesAutores", nomesAutores);
        model.addAttribute("paginaAtual", page.getNumber());
        model.addAttribute("totalPaginas", page.getTotalPages());
        model.addAttribute("totalItens", page.getTotalElements());
        // Mantém filtros selecionados
        model.addAttribute("filtroDataInicio", dataInicio);
        model.addAttribute("filtroDataFim", dataFim);
        model.addAttribute("filtroTituloAtividade", tituloAtividade);
        model.addAttribute("filtroAssuntoDivulgacao", assuntoDivulgacao);
        model.addAttribute("filtroAutor", autor);
        model.addAttribute("filtroStatusFalha", statusFalha);
        model.addAttribute("filtroItensPorPagina", itensPorPagina);
        model.addAttribute("origem", origem);
        return "administrador/lista-postagem";
    }

    // Endpoint para visualizar mensagemLogPostagem (AJAX)
    @GetMapping("/mensagem-log/{id}")
    @ResponseBody
    public String visualizarMensagemLog(@PathVariable Long id) {
        return logPostagemRepository.findById(id)
                .map(log -> log.getMensagemLogPostagem() != null ? log.getMensagemLogPostagem() : "-")
                .orElse("Log não encontrado");
    }

    // Endpoint para excluir log
    @PostMapping("/excluir/{id}")
    @ResponseBody
    public String excluirLog(@PathVariable Long id) {
        try {
            logPostagemRepository.deleteById(id);
            return "OK";
        } catch (Exception ex) {
            return "Erro ao excluir: " + ex.getMessage();
        }
    }

    // View de acompanhamento do disparo
    @GetMapping("/andamento/{ocorrenciaId}")
    public String andamentoDisparo(@PathVariable Long ocorrenciaId, Model model,
            @RequestParam(value = "origem", required = false) String origem,
            jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
        // Corrige origem se vier nula, vazia ou "null"
        if (origem == null || origem.isBlank() || origem.equals("null")) {
            StringBuilder origemPadrao = new StringBuilder("/administrador/ocorrencias?atividadeId=");
            if (ocorrencia != null && ocorrencia.getIdAtividade() != null) {
                origemPadrao.append(ocorrencia.getIdAtividade().getId());
            } else {
                origemPadrao.append("");
            }
            origem = origemPadrao.toString();
        }
        DisparoEmailService.ProgressoDisparo progresso = disparoEmailService.getProgresso(ocorrenciaId);
        int total = progresso != null ? progresso.total : 0;
        int enviados = progresso != null ? progresso.enviados : 0;
        int falhas = progresso != null ? progresso.falhas : 0;
        java.util.List<String> erros = progresso != null ? progresso.erros : java.util.Collections.emptyList();
        model.addAttribute("ocorrencia", ocorrencia);
        model.addAttribute("total", total);
        model.addAttribute("enviados", enviados);
        model.addAttribute("falhas", falhas);
        model.addAttribute("erros", erros);
        model.addAttribute("origem", origem);
        return "administrador/andamento-disparo";
    }

    // Simulação de destinatários
    @PostMapping("/simular-disparo")
    public String simularDisparo(@ModelAttribute OcorrenciaAtividade ocorrencia, Model model,
            jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        // Buscar a entidade completa pelo id
        OcorrenciaAtividade completa = null;
        if (ocorrencia != null && ocorrencia.getId() != null) {
            completa = ocorrenciaAtividadeRepository.findById(ocorrencia.getId()).orElse(null);
        }
        if (completa == null)
            completa = ocorrencia; // fallback para não quebrar

        // Busca o tipo de atividade e a instituição
        final Long tipoAtividadeId;
        final Long instituicaoId;
        if (completa != null && completa.getIdAtividade() != null
                && completa.getIdAtividade().getTipoAtividade() != null) {
            tipoAtividadeId = completa.getIdAtividade().getTipoAtividade().getId();
            instituicaoId = completa.getIdAtividade().getInstituicao().getId();
        } else {
            tipoAtividadeId = null;
            instituicaoId = null;
        }
        long totalDestinatarios = 0;
        if (tipoAtividadeId != null && instituicaoId != null) {
            java.util.List<com.agendademais.entities.InscricaoTipoAtividade> inscricoes = inscricaoTipoAtividadeRepository
                    .findAll();
            totalDestinatarios = inscricoes.stream()
                    .filter(ita -> ita.getTipoAtividade() != null
                            && ita.getTipoAtividade().getId().equals(tipoAtividadeId))
                    .filter(ita -> ita.getInscricao() != null && ita.getInscricao().getIdInstituicao() != null
                            && ita.getInscricao().getIdInstituicao().getId().equals(instituicaoId))
                    .filter(ita -> ita.getInscricao() != null && ita.getInscricao().getPessoa() != null
                            && "A".equalsIgnoreCase(ita.getInscricao().getPessoa().getSituacaoPessoa()))
                    .count();
        }
        // Adiciona email da instituição logada ao model
        Instituicao instituicaoLogada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        String emailInstituicaoLogada = instituicaoLogada != null ? instituicaoLogada.getEmailInstituicao() : "";
        model.addAttribute("instituicaoLogada", instituicaoLogada);
        model.addAttribute("emailInstituicaoLogada", emailInstituicaoLogada);
        model.addAttribute("ocorrencia", completa);
        model.addAttribute("totalDestinatarios", totalDestinatarios);
        model.addAttribute("simulacao", true);
        return "administrador/postagem-preview";
    }

    // Disparo real de e-mails
    @PostMapping("/disparar")
    public String dispararEmails(@ModelAttribute OcorrenciaAtividade ocorrencia,
            @RequestParam(value = "origem", required = false) String origem,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        // Buscar a entidade completa pelo id
        OcorrenciaAtividade completa = null;
        if (ocorrencia != null && ocorrencia.getId() != null) {
            completa = ocorrenciaAtividadeRepository.findById(ocorrencia.getId()).orElse(null);
        }
        if (completa == null) {
            redirectAttributes.addFlashAttribute("msgErro", "Ocorrência não encontrada. Disparo não realizado.");
            return "redirect:/administrador/postagens/lista";
        }

        // Busca o tipo de atividade e a instituição
        Long tipoAtividadeId = null;
        Long instituicaoId = null;
        if (completa.getIdAtividade() != null && completa.getIdAtividade().getTipoAtividade() != null) {
            tipoAtividadeId = completa.getIdAtividade().getTipoAtividade().getId();
            instituicaoId = completa.getIdAtividade().getInstituicao().getId();
        }
        long totalDestinatarios = 0;
        if (tipoAtividadeId != null && instituicaoId != null) {
            totalDestinatarios = inscricaoTipoAtividadeRepository
                    .countByTipoAtividadeIdAndInscricao_IdInstituicao_Id(tipoAtividadeId, instituicaoId);
        }
        // Se não houver destinatários, não interrompe, apenas registra aviso
        if (totalDestinatarios == 0) {
            redirectAttributes.addFlashAttribute("msgAviso",
                    "Nenhum destinatário encontrado para o disparo. Nenhum e-mail será enviado.");
        }
        disparoEmailService.iniciarDisparo(completa.getId(), (int) totalDestinatarios);
        String redirectUrl = "/administrador/postagens/andamento/" + completa.getId();
        if (origem != null && !origem.isBlank()) {
            redirectUrl += "?origem=" + java.net.URLEncoder.encode(origem, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:" + redirectUrl;
    }

    // Endpoint REST para progresso
    @GetMapping("/progresso/{ocorrenciaId}")
    @ResponseBody
    public DisparoEmailService.ProgressoDisparo progressoDisparo(@PathVariable Long ocorrenciaId) {
        return disparoEmailService.getProgresso(ocorrenciaId);
    }

    // --- AUTOCOMPLETE para filtros ---
    @GetMapping("/autocomplete-titulo")
    @ResponseBody
    public java.util.List<String> autocompleteTitulo(@RequestParam("term") String term) {
        return logPostagemRepository.findDistinctTituloAtividadeByTerm(term);
    }

    @GetMapping("/autocomplete-assunto")
    @ResponseBody
    public java.util.List<String> autocompleteAssunto(@RequestParam("term") String term) {
        return logPostagemRepository.findDistinctAssuntoDivulgacaoByTerm(term);
    }

    @GetMapping("/autocomplete-autor")
    @ResponseBody
    public java.util.List<java.util.Map<String, String>> autocompleteAutor(@RequestParam("term") String term) {
        java.util.List<Long> autorIds = logPostagemRepository.findDistinctAutorIds();
        java.util.List<com.agendademais.entities.Autor> autores = autorRepository.findAllById(autorIds);
        java.util.List<java.util.Map<String, String>> sugestoes = new java.util.ArrayList<>();
        String termo = term == null ? "" : term.toLowerCase();
        for (var a : autores) {
            if (a.getPessoa() != null) {
                String nome = a.getPessoa().getNomePessoa();
                String email = a.getPessoa().getEmailPessoa();
                String nomeEmail = nome + " <" + email + ">";
                if (termo.isEmpty() || nome.toLowerCase().contains(termo) || email.toLowerCase().contains(termo)
                        || nomeEmail.toLowerCase().contains(termo)) {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("label", nomeEmail);
                    map.put("value", nomeEmail);
                    sugestoes.add(map);
                }
            }
        }
        return sugestoes;
    }

}
