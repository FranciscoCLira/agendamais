package com.agendademais.controllers;

import com.agendademais.dto.UsuarioInstituicaoDTO;

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
import com.agendademais.entities.SubInstituicao;
import com.agendademais.entities.PessoaSubInstituicao;
import com.agendademais.repositories.SubInstituicaoRepository;
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

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

        // Verificar permissões
        if (usuarioLogado == null || nivelAcesso == null) {
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
            List<UsuarioInstituicao> usuarios;
            if (nivelAcesso == 9) {
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
            } else {
                usuarios = usuarioInstituicaoRepository
                        .findByInstituicaoAndNivelAcessoUsuarioInstituicaoLessThanEqualOrderByNivelAcessoUsuarioInstituicaoAsc(
                                instituicaoSelecionada, 5);
            }
            // REMOVER O USUÁRIO LOGADO DA LISTA
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
            // Paginação manual
            int safeSize = (size == null || size <= 0) ? 10 : size;
            int totalElements = usuarios.size();
            int fromIndex = Math.min(page * safeSize, totalElements);
            int toIndex = Math.min(fromIndex + safeSize, totalElements);
            List<UsuarioInstituicao> usuariosPaginados = usuarios.subList(fromIndex, toIndex);
            int totalPages = (int) Math.ceil((double) totalElements / safeSize);
            // Carrega sub-instituições ativas da instituição
            List<SubInstituicao> subInstituicoes = subInstituicaoRepository
                    .findByInstituicaoAndSituacaoSubInstituicao(instituicaoSelecionada, "A");
            model.addAttribute("subInstituicoes", subInstituicoes);
            // Converter para DTO
            List<UsuarioInstituicaoDTO> usuariosDTO = usuariosPaginados.stream()
                    .map(ui -> new UsuarioInstituicaoDTO(ui, null)) // ajuste o segundo parâmetro se necessário
                    .toList();
            model.addAttribute("usuarios", usuariosDTO);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);
            model.addAttribute("page", page);
            model.addAttribute("totalPages", totalPages > 0 ? totalPages : 1);
            model.addAttribute("size", safeSize);
            return "gestao-usuarios/lista-usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao carregar lista de usuários: " + e.getMessage());
            return "redirect:/administrador";
        }
    }

    @GetMapping("/editar-usuarios")
    public String editarUsuarios(
            @RequestParam(value = "filtroCodigo", required = false) String filtroCodigo,
            @RequestParam(value = "filtroNome", required = false) String filtroNome,
            @RequestParam(value = "filtroEmail", required = false) String filtroEmail,
            @RequestParam(value = "filtroSubInstituicao", required = false) String filtroSubInstituicao,
            @RequestParam(value = "filtroStatus", required = false) String filtroStatus,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

        // Verificar permissões
        if (usuarioLogado == null || nivelAcesso == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Sua sessão expirou por inatividade ou o sistema foi reiniciado. Faça login novamente para continuar.");
            return "redirect:/acesso";
        }
        if (nivelAcesso < 5) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
            return "redirect:/acesso";
        }
        if (instituicaoSelecionada == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro: Instituição não selecionada. Faça login novamente.");
            return "redirect:/acesso";
        }
        try {
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository
                    .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);

            // Filtros em memória, removendo acentos
            if (filtroCodigo != null && !filtroCodigo.trim().isEmpty()) {
                String codigoFiltro = removerAcentos(filtroCodigo);
                usuarios = usuarios.stream()
                        .filter(ui -> removerAcentos(ui.getUsuario().getUsername()).contains(codigoFiltro))
                        .toList();
            }
            if (filtroNome != null && !filtroNome.trim().isEmpty()) {
                String nomeFiltro = removerAcentos(filtroNome);
                usuarios = usuarios.stream()
                        .filter(ui -> removerAcentos(ui.getUsuario().getPessoa().getNomePessoa()).contains(nomeFiltro))
                        .toList();
            }
            if (filtroEmail != null && !filtroEmail.trim().isEmpty()) {
                String emailFiltro = filtroEmail.trim().toLowerCase();
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getPessoa().getEmailPessoa().toLowerCase().contains(emailFiltro))
                        .toList();
            }
            if (filtroSubInstituicao != null && !filtroSubInstituicao.trim().isEmpty()) {
                if ("-1".equals(filtroSubInstituicao)) {
                    usuarios = usuarios.stream()
                            .filter(ui -> ui.getUsuario().getPessoa().getPessoaSubInstituicao() == null
                                    || ui.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty()
                                    || ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0) == null
                                    || ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                            .getSubInstituicao() == null)
                            .toList();
                } else {
                    try {
                        Long filtroSubId = Long.parseLong(filtroSubInstituicao);
                        usuarios = usuarios.stream()
                                .filter(ui -> {
                                    try {
                                        return ui.getUsuario().getPessoa().getPessoaSubInstituicao() != null
                                                && !ui.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty()
                                                && ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0) != null
                                                && ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                                        .getSubInstituicao() != null
                                                && ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                                        .getSubInstituicao().getId().equals(filtroSubId);
                                    } catch (Exception e) {
                                        return false;
                                    }
                                })
                                .toList();
                    } catch (NumberFormatException e) {
                        usuarios = usuarios.stream().filter(ui -> false).toList();
                    }
                }
            }
            if (filtroStatus != null && !filtroStatus.trim().isEmpty()) {
                usuarios = usuarios.stream()
                        .filter(ui -> filtroStatus.equalsIgnoreCase(ui.getSitAcessoUsuarioInstituicao()))
                        .toList();
            }

            int safeSize = (size == null || size <= 0) ? 10 : size;
            model.addAttribute("filtroPaginacao", safeSize);
            int totalElements = usuarios.size();
            int fromIndex = Math.min(page * safeSize, totalElements);
            int toIndex = Math.min(fromIndex + safeSize, totalElements);
            List<com.agendademais.dto.UsuarioInstituicaoDTO> usuariosDTO = new java.util.ArrayList<>();
            for (UsuarioInstituicao usuarioInst : usuarios.subList(fromIndex, toIndex)) {
                String nomeSub = null;
                try {
                    if (usuarioInst.getUsuario() != null && usuarioInst.getUsuario().getPessoa() != null
                            && usuarioInst.getUsuario().getPessoa().getPessoaSubInstituicao() != null) {
                        nomeSub = usuarioInst.getUsuario().getPessoa().getPessoaSubInstituicao().stream()
                                .filter(psi -> psi.getInstituicao() != null
                                        && psi.getInstituicao().getId().equals(instituicaoSelecionada.getId()))
                                .map(psi -> psi.getSubInstituicao() != null
                                        ? psi.getSubInstituicao().getNomeSubInstituicao()
                                        : null)
                                .filter(n -> n != null)
                                .findFirst().orElse(null);
                    }
                } catch (Exception e) {
                    nomeSub = null;
                }
                usuariosDTO.add(new com.agendademais.dto.UsuarioInstituicaoDTO(usuarioInst, nomeSub));
            }
            int totalPages = (int) Math.ceil((double) totalElements / safeSize);
            model.addAttribute("usuarios", usuariosDTO);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);
            model.addAttribute("page", page);
            model.addAttribute("totalPages", totalPages > 0 ? totalPages : 1);
            model.addAttribute("size", safeSize);
            model.addAttribute("filtroCodigo", filtroCodigo);
            model.addAttribute("filtroNome", filtroNome);
            model.addAttribute("filtroEmail", filtroEmail);
            model.addAttribute("filtroSubInstituicao", filtroSubInstituicao);
            model.addAttribute("filtroStatus", filtroStatus);
            model.addAttribute("totalUsuarios", usuariosDTO.size());
            List<SubInstituicao> subInstituicoes = subInstituicaoRepository
                    .findByInstituicaoAndSituacaoSubInstituicao(instituicaoSelecionada, "A");
            model.addAttribute("subInstituicoes", subInstituicoes);
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
            @RequestParam(value = "nomeUsuario", required = false) String nomeUsuario,
            @RequestParam(value = "emailUsuario", required = false) String emailUsuario,
            @RequestParam(value = "subInstituicaoUsuario", required = false) String subInstituicaoUsuarioId,
            @RequestParam(value = "origem", required = false) String origem,
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
                if (origem != null && origem.equals("lista-usuarios")) {
                    return "redirect:/usuarios";
                } else {
                    return "redirect:/administrador/editar-usuarios";
                }
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

            // Atualizar nome e email se fornecidos e diferentes do atual
            Pessoa pessoa = usuarioInst.getUsuario().getPessoa();
            if (nomeUsuario != null && !nomeUsuario.trim().isEmpty()
                    && !nomeUsuario.trim().equals(pessoa.getNomePessoa())) {
                pessoa.setNomePessoa(nomeUsuario.trim());
            }
            if (emailUsuario != null && !emailUsuario.trim().isEmpty()
                    && !emailUsuario.trim().equals(pessoa.getEmailPessoa())) {
                pessoa.setEmailPessoa(emailUsuario.trim());
            }
            // Atualizar sub-instituição se fornecida e diferente da atual
            if (subInstituicaoUsuarioId != null) {
                if (subInstituicaoUsuarioId.equals("-1") || subInstituicaoUsuarioId.isEmpty()) {
                    // Desvincular sub-instituição
                    if (pessoa.getPessoaSubInstituicao() != null && !pessoa.getPessoaSubInstituicao().isEmpty()) {
                        pessoa.getPessoaSubInstituicao().clear();
                        // Forçar remoção no banco se necessário
                        pessoaRepository.save(pessoa);
                    }
                } else {
                    try {
                        Long subId = Long.parseLong(subInstituicaoUsuarioId);
                        SubInstituicao novaSub = subInstituicaoRepository.findById(subId).orElse(null);
                        if (novaSub != null) {
                            if (pessoa.getPessoaSubInstituicao() == null
                                    || pessoa.getPessoaSubInstituicao().isEmpty()) {
                                // Cria nova relação se não existir
                                PessoaSubInstituicao psi = new PessoaSubInstituicao();
                                psi.setPessoa(pessoa);
                                psi.setSubInstituicao(novaSub);
                                pessoa.getPessoaSubInstituicao().add(psi);
                            } else {
                                if (pessoa.getPessoaSubInstituicao().get(0).getSubInstituicao() == null
                                        || !pessoa.getPessoaSubInstituicao().get(0).getSubInstituicao().getId()
                                                .equals(subId)) {
                                    pessoa.getPessoaSubInstituicao().get(0).setSubInstituicao(novaSub);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // Ignorar erro de parse ou sub não encontrada
                    }
                }
            }
            pessoaRepository.save(pessoa);

            usuarioInstituicaoRepository.save(usuarioInst);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Usuário atualizado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao atualizar usuário: " + e.getMessage());
        }

        if (origem != null && origem.equals("lista-usuarios")) {
            return "redirect:/usuarios";
        }
        return "redirect:/administrador/editar-usuarios";
    }

    @GetMapping("/lista-usuarios")
    public String listaUsuarios(
            @RequestParam(value = "codigoUsuario", required = false) String codigoUsuario,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "subInstituicao", required = false) String subInstituicao,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "pais", required = false) String pais,
            @RequestParam(value = "statusUsuario", required = false) String statusUsuario,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

            // Verificar permissões
            if (usuarioLogado == null || nivelAcesso == null) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Sua sessão expirou por inatividade ou o sistema foi reiniciado. Faça login novamente para continuar.");
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

            // Carrega sub-instituições ativas da instituição
            List<SubInstituicao> subInstituicoes = subInstituicaoRepository
                    .findByInstituicaoAndSituacaoSubInstituicao(instituicaoSelecionada, "A");
            model.addAttribute("subInstituicoes", subInstituicoes);

            // Aplicar filtros
            if (codigoUsuario != null && !codigoUsuario.trim().isEmpty()) {
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getId().toString().contains(codigoUsuario))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (username != null && !username.trim().isEmpty()) {
                String usernameFiltro = removerAcentos(username);
                usuarios = usuarios.stream()
                        .filter(ui -> removerAcentos(ui.getUsuario().getUsername()).contains(usernameFiltro))
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
                if ("-1".equals(subInstituicao)) {
                    // Nenhuma sub-instituição
                    usuarios = usuarios.stream()
                            .filter(ui -> {
                                try {
                                    return ui.getUsuario().getPessoa().getPessoaSubInstituicao() == null ||
                                            ui.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty() ||
                                            ui.getUsuario().getPessoa().getPessoaSubInstituicao().stream()
                                                    .allMatch(psi -> psi.getSubInstituicao() == null);
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .collect(java.util.stream.Collectors.toList());
                } else if (!"".equals(subInstituicao)) {
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
            }

            // Filtro por situação/status do usuário (ATIVO, BLOQUEADO, CANCELADO)
            if (statusUsuario != null && !statusUsuario.trim().isEmpty()) {
                String statusFiltro = statusUsuario.trim();
                // Mapeamento: ATIVO -> A, BLOQUEADO -> B, CANCELADO -> C
                final String statusInterno;
                if ("ATIVO".equalsIgnoreCase(statusFiltro))
                    statusInterno = "A";
                else if ("BLOQUEADO".equalsIgnoreCase(statusFiltro))
                    statusInterno = "B";
                else if ("CANCELADO".equalsIgnoreCase(statusFiltro))
                    statusInterno = "C";
                else
                    statusInterno = null;
                if (statusInterno != null) {
                    usuarios = usuarios.stream()
                            .filter(ui -> statusInterno
                                    .equalsIgnoreCase(String.valueOf(ui.getSitAcessoUsuarioInstituicao())))
                            .collect(java.util.stream.Collectors.toList());
                }
            }

            // Paginação
            int safeSize = (size == null || size <= 0) ? 10 : size;
            int totalElements = usuarios.size();
            int fromIndex = Math.min(page * safeSize, totalElements);
            int toIndex = Math.min(fromIndex + safeSize, totalElements);
            List<UsuarioInstituicao> usuariosPaginados = usuarios.subList(fromIndex, toIndex);
            int totalPages = (int) Math.ceil((double) totalElements / safeSize);

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

            // Corrigir: criar DTOs a partir da lista já paginada
            java.util.List<com.agendademais.dto.UsuarioInstituicaoDTO> usuariosDTO = new java.util.ArrayList<>();
            for (UsuarioInstituicao usuarioInst : usuariosPaginados) {
                String nomeSubInstituicao = null;
                try {
                    if (usuarioInst.getUsuario() != null && usuarioInst.getUsuario().getPessoa() != null
                            && usuarioInst.getUsuario().getPessoa().getPessoaSubInstituicao() != null) {
                        nomeSubInstituicao = usuarioInst.getUsuario().getPessoa().getPessoaSubInstituicao().stream()
                                .filter(psi -> psi.getInstituicao() != null
                                        && psi.getInstituicao().getId().equals(instituicaoSelecionada.getId()))
                                .map(psi -> psi.getSubInstituicao() != null
                                        ? psi.getSubInstituicao().getNomeSubInstituicao()
                                        : null)
                                .filter(n -> n != null)
                                .findFirst().orElse(null);
                    }
                } catch (Exception e) {
                    nomeSubInstituicao = null;
                }
                usuariosDTO.add(new com.agendademais.dto.UsuarioInstituicaoDTO(usuarioInst, nomeSubInstituicao));
            }
            // Remove qualquer DTO nulo ou com usuarioInstituicao nulo
            usuariosDTO = usuariosDTO.stream()
                    .filter(dto -> dto != null && dto.getUsuarioInstituicao() != null)
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("usuarios", usuariosDTO);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("page", page);
            model.addAttribute("size", safeSize);
            model.addAttribute("totalPages", totalPages > 0 ? totalPages : 1);
            model.addAttribute("estatisticasEstado", estatisticasEstado);
            model.addAttribute("estatisticasPais", estatisticasPais);
            model.addAttribute("estatisticasSubInstituicao", estatisticasSubInstituicao);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);

            // Manter valores dos filtros para exibição
            model.addAttribute("codigoUsuario", codigoUsuario);
            model.addAttribute("username", username);
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            model.addAttribute("subInstituicao", subInstituicao);
            model.addAttribute("estado", estado);
            model.addAttribute("cidade", cidade);
            model.addAttribute("pais", pais);
            model.addAttribute("statusUsuario", statusUsuario); // manter valor selecionado

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
