package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ExcluirVinculosInstituicaoController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private PessoaInstituicaoRepository pessoaInstituicaoRepository;

    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    @PostMapping("/excluir-vinculos-instituicao")
    public String excluirVinculos(HttpSession session, RedirectAttributes redirectAttributes) {

        System.out.println("*** ");
        System.out.println("*** 1. ExcluirVinculosInstituicaoController ***********************************");
        System.out.println("*** ");

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuario == null || instituicao == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/login";
        }

        Pessoa pessoa = usuario.getPessoa();
        Long instituicaoId = (Long) instituicao.getId();

        System.out.println("*** 2. ExcluirVinculosInstituicaoController ***********************************");
        System.out.println("***    usuario                     = " + usuario);
        System.out.println("***    usuario.getSituacaoUsuario()= " + usuario.getSituacaoUsuario());
        System.out.println("***    usuario.getPessoa()         = " + usuario.getPessoa());
        System.out.println("***    pessoa.getId()              = " + pessoa.getId());
        System.out.println("***    instituicao                 = " + instituicao);
        System.out.println("***    instituicaoId               = " + instituicaoId);
        System.out.println("****************************************************************************");
        System.out.println("*** ");
        System.out.println(
                "***    Antes delete - usuarioInstituicaoRepository.deleteByUsuarioAndInstituicao(usuario, instituicao");

        // Exclui vínculos com a instituição atual
        usuarioInstituicaoRepository.deleteByUsuarioAndInstituicao(usuario, instituicao);
        inscricaoRepository.deleteByIdPessoaAndIdInstituicao(pessoa, instituicao);
        pessoaInstituicaoRepository.deleteByPessoaAndInstituicao(pessoa, instituicao);
        pessoaSubInstituicaoRepository.deleteByPessoaAndInstituicao(pessoa, instituicao);

        System.out.println("*** ");
        System.out.println("*** 3. Relacionamento com a Instituição excluído com sucesso.");
        System.out.println("****************************************************************************");

        // 2. Verifica se o usuário ainda tem vínculos com outras instituições
        List<UsuarioInstituicao> outrosVinculos = usuarioInstituicaoRepository.findByUsuario(usuario);

        if (outrosVinculos.isEmpty()) {
            // Exclui todas as inscrições vinculadas à pessoa
            inscricaoRepository.deleteByIdPessoa(pessoa);

            // Exclui usuário e pessoa
            usuarioRepository.delete(usuario);
            pessoaRepository.delete(pessoa);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Relacionamento com a Instituição excluído com sucesso.<br>"
                            + "Nenhum outro vínculo restante. Cadastro geral removido.<br>"
                            + usuario.getUsername() + " - " + pessoa.getNomePessoa());

        } else {
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Relacionamento com a Instituição excluído com sucesso:<br>"
                            + usuario.getUsername() + " - " + pessoa.getNomePessoa() + "<br>"
                            + instituicao.getNomeInstituicao());
        }

        session.invalidate();
        return "redirect:/acesso";

    }
}