package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import com.agendademais.services.LocalService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/cadastro-pessoa")
public class CadastroPessoaController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final LocalService localService; // Para preencher listas de países, estados, cidades

    public CadastroPessoaController(
            UsuarioRepository usuarioRepository,
            PessoaRepository pessoaRepository,
            LocalService localService) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.localService = localService;
    }

    @GetMapping
    public String mostrarFormulario(Model model, HttpSession session) {
        System.out.println("*** DEBUG CadastroPessoaController.mostrarFormulario() ***");

        Usuario usuario = (Usuario) session.getAttribute("usuarioCadastro");
        System.out.println("Usuário da sessão: " + (usuario != null ? usuario.getUsername() : "null"));

        if (usuario == null) {
            // Se não está na sessão, volta ao cadastro de usuário
            System.out.println("Usuário não encontrado na sessão, redirecionando...");
            model.addAttribute("mensagemErro", "Faça o cadastro do usuário antes.");
            return "redirect:/cadastro-usuario";
        }

        // Se Pessoa já existe, carrega, senão crie nova
        Pessoa pessoa = usuario.getPessoa() != null ? usuario.getPessoa() : new Pessoa();
        System.out.println(
                "Pessoa carregada: " + (pessoa.getNomePessoa() != null ? pessoa.getNomePessoa() : "nova pessoa"));

        String nomePais = pessoa.getNomePais() != null ? pessoa.getNomePais().trim() : null;

        List<Local> paises = localService.listarPorTipo(1);
        System.out.println("Países carregados: " + paises.size());

        // Verifica se o país da pessoa existe na lista de países disponíveis
        boolean paisExiste = nomePais == null || paises.stream()
                .anyMatch(p -> p.getNomeLocal().equalsIgnoreCase(nomePais));

        List<Local> estados = Collections.emptyList();
        if (nomePais != null && paisExiste) {
            estados = localService.listarEstadosPorPais(nomePais);
        }

        // Cidades são carregadas dinamicamente via JavaScript

        // Adiciona celular formatado para exibição
        if (pessoa.getCelularPessoa() != null && pessoa.getCelularPessoa().length() == 13) {
            String celularFormatado = com.agendademais.utils.StringUtils.formatarCelularParaExibicao(pessoa.getCelularPessoa());
            model.addAttribute("celularPessoaFormatado", celularFormatado);
        } else {
            model.addAttribute("celularPessoaFormatado", pessoa.getCelularPessoa());
        }

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("paises", paises);
        model.addAttribute("estados", estados);
        model.addAttribute("username", usuario.getUsername());

        System.out.println("*** DEBUG antes de retornar template ***");
        System.out.println("Template: cadastro-pessoa");
        System.out.println("Username no model: " + usuario.getUsername());
        System.out.println("Sessão ID: " + session.getId());

        return "cadastro-pessoa";
    }

    @Transactional
    @PostMapping
    public String processarCadastroPessoa(
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

        Usuario usuario = (Usuario) session.getAttribute("usuarioCadastro");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Sessão expirada. Faça o cadastro do usuário novamente.");
            return "redirect:/cadastro-usuario";
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

        // Validação de campos obrigatórios
        if (pessoa.getNomePessoa() == null || pessoa.getNomePessoa().isBlank()) {
            model.addAttribute("mensagemErro", "Nome é obrigatório.");
            return recarregarViewComListas(model, pessoa, usuario);
        }
        if (pessoa.getEmailPessoa() == null || pessoa.getEmailPessoa().isBlank()) {
            model.addAttribute("mensagemErro", "E-mail é obrigatório.");
            return recarregarViewComListas(model, pessoa, usuario);
        }

        List<Pessoa> existentes = pessoaRepository.findAllByEmailPessoa(pessoa.getEmailPessoa());
        if (!existentes.isEmpty()) {
            model.addAttribute("mensagemErro",
                    "Este Email já possui cadastro. <a href='/login/recuperar-login-email'>Quer recuperá-lo?</a>");
            return recarregarViewComListas(model, pessoa, usuario);
        }

        // Validações de país, estado, cidade
        if (paisNome == null || paisNome.isBlank() || "Outro".equals(paisNome)) {
            model.addAttribute("mensagemErro", "Informe o País.");
            return recarregarViewComListas(model, pessoa, usuario);
        }
        if (estadoNome == null || estadoNome.isBlank() || "Outro".equals(estadoNome)) {
            model.addAttribute("mensagemErro", "Informe o Estado.");
            return recarregarViewComListas(model, pessoa, usuario);
        }
        if (cidadeNome == null || cidadeNome.isBlank() || "Outro".equals(cidadeNome)) {
            model.addAttribute("mensagemErro", "Informe a Cidade.");
            return recarregarViewComListas(model, pessoa, usuario);
        }

        // Cria automaticamente os locais se não existirem e define as referências
        try {
            // Busca ou cria o país
            Local paisLocal = localService.buscarOuCriar(1, paisNome, null);

            // Busca ou cria o estado
            Local estadoLocal = localService.buscarOuCriar(2, estadoNome, paisLocal);

            // Busca ou cria a cidade
            Local cidadeLocal = localService.buscarOuCriar(3, cidadeNome, estadoLocal);

            // Define as referências normalizadas
            pessoa.setPais(paisLocal);
            pessoa.setEstado(estadoLocal);
            pessoa.setCidade(cidadeLocal);

            System.out.println("Pessoa cadastrada com locais normalizados: País=" + paisLocal.getId() +
                    ", Estado=" + estadoLocal.getId() + ", Cidade=" + cidadeLocal.getId());

        } catch (Exception e) {
            System.err.println("*** ERRO DETALHADO no cadastro-pessoa ***");
            System.err.println("Erro ao criar/buscar locais no cadastro: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("mensagemErro", "Erro interno ao processar localização: " + e.getMessage());
            return recarregarViewComListas(model, pessoa, usuario);
        }

        // Normalizar celular: salvar apenas números
        if (pessoa.getCelularPessoa() != null && !pessoa.getCelularPessoa().isBlank()) {
            pessoa.setCelularPessoa(com.agendademais.utils.StringUtils.somenteNumeros(pessoa.getCelularPessoa()));
        }

        // Salvar comentários e currículo como null se vierem em branco
        if (pessoa.getComentarios() != null && pessoa.getComentarios().isBlank()) {
            pessoa.setComentarios(null);
        }
        if (pessoa.getCurriculoPessoal() != null && pessoa.getCurriculoPessoal().isBlank()) {
            pessoa.setCurriculoPessoal(null);
        }

        // System.out.println("*** CadastroPessoaController.java /cadastro-pessoa =" +
        // "Erro inesperado ao processar o cadastro.");
        // System.out.println("****************************************************************************");

        // Usa o objeto já preenchido e completa com dados adicionais

        pessoa.setSituacaoPessoa("A");
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa);

        // REMOVIDO: setNivelAcessoUsuario - agora está em UsuarioInstituicao
        usuario.setSituacaoUsuario("A");
        usuario.setDataUltimaAtualizacao(LocalDate.now());

        // Relaciona ao usuário e salva

        usuario.setPessoa(pessoa);
        // Criptografar senha se ainda não estiver criptografada
        String senha = usuario.getPassword();
        if (senha != null && !senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
            usuario.setPassword(passwordEncoder.encode(senha));
        }
        usuarioRepository.save(usuario);

        // Remove da sessão (opcional)
        session.removeAttribute("usuarioCadastro");

        // Opcional: loga direto, ou salva para continuar fluxo
        session.setAttribute("usuarioPendencia", usuario);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Informações salvas. Agora escolha suas instituições.");

        return "redirect:/cadastro-relacionamentos?username=" + usuario.getUsername();
    }

    // Método utilitário para recarregar a view com listas
    private String recarregarViewComListas(Model model, Pessoa pessoa, Usuario usuario) {
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
        model.addAttribute("username", usuario != null ? usuario.getUsername() : null);
        return "cadastro-pessoa";
    }
}
