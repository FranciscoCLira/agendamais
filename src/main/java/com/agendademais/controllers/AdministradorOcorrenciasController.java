package com.agendademais.controllers;

import com.agendademais.entities.Autor;
import com.agendademais.entities.Atividade;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.entities.Pessoa;
import com.agendademais.repositories.AtividadeRepository;
import com.agendademais.repositories.AutorRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import com.agendademais.repositories.PessoaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/administrador/ocorrencias")
public class AdministradorOcorrenciasController {
    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;
    @Autowired
    private AtividadeRepository atividadeRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private AutorRepository autorRepository;

    @GetMapping
    public String listarOcorrencias(
            @RequestParam(value = "atividadeId", required = false) Long atividadeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            Model model,
            HttpSession session) {
        // Se veio do menu, sem atividade selecionada
        if (atividadeId == null) {
            model.addAttribute("ocorrencias", org.springframework.data.domain.Page.empty());
            model.addAttribute("atividadeSelecionada", null);
            return "administrador/ocorrencias";
        }
        // Se veio da tela de atividades
        Optional<Atividade> atividadeOpt = atividadeRepository.findById(atividadeId);
        if (atividadeOpt.isEmpty()) {
            model.addAttribute("ocorrencias", org.springframework.data.domain.Page.empty());
            model.addAttribute("atividadeSelecionada", null);
            return "administrador/ocorrencias";
        }
        Atividade atividade = atividadeOpt.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<OcorrenciaAtividade> ocorrencias = ocorrenciaAtividadeRepository.findByIdAtividade(atividade, pageable);
        model.addAttribute("atividadeSelecionada", atividade);
        model.addAttribute("ocorrencias", ocorrencias);
        // Se não houver ocorrências, redireciona para nova ocorrência
        if (ocorrencias.isEmpty()) {
            return "redirect:/administrador/ocorrencias/nova?atividadeId=" + atividade.getId();
        }
        return "administrador/ocorrencias";
    }

    @GetMapping("/nova")
    public String novaOcorrencia(@RequestParam(value = "atividadeId", required = false) Long atividadeId, Model model) {
        OcorrenciaAtividade ocorrencia = new OcorrenciaAtividade();
        if (atividadeId != null) {
            atividadeRepository.findById(atividadeId).ifPresent(ocorrencia::setIdAtividade);
        }
        model.addAttribute("ocorrencia", ocorrencia);
        return "administrador/ocorrencia-form";
    }

    @PostMapping("/nova")
    public String salvarOcorrencia(@ModelAttribute("ocorrencia") OcorrenciaAtividade ocorrencia,
            @RequestParam(value = "idAutorId", required = false) Long idAutorId,
            Model model, HttpSession session) {
        // Buscar o Autor pelo idAutorId do formulário
        if (idAutorId != null) {
            Autor autor = autorRepository.findById(idAutorId).orElse(null);
            ocorrencia.setIdAutor(autor);
        } else {
            model.addAttribute("erroAutor", "Selecione um autor válido no autocomplete.");
            return "administrador/ocorrencia-form";
        }
        // Garantir que idAtividade não seja null se veio do form
        // Se idAtividade não veio, não salva (deve ser obrigatório via binding)
        // dataAtualizacao sempre data atual
        ocorrencia.setDataAtualizacao(java.time.LocalDate.now());
        // Campos opcionais: se em branco, salvar como null
        if (isBlank(ocorrencia.getAssuntoDivulgacao()))
            ocorrencia.setAssuntoDivulgacao(null);
        if (isBlank(ocorrencia.getBibliografia()))
            ocorrencia.setBibliografia(null);
        if (isBlank(ocorrencia.getDetalheDivulgacao()))
            ocorrencia.setDetalheDivulgacao(null);
        if (isBlank(ocorrencia.getLinkImgDivulgacao()))
            ocorrencia.setLinkImgDivulgacao(null);
        if (isBlank(ocorrencia.getLinkMaterialTema()))
            ocorrencia.setLinkMaterialTema(null);
        if (isBlank(ocorrencia.getObsEncerramento()))
            ocorrencia.setObsEncerramento(null);
        ocorrenciaAtividadeRepository.save(ocorrencia);
        // Redireciona para a lista de ocorrências da atividade
        Long atividadeId = (ocorrencia.getIdAtividade() != null && ocorrencia.getIdAtividade().getId() != null)
                ? ocorrencia.getIdAtividade().getId()
                : null;
        if (atividadeId != null) {
            return "redirect:/administrador/ocorrencias?atividadeId=" + atividadeId;
        } else {
            return "redirect:/administrador/ocorrencias";
        }
    }

    // Utilitário para checar se string está em branco
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @GetMapping("/autocomplete-atividade")
    @ResponseBody
    public List<Atividade> autocompleteAtividade(@RequestParam("term") String term) {
        return atividadeRepository.findByTituloAtividadeContainingIgnoreCase(term);
    }

    @GetMapping("/autocomplete-autor")
    @ResponseBody
    public List<Autor> autocompleteAutor(@RequestParam("term") String term) {
        // Buscar autores cujos nomes ou emails da pessoa associada contenham o termo
        return autorRepository.findAll().stream()
                .filter(a -> a.getIdPessoa() != null && ((a.getIdPessoa().getNomePessoa() != null
                        && a.getIdPessoa().getNomePessoa().toLowerCase().contains(term.toLowerCase())) ||
                        (a.getIdPessoa().getEmailPessoa() != null
                                && a.getIdPessoa().getEmailPessoa().toLowerCase().contains(term.toLowerCase()))))
                .limit(10)
                .toList();
    }
}
