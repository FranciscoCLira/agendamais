package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class CadastroPessoaController {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public CadastroPessoaController(UsuarioRepository usuarioRepository,
                                    PessoaRepository pessoaRepository,
                                    InstituicaoRepository instituicaoRepository,
                                    UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @GetMapping("/cadastro-pessoa")
    public String mostrarFormularioCadastroPessoa(@RequestParam String codUsuario,
                                                  @RequestParam String senha,
                                                  Model model) {
        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("senha", senha);
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "cadastro-pessoa";
    }

    @PostMapping("/cadastro-pessoa")
    public String processarCadastroPessoa(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam String nomePessoa,
            @RequestParam String emailPessoa,
            @RequestParam String celularPessoa,
            @RequestParam(required = false) String paisOutro,
            @RequestParam(required = false) String estadoOutro,
            @RequestParam String cidadeSelect,
            @RequestParam(required = false) String cidadeOutro,
            @RequestParam String nomePaisSelect,
            @RequestParam String nomeEstadoSelect,
            @RequestParam(required = false) String curriculoPessoal,
            @RequestParam(required = false) String comentarios,
            @RequestParam List<Long> idInstituicoes,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Tratamento de Estado
        String estadoFinal;
        if ("Brasil".equals(nomePaisSelect)) {
            estadoFinal = nomeEstadoSelect;
        } else if ("Outro".equals(nomePaisSelect)) {
            estadoFinal = estadoOutro;
        } else {
            estadoFinal = estadoOutro;
        }

        if (estadoFinal == null || estadoFinal.isEmpty()) {
            model.addAttribute("mensagemErro", "Por favor, informe o Estado.");
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("senha", senha);
            model.addAttribute("nomePessoa", nomePessoa);
            model.addAttribute("emailPessoa", emailPessoa);
            model.addAttribute("celularPessoa", celularPessoa);
            model.addAttribute("curriculoPessoal", curriculoPessoal);
            model.addAttribute("comentarios", comentarios);
            model.addAttribute("instituicoes", instituicaoRepository.findAll());
            return "cadastro-pessoa";
        }

        // Tratamento de Cidade
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
            model.addAttribute("instituicoes", instituicaoRepository.findAll());
            return "cadastro-pessoa";
        }

        // Verifica se o usuário já existe
        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            model.addAttribute("mensagemErro", "Usuário já existente, informe um novo Código de Usuário.");
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("senha", senha);
            model.addAttribute("nomePessoa", nomePessoa);
            model.addAttribute("emailPessoa", emailPessoa);
            model.addAttribute("celularPessoa", celularPessoa);
            model.addAttribute("curriculoPessoal", curriculoPessoal);
            model.addAttribute("comentarios", comentarios);
            model.addAttribute("instituicoes", instituicaoRepository.findAll());
            return "redirect:/cadastro-usuario";
        }

        // País
        String paisFinal;
        if ("Outro".equals(nomePaisSelect)) {
            paisFinal = paisOutro;
        } else {
            paisFinal = nomePaisSelect;
        }

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

        // Relacionamentos
        for (Long instId : idInstituicoes) {
            Instituicao inst = instituicaoRepository.findById(instId).orElse(null);
            if (inst != null) {
                UsuarioInstituicao ui = new UsuarioInstituicao();
                ui.setUsuario(usuario);
                ui.setInstituicao(inst);
                ui.setSitAcessoUsuarioInstituicao("A");
                usuarioInstituicaoRepository.save(ui);
            }
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cadastro Concluído com Sucesso! Usuário: " + codUsuario + " - " + nomePessoa);
        return "redirect:/cadastro-usuario";
    }

}