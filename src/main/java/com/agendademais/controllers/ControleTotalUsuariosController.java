package com.agendademais.controllers;

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

import com.agendademais.entities.Pessoa;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/controle-total")
public class ControleTotalUsuariosController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @GetMapping("/usuarios")
    public String listarTodosUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões - apenas Controle Total (nível 0)
        if (usuarioLogado == null || nivelAcesso == null || nivelAcesso != 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Sessão expirou. Faça login novamente.");
            return "redirect:/acesso";
        }

        try {
            // Buscar todos os usuários de todas as instituições
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAllWithDetails();

            model.addAttribute("usuarios", usuarios);

            return "gestao-usuarios/lista-usuarios-controle-total";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao carregar lista de usuários: " + e.getMessage());
            return "redirect:/controle-total";
        }
    }

    @PostMapping("/atualizar-usuario")
    public String atualizarUsuarioControlTotal(
            @RequestParam Long usuarioInstituicaoId,
            @RequestParam Integer nivelAcessoUsuarioInstituicao,
            @RequestParam String sitAcessoUsuarioInstituicao,
            @RequestParam String situacaoPessoa,
            @RequestParam(required = false) String filtroUsuario,
            @RequestParam(required = false) String filtroInstituicao,
            @RequestParam(required = false) String filtroDataIni,
            @RequestParam(required = false) String filtroDataFim,
            @RequestParam(required = false) String filtroOrdenacao,
            @RequestParam(required = false) String itensPorPagina,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões - apenas Controle Total (nível 0)
        if (usuarioLogado == null || nivelAcesso == null || nivelAcesso != 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Sessão expirou. Faça login novamente.");
            return "redirect:/acesso";
        }

        try {
            Optional<UsuarioInstituicao> usuarioInstOpt = usuarioInstituicaoRepository.findById(usuarioInstituicaoId);

            if (usuarioInstOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
                return "redirect:/controle-total/usuarios";
            }

            UsuarioInstituicao usuarioInst = usuarioInstOpt.get();

            // Atualizar dados do UsuarioInstituicao
            usuarioInst.setNivelAcessoUsuarioInstituicao(nivelAcessoUsuarioInstituicao);
            usuarioInst.setSitAcessoUsuarioInstituicao(sitAcessoUsuarioInstituicao);
            usuarioInstituicaoRepository.save(usuarioInst);

            // Atualizar situação da Pessoa
            Pessoa pessoa = usuarioInst.getUsuario().getPessoa();
            pessoa.setSituacaoPessoa(situacaoPessoa);
            pessoa.setDataUltimaAtualizacao(java.time.LocalDate.now());
            pessoaRepository.save(pessoa);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Usuário atualizado com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao atualizar usuário: " + e.getMessage());
        }

        StringBuilder redirectUrl = new StringBuilder("redirect:/controle-total/usuarios?");
        if (filtroUsuario != null && !filtroUsuario.trim().isEmpty())
            redirectUrl.append("filtroUsuario=").append(filtroUsuario).append("&");
        if (filtroInstituicao != null && !filtroInstituicao.trim().isEmpty())
            redirectUrl.append("filtroInstituicao=").append(filtroInstituicao).append("&");
        if (filtroDataIni != null && !filtroDataIni.trim().isEmpty())
            redirectUrl.append("filtroDataIni=").append(filtroDataIni).append("&");
        if (filtroDataFim != null && !filtroDataFim.trim().isEmpty())
            redirectUrl.append("filtroDataFim=").append(filtroDataFim).append("&");
        if (filtroOrdenacao != null && !filtroOrdenacao.trim().isEmpty())
            redirectUrl.append("filtroOrdenacao=").append(filtroOrdenacao).append("&");
        if (itensPorPagina != null && !itensPorPagina.trim().isEmpty())
            redirectUrl.append("itensPorPagina=").append(itensPorPagina).append("&");
        return redirectUrl.toString();
    }
}
