package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.entities.SubInstituicao;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.SubInstituicaoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.text.Normalizer;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @GetMapping
    public String listarUsuarios(
            HttpSession session,
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "filtroCodigo", required = false) String filtroCodigo,
            @RequestParam(value = "filtroNome", required = false) String filtroNome,
            @RequestParam(value = "filtroEmail", required = false) String filtroEmail,
            @RequestParam(value = "filtroSubInstituicao", required = false) String filtroSubInstituicao,
            @RequestParam(value = "filtroStatus", required = false) String filtroStatus,
            @RequestParam(value = "filtroPais", required = false) String filtroPais,
            @RequestParam(value = "filtroEstado", required = false) String filtroEstado,
            @RequestParam(value = "filtroCidade", required = false) String filtroCidade) {
        // Recupera a instituição selecionada da sessão
        Object instObj = session.getAttribute("instituicaoSelecionada");
        if (instObj == null) {
            model.addAttribute("mensagemErro", "Instituição não selecionada.");
            return "gestao-usuarios/lista-usuarios";
        }
        com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) instObj;

        // Recupera o nível de acesso do usuário logado para manter o link
        // 'Administrador'
        Integer nivelAcessoLogado = (Integer) session.getAttribute("nivelAcessoAtual");
        model.addAttribute("nivelAcessoLogado", nivelAcessoLogado);

        // Busca todos os usuários da instituição
        List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository
                .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicao);

        // Carrega sub-instituições ativas da instituição
        List<SubInstituicao> subInstituicoes = subInstituicaoRepository.findBySituacaoSubInstituicao("A");
        // Filtra para a instituição selecionada
        subInstituicoes = subInstituicoes.stream()
                .filter(sub -> sub.getInstituicao() != null && sub.getInstituicao().getId().equals(instituicao.getId()))
                .toList();
        model.addAttribute("subInstituicoes", subInstituicoes);

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
                // Nenhuma sub-instituição
                usuarios = usuarios.stream()
                        .filter(ui -> ui.getUsuario().getPessoa().getPessoaSubInstituicao() == null ||
                                ui.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty() ||
                                ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0) == null ||
                                ui.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                                        .getSubInstituicao() == null)
                        .toList();
            } else {
                try {
                    Long filtroSubId = Long.parseLong(filtroSubInstituicao);
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
                                                    .getSubInstituicao().getId().equals(filtroSubId);
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .toList();
                } catch (NumberFormatException e) {
                    // Se não for um id válido, não filtra nada
                    usuarios = usuarios.stream().filter(ui -> false).toList();
                }
            }
        }
        if (filtroStatus != null && !filtroStatus.trim().isEmpty()) {
            usuarios = usuarios.stream()
                    .filter(ui -> filtroStatus.equalsIgnoreCase(ui.getSitAcessoUsuarioInstituicao()))
                    .toList();
        }
        if (filtroPais != null && !filtroPais.trim().isEmpty()) {
            String paisFiltro = removerAcentos(filtroPais);
            usuarios = usuarios.stream()
                    .filter(ui -> ui.getUsuario().getPessoa().getNomePais() != null &&
                            removerAcentos(ui.getUsuario().getPessoa().getNomePais()).contains(paisFiltro))
                    .toList();
        }
        if (filtroEstado != null && !filtroEstado.trim().isEmpty()) {
            String estadoFiltro = removerAcentos(filtroEstado);
            usuarios = usuarios.stream()
                    .filter(ui -> ui.getUsuario().getPessoa().getNomeEstado() != null &&
                            removerAcentos(ui.getUsuario().getPessoa().getNomeEstado()).contains(estadoFiltro))
                    .toList();
        }
        if (filtroCidade != null && !filtroCidade.trim().isEmpty()) {
            String cidadeFiltro = removerAcentos(filtroCidade);
            usuarios = usuarios.stream()
                    .filter(ui -> ui.getUsuario().getPessoa().getNomeCidade() != null &&
                            removerAcentos(ui.getUsuario().getPessoa().getNomeCidade()).contains(cidadeFiltro))
                    .toList();
        }

        // Paginação manual robusta
        int totalElements = usuarios.size();
        int safeSize = size > 0 ? size : 10;
        // Corrige para garantir 10 na primeira carga (quando size=100 default)
        if (size == 100 && (filtroCodigo == null && filtroNome == null && filtroEmail == null
                && filtroSubInstituicao == null && filtroStatus == null && filtroPais == null && filtroEstado == null
                && filtroCidade == null)) {
            safeSize = 10;
            model.addAttribute("size", 10);
        }
        int safePage = page >= 0 ? page : 0;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (safePage >= totalPages)
            safePage = totalPages > 0 ? totalPages - 1 : 0;
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<UsuarioInstituicao> usuariosPaginados = usuarios.subList(fromIndex, toIndex);

        // Adiciona flag podeEditar para cada usuário
        List<Boolean> podeEditarList = usuariosPaginados.stream()
                .map(ui -> {
                    if (nivelAcessoLogado != null && nivelAcessoLogado == 5) {
                        return ui.getNivelAcessoUsuarioInstituicao() <= 5;
                    } else {
                        return true;
                    }
                })
                .toList();
        model.addAttribute("usuarios", usuariosPaginados);
        model.addAttribute("podeEditarList", podeEditarList);
        model.addAttribute("page", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("filtroCodigo", filtroCodigo);
        model.addAttribute("filtroNome", filtroNome);
        model.addAttribute("filtroEmail", filtroEmail);
        model.addAttribute("filtroSubInstituicao", filtroSubInstituicao);
        model.addAttribute("filtroStatus", filtroStatus);
        model.addAttribute("filtroPais", filtroPais);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("filtroCidade", filtroCidade);
        model.addAttribute("nomeInstituicao", instituicao.getNomeInstituicao());
        return "gestao-usuarios/lista-usuarios";
    }

    // Utilitário para remover acentos e converter para minúsculo
    private String removerAcentos(String texto) {
        if (texto == null)
            return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    @PostMapping("/alterar-nivel")
    public String alterarNivel(@RequestParam Long id, @RequestParam int novoNivel, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Usuario alvo = usuarioRepository.findById(id).orElse(null);
        Integer nivelAcessoAtual = (Integer) session.getAttribute("nivelAcessoAtual");

        if (alvo == null || usuarioLogado == null || nivelAcessoAtual == null)
            return "redirect:/usuarios";

        // NOTA: Este controlador precisa ser refatorado para trabalhar com níveis por
        // instituição
        // Por enquanto, mantendo verificação básica
        boolean permissao = nivelAcessoAtual == 9 ||
                (nivelAcessoAtual == 5 && novoNivel < 5);

        if (permissao) {
            // FIXME: Este método não deveria mais existir - níveis são por instituição
            // agora
            // alvo.setNivelAcessoUsuario(novoNivel);
            // usuarioRepository.save(alvo);
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/remover")
    public String removerUsuario(@RequestParam Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Usuario alvo = usuarioRepository.findById(id).orElse(null);
        Integer nivelAcessoAtual = (Integer) session.getAttribute("nivelAcessoAtual");

        if (alvo == null || usuarioLogado == null || nivelAcessoAtual == null)
            return "redirect:/usuarios";

        // NOTA: Este controlador precisa ser refatorado para trabalhar com níveis por
        // instituição
        // Por enquanto, mantendo verificação básica usando sessão
        boolean permissao = nivelAcessoAtual == 9 ||
                (nivelAcessoAtual == 5); // Simplificando por enquanto

        if (permissao) {
            usuarioRepository.delete(alvo);
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/situacao")
    public String alterarSituacaoUsuarioInstituicao(@RequestParam Long idUsuario, @RequestParam String novaSituacao,
            HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcessoAtual = (Integer) session.getAttribute("nivelAcessoAtual");

        if (usuarioLogado == null || nivelAcessoAtual == null)
            return "redirect:/acesso";

        List<UsuarioInstituicao> acessos = usuarioInstituicaoRepository.findByUsuarioId(idUsuario);

        for (UsuarioInstituicao acesso : acessos) {
            // NOTA: Esta lógica precisa ser refatorada
            // Por enquanto, simplificando verificação de permissão
            boolean podeAlterar = nivelAcessoAtual == 9 ||
                    (nivelAcessoAtual == 5 &&
                            usuarioInstituicaoRepository.existsByUsuarioIdAndInstituicaoId(
                                    usuarioLogado.getId(), acesso.getInstituicao().getId()));

            if (podeAlterar) {
                acesso.setSitAcessoUsuarioInstituicao(novaSituacao);
                usuarioInstituicaoRepository.save(acesso);
            }
        }

        return "redirect:/usuarios";
    }
}
