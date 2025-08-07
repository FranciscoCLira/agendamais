package com.agendademais.controllers;

import com.agendademais.entities.Local;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.services.LocalService;
import com.agendademais.utils.LocalFormUtil;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller unificado para meus-dados - funciona para todos os níveis de
 * usuário
 * (participante, autor, administrador, super-usuario, controle-total)
 */
@Controller
public class MeusDadosController {

    private final PessoaRepository pessoaRepository;
    private final LocalService localService;

    public MeusDadosController(PessoaRepository pessoaRepository, LocalService localService) {
        this.pessoaRepository = pessoaRepository;
        this.localService = localService;
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

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados atualizados com sucesso!");

            // Redireciona de volta para /meus-dados em modo readonly
            return "redirect:/meus-dados";

        } catch (Exception e) {
            model.addAttribute("mensagemErro", "Erro ao atualizar dados: " + e.getMessage());
            return recarregarViewComListas(model, pessoa, tipoUsuario);
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
}
