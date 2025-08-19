package com.agendademais.controllers;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador")
public class GestaoUsuariosController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

        // Verificar permissões
        if (usuarioLogado == null || nivelAcesso == null) {
            // Sessão expirada - redirecionar para acesso com mensagem amigável
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirou. Faça login novamente.");
            return "redirect:/acesso";
        }

        if (nivelAcesso < 5) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
            return "redirect:/acesso";
        }

        if (instituicaoSelecionada == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro: Instituição não selecionada.");
            return "redirect:/acesso";
        }

        try {
            // Buscar usuários da instituição com nível de acesso igual ou inferior ao
            // usuário logado
            List<UsuarioInstituicao> usuarios;

            if (nivelAcesso == 9) { // SuperUsuário - pode ver todos os níveis
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
            } else { // Administrador - pode ver apenas participantes, autores e administradores
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoAndNivelAcessoUsuarioInstituicaoLessThanEqualOrderByNivelAcessoUsuarioInstituicaoAsc(
                                instituicaoSelecionada, 5);
            }

            // REMOVER O USUÁRIO LOGADO DA LISTA (ele acessa seus dados via "Meus Dados")
            usuarios.removeIf(usuarioInst -> usuarioInst.getUsuario().getId().equals(usuarioLogado.getId()));

            // Adiciona celular formatado para exibição em cada usuário
            for (UsuarioInstituicao usuarioInst : usuarios) {
                Pessoa pessoa = usuarioInst.getUsuario().getPessoa();
                if (pessoa != null && pessoa.getCelularPessoa() != null && pessoa.getCelularPessoa().length() == 13) {
                    String celularFormatado = com.agendademais.utils.StringUtils
                            .formatarCelularParaExibicao(pessoa.getCelularPessoa());
                    pessoa.setCelularPessoa(celularFormatado);
                }
            }
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);

            return "gestao-usuarios/lista-usuarios";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao carregar lista de usuários: " + e.getMessage());
            return "redirect:/administrador";
        }
    }

    @GetMapping("/editar-usuarios")
    public String editarUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

        // Verificar permissões
        if (usuarioLogado == null || nivelAcesso == null || nivelAcesso < 5) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
            return "redirect:/acesso";
        }

        if (instituicaoSelecionada == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro: Instituição não selecionada.");
            return "redirect:/acesso";
        }

        try {
            // Buscar usuários da instituição para edição
            List<UsuarioInstituicao> usuarios;

            if (nivelAcesso == 9) { // SuperUsuário - pode editar todos os níveis
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
            } else { // Administrador - pode editar apenas participantes, autores e administradores
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoAndNivelAcessoUsuarioInstituicaoLessThanEqualOrderByNivelAcessoUsuarioInstituicaoAsc(
                                instituicaoSelecionada, 5);
            }

            // REMOVER O USUÁRIO LOGADO DA LISTA (ele acessa seus dados via "Meus Dados")
            usuarios.removeIf(usuarioInst -> usuarioInst.getUsuario().getId().equals(usuarioLogado.getId()));

            model.addAttribute("usuarios", usuarios);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);

            return "gestao-usuarios/editar-usuarios";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao carregar dados para edição: " + e.getMessage());
            return "redirect:/administrador";
        }
    }

    @PostMapping("/atualizar-usuario")
    public String atualizarUsuario(
            @RequestParam Long usuarioInstituicaoId,
            @RequestParam Integer nivelAcessoUsuarioInstituicao,
            @RequestParam String sitAcessoUsuarioInstituicao,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuarioLogado == null || nivelAcesso == null || nivelAcesso < 5) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
            return "redirect:/acesso";
        }

        try {
            Optional<UsuarioInstituicao> usuarioInstOpt = usuarioInstituicaoRepository.findById(usuarioInstituicaoId);

            if (usuarioInstOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
                return "redirect:/administrador/usuarios";
            }

            UsuarioInstituicao usuarioInst = usuarioInstOpt.get();

            // Verificar se o usuário logado tem permissão para alterar este nível
            if (nivelAcesso != 9 && nivelAcessoUsuarioInstituicao > nivelAcesso) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Você não tem permissão para definir um nível de acesso superior ao seu.");
                return "redirect:/administrador/usuarios";
            }

            // Verificar se o usuário logado tem permissão para alterar este usuário
            if (nivelAcesso != 9 && usuarioInst.getNivelAcessoUsuarioInstituicao() > nivelAcesso) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Você não tem permissão para alterar usuários com nível superior ao seu.");
                return "redirect:/administrador/usuarios";
            }

            // Atualizar os dados
            usuarioInst.setNivelAcessoUsuarioInstituicao(nivelAcessoUsuarioInstituicao);
            usuarioInst.setSitAcessoUsuarioInstituicao(sitAcessoUsuarioInstituicao);

            usuarioInstituicaoRepository.save(usuarioInst);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Usuário atualizado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao atualizar usuário: " + e.getMessage());
        }

        return "redirect:/administrador/usuarios";
    }

    @GetMapping("/lista-usuarios")
    public String listaUsuarios(
            @RequestParam(value = "codigoUsuario", required = false) String codigoUsuario,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "subInstituicao", required = false) String subInstituicao,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "pais", required = false) String pais,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

            // Verificar permissões
            if (usuarioLogado == null || nivelAcesso == null) {
                // Sessão expirada - redirecionar para acesso com mensagem amigável
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirou. Faça login novamente.");
                return "redirect:/acesso";
            }

            if (nivelAcesso < 5) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
                return "redirect:/acesso";
            }

            // Buscar usuários da instituição selecionada
            List<UsuarioInstituicao> usuarios;
            if (nivelAcesso == 9) {
                usuarios = usuarioInstituicaoRepository.findAll();
            } else {
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
            }

            // NOTA: Incluindo o usuário logado na lista pois esta é uma view apenas de
            // consulta

            // Aplicar filtros
            if (codigoUsuario != null && !codigoUsuario.trim().isEmpty()) {
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getId().toString().contains(codigoUsuario))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (nome != null && !nome.trim().isEmpty()) {
                String nomeFiltro = removerAcentos(nome);
                usuarios = usuarios.stream()
                        .filter(ui -> removerAcentos(ui.getUsuario().getPessoa().getNomePessoa())
                                .contains(nomeFiltro))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (email != null && !email.trim().isEmpty()) {
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getPessoa().getEmailPessoa().toLowerCase()
                                .contains(email.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (estado != null && !estado.trim().isEmpty()) {
                String estadoFiltro = removerAcentos(estado);
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getPessoa().getNomeEstado() != null &&
                                removerAcentos(ui.getUsuario().getPessoa().getNomeEstado())
                                        .contains(estadoFiltro))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (cidade != null && !cidade.trim().isEmpty()) {
                String cidadeFiltro = removerAcentos(cidade);
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getPessoa().getNomeCidade() != null &&
                                removerAcentos(ui.getUsuario().getPessoa().getNomeCidade())
                                        .contains(cidadeFiltro))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (pais != null && !pais.trim().isEmpty()) {
                String paisFiltro = removerAcentos(pais);
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getPessoa().getNomePais() != null &&
                                removerAcentos(ui.getUsuario().getPessoa().getNomePais())
                                        .contains(paisFiltro))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (subInstituicao != null && !subInstituicao.trim().isEmpty()) {
                String subInstituicaoFiltro = removerAcentos(subInstituicao);
                usuarios = usuarios.stream()
                        .filter(ui -> {
                            try {
                                return ui.getUsuario().getPessoa().getPessoaSubInstituicao() != null &&
                                        !ui.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty() &&
                                        ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0) != null &&
                                        ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                                .getSubInstituicao() != null
                                        &&
                                        ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                                .getSubInstituicao().getNomeSubInstituicao() != null
                                        &&
                                        removerAcentos(ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                                .getSubInstituicao().getNomeSubInstituicao())
                                                .contains(subInstituicaoFiltro);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .collect(java.util.stream.Collectors.toList());
            }

            // Calcular estatísticas
            long totalUsuarios = usuarios.size();

            // Estatísticas por país/estado
            java.util.Map<String, Long> estatisticasEstado = usuarios.stream()
                    .filter(ui -> ui.getUsuario().getPessoa().getNomeEstado() != null &&
                            ui.getUsuario().getPessoa().getNomePais() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            ui -> ui.getUsuario().getPessoa().getNomePais() + "/" +
                                    ui.getUsuario().getPessoa().getNomeEstado(),
                            java.util.stream.Collectors.counting()));

            // Estatísticas por país
            java.util.Map<String, Long> estatisticasPais = usuarios.stream()
                    .filter(ui -> ui.getUsuario().getPessoa().getNomePais() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            ui -> ui.getUsuario().getPessoa().getNomePais(),
                            java.util.stream.Collectors.counting()));

            // Estatísticas por sub-instituição (incluindo usuários sem sub-instituição)
            java.util.Map<String, Long> estatisticasSubInstituicao = usuarios.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            ui -> {
                                try {
                                    // Encontrar a sub-instituição da instituição atual
                                    return ui.getUsuario().getPessoa().getPessoaSubInstituicao().stream()
                                            .filter(psi -> psi.getInstituicao().getId()
                                                    .equals(instituicaoSelecionada.getId()))
                                            .map(psi -> psi.getSubInstituicao() != null
                                                    ? psi.getSubInstituicao().getNomeSubInstituicao()
                                                    : "Nenhuma")
                                            .findFirst()
                                            .orElse("Nenhuma");
                                } catch (Exception e) {
                                    return "Nenhuma";
                                }
                            },
                            java.util.stream.Collectors.counting()));

            // Adicionar ao modelo
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("estatisticasEstado", estatisticasEstado);
            model.addAttribute("estatisticasPais", estatisticasPais);
            model.addAttribute("estatisticasSubInstituicao", estatisticasSubInstituicao);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);

            // Manter valores dos filtros para exibição
            model.addAttribute("codigoUsuario", codigoUsuario);
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            model.addAttribute("subInstituicao", subInstituicao);
            model.addAttribute("estado", estado);
            model.addAttribute("cidade", cidade);
            model.addAttribute("pais", pais);

            return "gestao-usuarios/lista-usuarios-completa";

        } catch (Exception e) {
            System.out.println("*** ERRO DETALHADO em listaUsuarios ***");
            System.out.println("Tipo: " + e.getClass().getName());
            System.out.println("Mensagem: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao carregar lista de usuários: " + e.getMessage());
            return "redirect:/administrador";
        }
    }

    /**
     * Remove acentos e caracteres especiais de uma string para facilitar busca
     */
    private String removerAcentos(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }
}
