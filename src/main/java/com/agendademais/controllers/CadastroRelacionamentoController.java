package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    public String mostrarFormulario(@RequestParam String codUsuario, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado.");
            return "login";
        }

        Usuario usuario = usuarioOpt.get();

        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("nomeUsuario", usuario.getPessoa() != null ? usuario.getPessoa().getNomePessoa() : "");
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());

        return "cadastro-relacionamentos";
    }

    @Transactional
    @PostMapping
    public String processarRelacionamentos(
            @RequestParam String codUsuario,
            @RequestParam(value = "instituicoesSelecionadas", required = false) List<Long> instituicoesIds,
            @RequestParam(required = false) String identificacao_,
            @RequestParam(required = false) String dataAfiliacao_,
            @RequestParam(required = false) String subInstituicao_,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();
        Pessoa pessoa = usuario.getPessoa();

        if (instituicoesIds == null || instituicoesIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Selecione ao menos uma Instituição.");
            return "redirect:/cadastro-relacionamentos?codUsuario=" + codUsuario;
        }

        for (Long instId : instituicoesIds) {
            Instituicao inst = instituicaoRepository.findById(instId).orElse(null);
            if (inst == null) continue;

            String identificacaoKey = "identificacao_" + instId;
            String dataKey = "dataAfiliacao_" + instId;
            String subKey = "subInstituicao_" + instId;

            String identificacao = identificacao_;
            String dataAfiliacao = dataAfiliacao_;
            String subIdStr = subInstituicao_;

            PessoaInstituicao pi = new PessoaInstituicao();
            pi.setPessoa(pessoa);
            pi.setInstituicao(inst);
            pi.setIdentificacaoPessoaInstituicao(identificacao);
            pi.setDataAfiliacao(dataAfiliacao != null && !dataAfiliacao.isBlank() ? LocalDate.parse(dataAfiliacao) : null);
            pi.setDataUltimaAtualizacao(LocalDate.now());
            pessoaInstituicaoRepository.save(pi);

            if (subIdStr != null && !subIdStr.isBlank()) {
                Long subId = Long.parseLong(subIdStr);
                SubInstituicao sub = subInstituicaoRepository.findById(subId).orElse(null);
                if (sub != null) {
                    PessoaSubInstituicao psi = new PessoaSubInstituicao();
                    psi.setPessoa(pessoa);
                    psi.setInstituicao(inst);
                    psi.setSubInstituicao(sub);
                    psi.setIdentificacaoPessoaSubInstituicao(identificacao);
                    psi.setDataAfiliacao(dataAfiliacao != null && !dataAfiliacao.isBlank() ? LocalDate.parse(dataAfiliacao) : null);
                    psi.setDataUltimaAtualizacao(LocalDate.now());
                    pessoaSubInstituicaoRepository.save(psi);
                }
            }
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cadastro concluído com sucesso! Usuário: " + codUsuario +
                        (pessoa != null ? " - " + pessoa.getNomePessoa() : ""));

        return "redirect:/cadastro-usuario";
    }
}
