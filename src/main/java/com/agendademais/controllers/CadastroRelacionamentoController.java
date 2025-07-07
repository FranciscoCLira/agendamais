package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cadastro-relacionamentos")
public class CadastroRelacionamentoController {

	private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public CadastroRelacionamentoController(
    	    UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            SubInstituicaoRepository subInstituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
    	this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.pessoaSubInstituicaoRepository = pessoaSubInstituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @GetMapping
    public String mostrarFormulario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioPendencia");
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("codUsuario", usuario.getCodUsuario());
        model.addAttribute("nomeUsuario",
                usuario.getPessoa() != null ? usuario.getPessoa().getNomePessoa() : "");
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());

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

        // ===== 1) VALIDAÇÃO PRÉVIA - NÃO DELETA NADA AINDA =====
        for (String key : allParams.keySet()) {
            if (key.startsWith("instituicoesSelecionadas")) {
                String instIdStr = allParams.get(key);
                Long instId = Long.parseLong(instIdStr);

                // Data afiliação Instituição
                String dataAfiliacaoStr = allParams.get("dataAfiliacao_" + instId);
                if (dataAfiliacaoStr != null && !dataAfiliacaoStr.isEmpty()) {
                    LocalDate dataAfiliacao = LocalDate.parse(dataAfiliacaoStr);
                    if (dataAfiliacao.isAfter(LocalDate.now())) {
                        model.addAttribute("mensagemErro", "A data de afiliação da instituição não pode ser no futuro.");
                        prepararTela(model, codUsuario, usuario, allParams);
                        return "cadastro-relacionamentos";
                    }
                }

                // SubInstituição
                String subInstIdStr = allParams.get("subInstituicao_" + instId);
                if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                    String dataAfiliacaoSubStr = allParams.get("dataAfiliacaoSub_" + instId);
                    if (dataAfiliacaoSubStr != null && !dataAfiliacaoSubStr.isEmpty()) {
                        LocalDate dataAfiliacaoSub = LocalDate.parse(dataAfiliacaoSubStr);
                        if (dataAfiliacaoSub.isAfter(LocalDate.now())) {
                            model.addAttribute("mensagemErro", "A data de afiliação da subinstituição não pode ser no futuro.");
                            prepararTela(model, codUsuario, usuario, allParams);
                            return "cadastro-relacionamentos";
                        }
                    }
                }
            }
        }

        // ===== 2) TUDO VALIDADO: Deleta antigos =====
        usuarioInstituicaoRepository.deleteAllByUsuarioId(usuario.getId());
        pessoaInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());
        pessoaSubInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());

        // ===== 3) Insere os novos vínculos =====
        for (String key : allParams.keySet()) {
            if (key.startsWith("instituicoesSelecionadas")) {
                String instIdStr = allParams.get(key);
                Long instId = Long.parseLong(instIdStr);

                // Data afiliação
                String dataAfiliacaoStr = allParams.get("dataAfiliacao_" + instId);
                LocalDate dataAfiliacao = (dataAfiliacaoStr != null && !dataAfiliacaoStr.isEmpty())
                        ? LocalDate.parse(dataAfiliacaoStr) : null;

                // Inclui PessoaInstituicao
                PessoaInstituicao psi = new PessoaInstituicao();
                psi.setPessoa(pessoa);
                psi.setInstituicao(instituicaoRepository.findById(instId).orElse(null));
                psi.setDataUltimaAtualizacao(LocalDate.now());
                psi.setDataAfiliacao(dataAfiliacao);

                String identificacao = allParams.get("identificacao_" + instId);
                psi.setIdentificacaoPessoaInstituicao(identificacao);
                pessoaInstituicaoRepository.save(psi);

                // Inclui UsuarioInstituicao
                UsuarioInstituicao ui = new UsuarioInstituicao();
                ui.setUsuario(usuario);
                ui.setInstituicao(psi.getInstituicao());
                ui.setSitAcessoUsuarioInstituicao("A");
                usuarioInstituicaoRepository.save(ui);

                // SubInstituição
                String subInstIdStr = allParams.get("subInstituicao_" + instId);
                if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                    Long subInstId = Long.parseLong(subInstIdStr);
                    SubInstituicao subInst = subInstituicaoRepository.findById(subInstId).orElse(null);

                    if (subInst != null) {
                        String dataAfiliacaoSubStr = allParams.get("dataAfiliacaoSub_" + instId);
                        LocalDate dataAfiliacaoSub = (dataAfiliacaoSubStr != null && !dataAfiliacaoSubStr.isEmpty())
                                ? LocalDate.parse(dataAfiliacaoSubStr) : null;

                        PessoaSubInstituicao psiSub = new PessoaSubInstituicao();
                        psiSub.setPessoa(pessoa);
                        psiSub.setSubInstituicao(subInst);
                        psiSub.setInstituicao(subInst.getInstituicao());
                        psiSub.setDataUltimaAtualizacao(LocalDate.now());
                        psiSub.setDataAfiliacao(dataAfiliacaoSub);

                        psiSub.setIdentificacaoPessoaSubInstituicao(
                                allParams.get("identificacaoSub_" + instId)
                        );

                        pessoaSubInstituicaoRepository.save(psiSub);
                    }
                }
            }
        }

        // Remove dados de sessão
        session.removeAttribute("usuarioPendencia");

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cadastro concluído com sucesso! Usuário: " + usuario.getCodUsuario()
                        + " - " + (pessoa.getNomePessoa() != null ? pessoa.getNomePessoa() : ""));

        return "redirect:/login";
    }
    
    private void prepararTela(Model model, String codUsuario, Usuario usuario, Map<String, String> allParams) {
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());
        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("nomeUsuario", usuario.getPessoa() != null ? usuario.getPessoa().getNomePessoa() : "");
        model.addAttribute("parametrosForm", allParams);
    }

}
