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
    private final SubInstituicaoRepository subInstituicaoRepository;

    public CadastroPessoaController(
            UsuarioRepository usuarioRepository,
            PessoaRepository pessoaRepository,
            InstituicaoRepository instituicaoRepository,
            SubInstituicaoRepository subInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
    }

    @GetMapping
    public String mostrarFormulario(@RequestParam String codUsuario,
                                    @RequestParam String senha,
                                    Model model) {
        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("senha", senha);
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subinstituicoes", subInstituicaoRepository.findAll());
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
            @RequestParam(required = false) String curriculoPessoal,
            @RequestParam(required = false) String comentarios,
            @RequestParam String nomePaisSelect,
            @RequestParam(required = false) String paisOutro,
            @RequestParam String nomeEstadoSelect,
            @RequestParam(required = false) String estadoOutro,
            @RequestParam String cidadeSelect,
            @RequestParam(required = false) String cidadeOutro,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validação de Estado
        String estadoFinal = ("Outro".equals(nomeEstadoSelect)) ? estadoOutro : nomeEstadoSelect;
        if (estadoFinal == null || estadoFinal.isEmpty()) {
            model.addAttribute("mensagemErro", "Por favor, informe o Estado.");
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("senha", senha);
            model.addAttribute("nomePessoa", nomePessoa);
            model.addAttribute("emailPessoa", emailPessoa);
            model.addAttribute("celularPessoa", celularPessoa);
            model.addAttribute("curriculoPessoal", curriculoPessoal);
            model.addAttribute("comentarios", comentarios);
            return "cadastro-pessoa";
        }

        // Validação de Cidade
        String cidadeFinal = ("Outro".equals(cidadeSelect)) ? cidadeOutro : cidadeSelect;
        if (cidadeFinal == null || cidadeFinal.isEmpty()) {
            model.addAttribute("mensagemErro", "Por favor, informe a Cidade.");
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("senha", senha);
            model.addAttribute("nomePessoa", nomePessoa);
            model.addAttribute("emailPessoa", emailPessoa);
            model.addAttribute("celularPessoa", celularPessoa);
            model.addAttribute("curriculoPessoal", curriculoPessoal);
            model.addAttribute("comentarios", comentarios);
            return "cadastro-pessoa";
        }

        // Verifica se o usuário já existe
        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário já existente, informe um novo Código de Usuário.");
            return "redirect:/cadastro-usuario";
        }

        // País
        String paisFinal = ("Outro".equals(nomePaisSelect)) ? paisOutro : nomePaisSelect;

        // Salva Pessoa
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

        // Salva Usuario
        Usuario usuario = new Usuario();
        usuario.setCodUsuario(codUsuario);
        usuario.setSenha(senha);
        usuario.setNivelAcessoUsuario(1);
        usuario.setPessoa(pessoa);
        usuarioRepository.save(usuario);

        // Redireciona para a ETAPA FINAL de relacionamento
        return "redirect:/cadastro-relacionamentos?codUsuario=" + codUsuario;
    }
}
