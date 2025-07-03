package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
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

    @GetMapping("/cadastro-relacionamentos")
    public String mostrarFormularioRelacionamentos(@RequestParam String codUsuario, Model model) {
        if (codUsuario == null || codUsuario.trim().isEmpty()) {
            model.addAttribute("mensagemErro", "Código de usuário não informado.");
            return "login";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado.");
            return "login";
        }

        Usuario usuario = usuarioOpt.get();

        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());
        return "cadastro-relacionamentos";
    }


    @PostMapping("/cadastro-relacionamentos")
    public String processarRelacionamentos(
            @RequestParam String codUsuario,
            @RequestParam(required = false) List<Long> instituicoesSelecionadas,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (instituicoesSelecionadas == null || instituicoesSelecionadas.isEmpty()) {
            model.addAttribute("mensagemErro", "Selecione ao menos uma Instituição.");
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("instituicoes", instituicaoRepository.findAll());
            model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());
            return "cadastro-relacionamentos";
        }

        Optional<Usuario> optUsuario = usuarioRepository.findByCodUsuario(codUsuario);
        if (optUsuario.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado.");
            return "cadastro-relacionamentos";
        }
        Usuario usuario = optUsuario.get();
        Pessoa pessoa = usuario.getPessoa();

        if (pessoa == null) {
            model.addAttribute("mensagemErro", "Pessoa vinculada ao usuário não encontrada.");
            return "cadastro-relacionamentos";
        }

        for (Long instId : instituicoesSelecionadas) {
            Instituicao inst = instituicaoRepository.findById(instId).orElse(null);
            if (inst == null) continue;

            // Data de Afiliação
            String dataStr = allParams.get("dataAfiliacao_" + instId);
            LocalDate dataAfiliacao = (dataStr != null && !dataStr.isEmpty()) ? LocalDate.parse(dataStr) : null;

            // Identificação
            String identificacao = allParams.get("identificacao_" + instId);
            if (identificacao != null && identificacao.length() > 20) {
                identificacao = identificacao.substring(0, 20);
            }

            // Salva PessoaInstituicao
            PessoaInstituicao psi = new PessoaInstituicao();
            psi.setPessoa(pessoa);
            psi.setInstituicao(inst);
            psi.setDataAfiliacao(dataAfiliacao);
            psi.setIdentificacaoPessoaInstituicao(identificacao);
            psi.setDataUltimaAtualizacao(LocalDate.now());
            pessoaInstituicaoRepository.save(psi);

            // SubInstituicao
            String subInstIdStr = allParams.get("subInstituicao_" + instId);
            if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                Long subId = Long.parseLong(subInstIdStr);
                SubInstituicao subInst = subInstituicaoRepository.findById(subId).orElse(null);
                if (subInst != null) {
                    PessoaSubInstituicao psiSub = new PessoaSubInstituicao();
                    psiSub.setPessoa(pessoa);
                    psiSub.setInstituicao(inst);
                    psiSub.setSubInstituicao(subInst);
                    psiSub.setDataAfiliacao(dataAfiliacao);
                    psiSub.setIdentificacaoPessoaSubInstituicao(identificacao);
                    psiSub.setDataUltimaAtualizacao(LocalDate.now());
                    pessoaSubInstituicaoRepository.save(psiSub);
                }
            }
        }

        redirectAttributes.addFlashAttribute(
        	    "mensagemSucesso",
        	    "Cadastro concluído com sucesso! Usuário: " + codUsuario + " - " + pessoa.getNomePessoa()
        );
        return "redirect:/cadastro-usuario";

    }
}
