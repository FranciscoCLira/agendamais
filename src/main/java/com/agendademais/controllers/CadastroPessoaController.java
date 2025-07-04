package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/cadastro-pessoa")
public class CadastroPessoaController {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final InstituicaoRepository instituicaoRepository;


    public CadastroPessoaController(
            UsuarioRepository usuarioRepository,
            PessoaRepository pessoaRepository,
            InstituicaoRepository instituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
    }

    @GetMapping
    public String mostrarFormularioCadastroPessoa(@RequestParam String codUsuario,
                                                  @RequestParam String senha,
                                                  Model model) {
        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("senha", senha);
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "cadastro-pessoa";
    }

    @Transactional
    @PostMapping
    public String processarCadastroPessoa(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam String nomePessoa,
            @RequestParam String emailPessoa,
            @RequestParam String celularPessoa,
            @RequestParam String nomePaisSelect,
            @RequestParam(required = false) String paisOutro,
            @RequestParam String nomeEstadoSelect,
            @RequestParam(required = false) String estadoOutro,
            @RequestParam String cidadeSelect,
            @RequestParam(required = false) String cidadeOutro,
            @RequestParam(required = false) String curriculoPessoal,
            @RequestParam(required = false) String comentarios,
            RedirectAttributes redirectAttributes,
            Model model) {

        String paisFinal = "Outro".equals(nomePaisSelect) ? paisOutro : nomePaisSelect;
        String estadoFinal = "Outro".equals(nomeEstadoSelect) ? estadoOutro : nomeEstadoSelect;
        String cidadeFinal = "Outro".equals(cidadeSelect) ? cidadeOutro : cidadeSelect;

        if (paisFinal == null || paisFinal.isBlank()) {
            model.addAttribute("mensagemErro", "Informe o País.");
            return "cadastro-pessoa";
        }
        if (estadoFinal == null || estadoFinal.isBlank()) {
            model.addAttribute("mensagemErro", "Informe o Estado.");
            return "cadastro-pessoa";
        }
        if (cidadeFinal == null || cidadeFinal.isBlank()) {
            model.addAttribute("mensagemErro", "Informe a Cidade.");
            return "cadastro-pessoa";
        }

        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            Usuario usuarioExistente = existente.get();
            if (usuarioExistente.getPessoa() == null) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Usuário já iniciado. Faça login para concluir seu cadastro.");
                return "redirect:/login";
            }
            model.addAttribute("mensagemErro", "Usuário já existente.");
            return "cadastro-pessoa";
        }

        Pessoa pessoa = new Pessoa();
        pessoa.setNomePessoa(nomePessoa);
        pessoa.setEmailPessoa(emailPessoa);
        pessoa.setCelularPessoa(celularPessoa);
        pessoa.setNomePaisPessoa(paisFinal);
        pessoa.setNomeEstadoPessoa(estadoFinal);
        pessoa.setNomeCidadePessoa(cidadeFinal);
        pessoa.setCurriculoPessoal(curriculoPessoal);
        pessoa.setComentarios(comentarios);
        pessoa.setSituacaoPessoa("A");
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa);

        Usuario usuario = new Usuario();
        usuario.setCodUsuario(codUsuario);
        usuario.setSenha(senha);
        usuario.setNivelAcessoUsuario(1);
        usuario.setPessoa(pessoa);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Informações salvas. Agora escolha suas instituições.");
        redirectAttributes.addFlashAttribute("codUsuario", codUsuario);

        return "redirect:/cadastro-relacionamentos?codUsuario=" + codUsuario;
    }
}
