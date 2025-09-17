// ...existing code...
package com.agendademais.controllers;

import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.repositories.LogPostagemRepository;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.AutorRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agendademais.services.DisparoEmailService;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/administrador/postagens")
public class PostagemController {

    // Utilitário para checar sessão ativa
    private boolean isSessaoInvalida(jakarta.servlet.http.HttpSession session) {
        return session == null || session.getAttribute("usuarioLogado") == null;
    }

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;
    @Autowired
    private LogPostagemRepository logPostagemRepository;
    @Autowired
    private DisparoEmailService disparoEmailService;
    @Autowired
    private com.agendademais.repositories.InscricaoTipoAtividadeRepository inscricaoTipoAtividadeRepository;

    @GetMapping("/nova")
    public String previewPostagem(@RequestParam("ocorrenciaId") Long ocorrenciaId, Model model, jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
        if (ocorrencia == null) {
            model.addAttribute("msgErro", "Ocorrência não encontrada.");
            return "redirect:/administrador/ocorrencias";
        }
        model.addAttribute("ocorrencia", ocorrencia);
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
            Model model, jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        // Filtros básicos (apenas paginação e ordenação por enquanto)
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pagina, itensPorPagina, org.springframework.data.domain.Sort.Direction.DESC, "dataHoraPostagem");
        org.springframework.data.domain.Page<com.agendademais.entities.LogPostagem> page = logPostagemRepository
                .findAll(pageable);
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
    public String andamentoDisparo(@PathVariable Long ocorrenciaId, Model model, jakarta.servlet.http.HttpSession session) {
        if (isSessaoInvalida(session)) {
            return "redirect:/acesso";
        }
        // Simulação de progresso (em produção, usar serviço/estado real)
        OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
        int total = 42; // Buscar total real de destinatários
        int enviados = (int) (System.currentTimeMillis() / 3000 % (total + 1)); // Simula progresso
        int falhas = enviados > 35 ? 2 : 0;
        java.util.List<String> erros = java.util.Arrays.asList("email1@exemplo.com falhou",
                "email2@exemplo.com falhou");
        model.addAttribute("ocorrencia", ocorrencia);
        model.addAttribute("total", total);
        model.addAttribute("enviados", enviados);
        model.addAttribute("falhas", falhas);
        model.addAttribute("erros", erros);
        return "administrador/andamento-disparo";
    }

    // Simulação de destinatários
    @PostMapping("/simular-disparo")
    public String simularDisparo(@ModelAttribute OcorrenciaAtividade ocorrencia, Model model, jakarta.servlet.http.HttpSession session) {
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
        Long tipoAtividadeId = null;
        Long instituicaoId = null;
        if (completa != null && completa.getIdAtividade() != null
                && completa.getIdAtividade().getTipoAtividade() != null) {
            tipoAtividadeId = completa.getIdAtividade().getTipoAtividade().getId();
            instituicaoId = completa.getIdAtividade().getInstituicao().getId();
        }
        long totalDestinatarios = 0;
        if (tipoAtividadeId != null && instituicaoId != null) {
            totalDestinatarios = inscricaoTipoAtividadeRepository
                    .countByTipoAtividadeIdAndInscricao_IdInstituicao_Id(tipoAtividadeId, instituicaoId);
        }
        model.addAttribute("ocorrencia", completa);
        model.addAttribute("totalDestinatarios", totalDestinatarios);
        model.addAttribute("simulacao", true);
        return "administrador/postagem-preview";
    }

    // Disparo real de e-mails
    @PostMapping("/disparar")
    public String dispararEmails(@ModelAttribute OcorrenciaAtividade ocorrencia,
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
        return "redirect:/administrador/postagens/andamento/" + completa.getId();
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
    public java.util.List<String> autocompleteAutor(@RequestParam("term") String term) {
    return logPostagemRepository.findDistinctAutorNomeOuEmailByTerm(term);
    }

}
