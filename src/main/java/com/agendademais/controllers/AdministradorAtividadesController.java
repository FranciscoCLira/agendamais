package com.agendademais.controllers;

import com.agendademais.entities.Atividade;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.SubInstituicao;
import com.agendademais.repositories.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.agendademais.specs.AtividadeSpecs;

@Controller
@RequestMapping("/administrador/atividades")
public class AdministradorAtividadesController {
    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;
    @Autowired
    private AtividadeRepository atividadeRepo;
    @Autowired
    private SubInstituicaoRepository subInstituicaoRepo;

    @GetMapping
    public String listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) String forma,
            @RequestParam(required = false) String alvo,
            @RequestParam(required = false) Long subInstituicao,
            @RequestParam(required = false) Long solicitante,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) String ordenacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            Model model,
            HttpSession session) {
        if (session.getAttribute("usuarioLogado") == null) {
            // Mensagem padrão de sessão expirada
            model.addAttribute("mensagemErro", "Sessão expirada. Faça login novamente.");
            return "login";
        }
        // Conversão de datas
        LocalDate dataIni = null, dataFinal = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (dataInicio != null && !dataInicio.isEmpty())
                dataIni = LocalDate.parse(dataInicio, dtf);
        } catch (Exception ignored) {
        }
        try {
            if (dataFim != null && !dataFim.isEmpty())
                dataFinal = LocalDate.parse(dataFim, dtf);
        } catch (Exception ignored) {
        }

        // Ordenação
        Sort sort = Sort.by(Sort.Direction.DESC, "dataAtualizacao");
        if (ordenacao != null && !ordenacao.isEmpty()) {
            switch (ordenacao) {
                case "A": // Ordem: Data, Sit., Forma, Alvo, Sub, Título
                    sort = Sort.by(Sort.Direction.DESC, "dataAtualizacao")
                            .and(Sort.by("situacaoAtividade"))
                            .and(Sort.by("formaApresentacao"))
                            .and(Sort.by("publicoAlvo"))
                            .and(Sort.by("subInstituicao.nomeSubInstituicao"))
                            .and(Sort.by("tituloAtividade"));
                    break;
                case "B": // Ordem: Data, Sub, Sit., Título
                    sort = Sort.by(Sort.Direction.DESC, "dataAtualizacao")
                            .and(Sort.by("subInstituicao.nomeSubInstituicao"))
                            .and(Sort.by("situacaoAtividade"))
                            .and(Sort.by("tituloAtividade"));
                    break;
                case "C": // Ordem: Sit., Data, Forma, Alvo, Sub, Título
                    sort = Sort.by("situacaoAtividade")
                            .and(Sort.by(Sort.Direction.DESC, "dataAtualizacao"))
                            .and(Sort.by("formaApresentacao"))
                            .and(Sort.by("publicoAlvo"))
                            .and(Sort.by("subInstituicao.nomeSubInstituicao"))
                            .and(Sort.by("tituloAtividade"));
                    break;
                case "D": // Ordem: Sub, Data, Sit., Título
                    sort = Sort.by("subInstituicao.nomeSubInstituicao")
                            .and(Sort.by(Sort.Direction.DESC, "dataAtualizacao"))
                            .and(Sort.by("situacaoAtividade"))
                            .and(Sort.by("tituloAtividade"));
                    break;
                case "E": // Ordem: Sub, Título
                    sort = Sort.by("subInstituicao.nomeSubInstituicao")
                            .and(Sort.by("tituloAtividade"));
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "dataAtualizacao");
            }
        }

        // Recuperar instituição da sessão
        var instituicao = session.getAttribute("instituicaoSelecionada");
        Long instituicaoId = null;
        List<SubInstituicao> subinstituicoes;
        List<Pessoa> pessoas;
        if (instituicao != null && instituicao instanceof com.agendademais.entities.Instituicao) {
            instituicaoId = ((com.agendademais.entities.Instituicao) instituicao).getId();
            subinstituicoes = subInstituicaoRepo.findByInstituicaoAndSituacaoSubInstituicao(
                    (com.agendademais.entities.Instituicao) instituicao, "A");
            // Buscar apenas solicitantes com atividades na instituição logada
            pessoas = atividadeRepo.findDistinctSolicitantesByInstituicaoId(instituicaoId);
        } else {
            subinstituicoes = subInstituicaoRepo.findAll();
            pessoas = java.util.Collections.emptyList();
        }
        // Filtros dinâmicos
        var spec = AtividadeSpecs.filtro(
                titulo, situacao, forma, alvo,
                subInstituicao, solicitante,
                dataIni, dataFinal,
                instituicaoId);
        Page<Atividade> atividadesPage = atividadeRepo.findAll(spec, PageRequest.of(page, size, sort));
        // DEBUG: Inspeciona o conteúdo da lista de atividades
        for (Object obj : atividadesPage.getContent()) {
            if (obj == null) {
                System.out.println("[DEBUG] Atividade nula na lista!");
            } else if (!(obj instanceof Atividade)) {
                System.out.println("[DEBUG] Objeto inesperado: " + obj.getClass());
            } else {
                Atividade a = (Atividade) obj;
                System.out
                        .println("[DEBUG] Atividade: id=" + a.getId() + ", dataAtualizacao=" + a.getDataAtualizacao());
            }
        }
        // Garante que não há elementos nulos na lista de atividades
        List<Atividade> listaAtividades = atividadesPage.getContent().stream()
                .filter(java.util.Objects::nonNull)
                .toList();
        model.addAttribute("atividades", listaAtividades);
        model.addAttribute("page", atividadesPage);
        model.addAttribute("size", size);
        model.addAttribute("subinstituicoes", subinstituicoes);
        model.addAttribute("pessoas", pessoas);
        model.addAttribute("ordenacao", ordenacao);
        // Adicionar atributos de sessão para o cabeçalho
        model.addAttribute("instituicaoSelecionada", session.getAttribute("instituicaoSelecionada"));
        model.addAttribute("usuarioLogado", session.getAttribute("usuarioLogado"));
        return "administrador/atividades-lista";
    }

    @PostMapping("/deletar/{id}")
    public String deletarAtividade(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) String forma,
            @RequestParam(required = false) String alvo,
            @RequestParam(required = false) Long subInstituicao,
            @RequestParam(required = false) Long solicitante,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) String ordenacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            Model model,
            HttpSession session) {
        // Verifica se existem ocorrências vinculadas antes de tentar deletar
        long ocorrenciasVinculadas = ocorrenciaAtividadeRepository
                .count((root, query, cb) -> cb.equal(root.get("idAtividade").get("id"), id));
        if (ocorrenciasVinculadas > 0) {
            model.addAttribute("mensagemErro",
                    "Não é possível excluir esta Atividade pois existem Ocorrências vinculadas.");
        } else {
            try {
                atividadeRepo.deleteById(id);
                model.addAttribute("mensagemSucesso", "Atividade excluída com sucesso.");
            } catch (Exception ex) {
                ex.printStackTrace();
                model.addAttribute("mensagemErro", "Erro ao excluir Atividade.");
            }
        }
        // Redireciona para a lista mantendo os filtros
        return listar(titulo, situacao, forma, alvo, subInstituicao, solicitante, dataInicio, dataFim, ordenacao, page,
                size, model, session);
    }

    // Protege contra exclusão via GET: apenas redireciona com mensagem de erro
    @GetMapping("/deletar/{id}")
    public String deletarAtividadeGet(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensagemErro", "Exclusão de atividade só pode ser feita via POST.");
        return "redirect:/administrador/atividades";
    }
}
