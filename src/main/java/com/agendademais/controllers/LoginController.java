package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.PessoaInstituicaoRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public LoginController(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @GetMapping
    public String loginForm(Model model) {
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "login";
    }

    @PostMapping
    public String processarLogin(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam(required = false) Long instituicao,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha inválida.");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            return "redirect:/login";
        }

        // Verifica se está bloqueado pelo nível de acesso
        if (usuario.getNivelAcessoUsuario() == 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Usuário bloqueado. Consulte o administrador.");
            return "redirect:/login";
        }

        // Verifica se há vínculos
        boolean temVinculos = usuario.getPessoa() != null &&
                pessoaInstituicaoRepository.existsByPessoaId(usuario.getPessoa().getId());

        if (!temVinculos) {
            // Cadastro incompleto - salva na sessão
            session.setAttribute("usuarioPendencia", usuario);
            return "redirect:/cadastro-relacionamentos";
        }

        // Se é SuperUsuário, vai direto ao painel dele
        if (usuario.getNivelAcessoUsuario() == 9) {
            session.setAttribute("usuarioLogado", usuario);
            return "redirect:/superusuario-form";
        }

        // Carrega vínculos ativos
        List<UsuarioInstituicao> vinculosAtivos =
                usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(usuario.getId(), "A");

        if (vinculosAtivos.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Seu acesso foi bloqueado ou cancelado. Consulte o administrador.");
            return "redirect:/login";
        }

        // Se só tem 1 vínculo, salva a instituição e redireciona
        if (vinculosAtivos.size() == 1) {
            UsuarioInstituicao vinculo = vinculosAtivos.get(0);
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("instituicaoSelecionada", vinculo.getInstituicao());
            return redirecionarPorNivel(usuario.getNivelAcessoUsuario());
        }

        // Se mais de um vínculo, exibe seleção de instituição
        redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
        redirectAttributes.addFlashAttribute("instituicoes", vinculosAtivos.stream()
                .map(UsuarioInstituicao::getInstituicao)
                .toList());
        redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
        redirectAttributes.addFlashAttribute("senha", senha);

        return "redirect:/login";
    }

    @PostMapping("/entrar")
    public String processarEscolhaInstituicao(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam Long instituicao,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha inválida.");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            return "redirect:/login";
        }

        // Verifica se o vínculo com esta instituição está ativo
        boolean vinculoAtivo = usuarioInstituicaoRepository
                .existsByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao);

        if (!vinculoAtivo) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Você não tem vínculo ativo com esta instituição.");
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            redirectAttributes.addFlashAttribute("senha", senha);
            redirectAttributes.addFlashAttribute("instituicoes",
                    usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(
                            usuario.getId(), "A"
                    ).stream().map(UsuarioInstituicao::getInstituicao).toList());
            return "redirect:/login";
        }

        // Tudo ok - salva sessão e redireciona
        session.setAttribute("usuarioLogado", usuario);
        
        Instituicao inst = instituicaoRepository.findById(instituicao).orElse(null);
        session.setAttribute("instituicaoSelecionada", inst);

        return redirecionarPorNivel(usuario.getNivelAcessoUsuario());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private String redirecionarPorNivel(int nivel) {
        return switch (nivel) {
            case 1 -> "redirect:/participante-form";
            case 2 -> "redirect:/autor-form";
            case 5 -> "redirect:/administrador-form";
            default -> "redirect:/participante-form";
        };
    }
}
