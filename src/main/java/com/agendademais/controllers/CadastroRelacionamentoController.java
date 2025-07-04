package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession; // ✅ IMPORT CORRETA
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/cadastro-relacionamentos")
public class CadastroRelacionamentoController {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    public CadastroRelacionamentoController(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            SubInstituicaoRepository subInstituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.pessoaSubInstituicaoRepository = pessoaSubInstituicaoRepository;
    }

    @GetMapping
    public String mostrarFormulario(
            HttpSession session,
            Model model) {

        // Recupera usuário da sessão
        Usuario usuario = (Usuario) session.getAttribute("usuarioPendencia");
        // String senha = (String) session.getAttribute("senhaPendencia");

        // Se não tiver na sessão, volta ao login
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("codUsuario", usuario.getCodUsuario());
        model.addAttribute("nomeUsuario", usuario.getPessoa() != null ? usuario.getPessoa().getNomePessoa() : "");
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());
        
        
        // LOG DE TESTE NA CONSOLE 
        List<SubInstituicao> subinstituicoes = subInstituicaoRepository.findAll();
        System.out.println("==== SUBINSTITUICOES ====");
        for (SubInstituicao s : subinstituicoes) {
            System.out.println("ID: " + s.getId() 
                + " | Nome: " + s.getNomeSubInstituicao()
                + " | Instituicao ID: " + (s.getInstituicao() != null ? s.getInstituicao().getId() : "null"));
        }
        model.addAttribute("subInstituicoes", subinstituicoes);

        return "cadastro-relacionamentos";
    }
    
    @Transactional
    @PostMapping
    public String processarCadastroRelacionamentos(
            @RequestParam Map<String, String> allParams,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String codUsuario = allParams.get("codUsuario");
        if (codUsuario == null || codUsuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Código de usuário não informado.");
            return "redirect:/login";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();
        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
            return "redirect:/login";
        }

        // Limpa vínculos antigos
        pessoaInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());
        pessoaSubInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());

        // Processa novos vínculos
        for (String key : allParams.keySet()) {
            if (key.startsWith("instituicoesSelecionadas")) {
                String instIdStr = allParams.get(key);
                Long instId = Long.parseLong(instIdStr);

                PessoaInstituicao psi = new PessoaInstituicao();
                psi.setPessoa(pessoa);
                psi.setInstituicao(instituicaoRepository.findById(instId).orElse(null));
                psi.setDataUltimaAtualizacao(LocalDate.now());

                String dataAfiliacaoStr = allParams.get("dataAfiliacao_" + instId);
                if (dataAfiliacaoStr != null && !dataAfiliacaoStr.isEmpty()) {
                    psi.setDataAfiliacao(LocalDate.parse(dataAfiliacaoStr));
                }

                String identificacao = allParams.get("identificacao_" + instId);
                psi.setIdentificacaoPessoaInstituicao(identificacao);

                pessoaInstituicaoRepository.save(psi);

                String subInstIdStr = allParams.get("subInstituicao_" + instId);
                if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                    Long subInstId = Long.parseLong(subInstIdStr);
                    SubInstituicao subInst = subInstituicaoRepository.findById(subInstId).orElse(null);

                    if (subInst != null) {
                        PessoaSubInstituicao psiSub = new PessoaSubInstituicao();
                        psiSub.setPessoa(pessoa);
                        psiSub.setSubInstituicao(subInst);
                        psiSub.setInstituicao(subInst.getInstituicao());
                        psiSub.setDataUltimaAtualizacao(LocalDate.now());

                        psiSub.setIdentificacaoPessoaSubInstituicao(
                                allParams.get("identificacaoSub_" + instId)
                        );

                        pessoaSubInstituicaoRepository.save(psiSub);
                    }
                }
            }
        }

        // Limpa session
        session.removeAttribute("usuarioPendencia");
        session.removeAttribute("senhaPendencia");

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cadastro concluído com sucesso! Usuário: " + usuario.getCodUsuario()
                + " - " + (pessoa.getNomePessoa() != null ? pessoa.getNomePessoa() : ""));

        return "redirect:/login";
    }
}
