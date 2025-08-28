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
    @Autowired private AtividadeRepository atividadeRepo;
    @Autowired private SubInstituicaoRepository subInstituicaoRepo;
    @Autowired private PessoaRepository pessoaRepo;

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
        HttpSession session
    ) {
        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/acesso";
        }
        // Conversão de datas
        LocalDate dataIni = null, dataFinal = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try { if (dataInicio != null && !dataInicio.isEmpty()) dataIni = LocalDate.parse(dataInicio, dtf); } catch (Exception ignored) {}
        try { if (dataFim != null && !dataFim.isEmpty()) dataFinal = LocalDate.parse(dataFim, dtf); } catch (Exception ignored) {}

        // Ordenação
        Sort sort = Sort.by(Sort.Direction.DESC, "dataAtualizacao");
        if (ordenacao != null && !ordenacao.isEmpty()) {
            switch (ordenacao) {
                case "titulo": sort = Sort.by("tituloAtividade"); break;
                case "situacao": sort = Sort.by("situacaoAtividade"); break;
                case "forma": sort = Sort.by("formaApresentacao"); break;
                case "alvo": sort = Sort.by("publicoAlvo"); break;
                case "subinstituicao": sort = Sort.by("subInstituicao.nome"); break;
                case "solicitante": sort = Sort.by("idSolicitante.nome"); break;
                case "dataAtualizacao": sort = Sort.by(Sort.Direction.DESC, "dataAtualizacao"); break;
            }
        }

        // Filtros dinâmicos
        var spec = AtividadeSpecs.filtro(
                titulo, situacao, forma, alvo,
                subInstituicao, solicitante,
                dataIni, dataFinal
        );
    Page<Atividade> atividadesPage = atividadeRepo.findAll(spec, PageRequest.of(page, size, sort));
        // Recuperar instituição da sessão
        var instituicao = session.getAttribute("instituicaoSelecionada");
        List<SubInstituicao> subinstituicoes;
        if (instituicao != null && instituicao instanceof com.agendademais.entities.Instituicao) {
            subinstituicoes = subInstituicaoRepo.findByInstituicaoAndSituacaoSubInstituicao((com.agendademais.entities.Instituicao)instituicao, "A");
        } else {
            subinstituicoes = subInstituicaoRepo.findAll();
        }
        List<Pessoa> pessoas = pessoaRepo.findAll();
    model.addAttribute("atividades", atividadesPage.getContent());
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
}
