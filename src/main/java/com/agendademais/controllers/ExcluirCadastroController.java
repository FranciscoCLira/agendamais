package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.Pessoa;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ExcluirCadastroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @PostMapping("/excluir-cadastro")
    @Transactional
    public String excluirCadastro(HttpSession session, RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null || usuario.getPessoa() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/login";
        }

        Pessoa pessoa = usuario.getPessoa();

        if (!"C".equalsIgnoreCase(pessoa.getSituacaoPessoa())) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Cadastro só pode ser excluído após ser cancelado.");
            return "redirect:/participante";
        }

        // Excluir vínculos antes do usuário
        usuarioInstituicaoRepository.deleteAllByUsuario(usuario);

        // Excluir o usuário (e pessoa associada, se mapeamento permitir)
        usuarioRepository.delete(usuario);

        // Finaliza sessão
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro excluído com sucesso.");
        return "redirect:/login";
    }
}
