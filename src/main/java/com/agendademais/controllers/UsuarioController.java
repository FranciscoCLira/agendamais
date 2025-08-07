package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping
    public String listarUsuarios(HttpSession session, Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("nomeInstituicao", session.getAttribute("nomeInstituicao"));
        return "usuario-lista";
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
