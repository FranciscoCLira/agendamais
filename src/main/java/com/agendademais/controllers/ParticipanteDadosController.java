package com.agendademais.controllers;

import com.agendademais.entities.Local;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.services.LocalService;
import com.agendademais.utils.LocalFormUtil;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// @Controller - DESABILITADO: conflito com MeusDadosController unificado
@RequestMapping("/participante/dados-old")
public class ParticipanteDadosController {

    private final PessoaRepository pessoaRepository;
    private final LocalService localService;

    public ParticipanteDadosController(PessoaRepository pessoaRepository, LocalService localService) {
        this.pessoaRepository = pessoaRepository;
        this.localService = localService;
    }

    // Utilitario com.agendademais.utils/LocalFormUtil.java
    private String recarregarViewComListas(Model model, Pessoa pessoa) {
        LocalFormUtil.preencherListasLocais(model, localService, pessoa);
        model.addAttribute("pessoa", pessoa);
        return "participante/meus-dados";
    }

    @GetMapping
    public String exibirMeusDados(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null || usuario.getPessoa() == null) {
            model.addAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }
        Pessoa pessoa = usuario.getPessoa();

        String nomePais = pessoa.getNomePais() != null ? pessoa.getNomePais().trim() : null;

        List<Local> paises = localService.listarPorTipo(1);

        // Verifica se o país da pessoa existe na lista de países disponíveis
        boolean paisExiste = nomePais == null || paises.stream()
                .anyMatch(p -> p.getNomeLocal().equalsIgnoreCase(nomePais));

        List<Local> estados = Collections.emptyList();
        if (nomePais != null && paisExiste) {
            estados = localService.listarEstadosPorPais(nomePais);
        }

        // Cidades são carregadas dinamicamente via JavaScript

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("paises", paises);
        model.addAttribute("estados", estados);

        return "participante/meus-dados";

    }

    // --- POST: SALVAR ALTERAÇÕES ---

    @PostMapping("/salvar")
    public String salvarMeusDados(
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

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null || usuario.getPessoa() == null) {
            return "redirect:/acesso";
        }

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

        // Validações simples de obrigatoriedade
        if (paisNome == null || paisNome.isBlank() || "Outro".equals(paisNome)) {
            model.addAttribute("mensagemErro", "Informe o País.");
            return recarregarViewComListas(model, pessoa);
        }
        if (estadoNome == null || estadoNome.isBlank() || "Outro".equals(estadoNome)) {
            model.addAttribute("mensagemErro", "Informe o Estado.");
            return recarregarViewComListas(model, pessoa);
        }
        if (cidadeNome == null || cidadeNome.isBlank() || "Outro".equals(cidadeNome)) {
            model.addAttribute("mensagemErro", "Informe a Cidade.");
            return recarregarViewComListas(model, pessoa);
        }

        // Cria automaticamente os locais se não existirem e define as referências
        try {
            // Busca ou cria o país
            Local paisLocal = localService.buscarOuCriar(1, paisNome, null);

            // Busca ou cria o estado
            Local estadoLocal = localService.buscarOuCriar(2, estadoNome, paisLocal);

            // Busca ou cria a cidade
            Local cidadeLocal = localService.buscarOuCriar(3, cidadeNome, estadoLocal); // Atualiza os dados no objeto
                                                                                        // persistido (importante!
                                                                                        // garantir que está usando
            // o objeto correto)
            Pessoa pessoaDb = usuario.getPessoa();
            pessoaDb.setNomePessoa(pessoa.getNomePessoa());
            pessoaDb.setEmailPessoa(pessoa.getEmailPessoa());
            pessoaDb.setCelularPessoa(pessoa.getCelularPessoa());
            pessoaDb.setCurriculoPessoal(pessoa.getCurriculoPessoal());
            pessoaDb.setComentarios(pessoa.getComentarios());

            // Define as referências normalizadas
            pessoaDb.setPais(paisLocal);
            pessoaDb.setEstado(estadoLocal);
            pessoaDb.setCidade(cidadeLocal);

            pessoaRepository.save(pessoaDb);
            session.setAttribute("usuarioLogado", usuario);

            System.out.println("Pessoa atualizada com locais normalizados: País=" + paisLocal.getId() +
                    ", Estado=" + estadoLocal.getId() + ", Cidade=" + cidadeLocal.getId());

        } catch (Exception e) {
            System.err.println("Erro ao criar/buscar locais: " + e.getMessage());
            model.addAttribute("mensagemErro", "Erro interno ao processar localização. Tente novamente.");
            return recarregarViewComListas(model, pessoa);
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados alterados com sucesso.");
        return "redirect:/meus-dados";
    }
}
