package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class CadastroController {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public CadastroController(UsuarioRepository usuarioRepository,
                              PessoaRepository pessoaRepository,
                              InstituicaoRepository instituicaoRepository,
                              UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        List<Instituicao> instituicoes = instituicaoRepository.findAll();
        model.addAttribute("instituicoes", instituicoes);
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(@RequestParam String nomePessoa,
                                    @RequestParam String emailPessoa,
                                    @RequestParam String codUsuario,
                                    @RequestParam String senha,
                                    @RequestParam List<Long> idInstituicoes,
                                    Model model) {

        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            model.addAttribute("erro", "Já existe um usuário com esse código. Tente outro.");
            List<Instituicao> instituicoes = instituicaoRepository.findAll();
            model.addAttribute("instituicoes", instituicoes);
            return "cadastro";
        }

        Usuario usuario = new Usuario();
        usuario.setCodUsuario(codUsuario);
        usuario.setSenha(senha);
        usuario.setNivelAcessoUsuario(1); // Participante
        usuarioRepository.save(usuario);

        Pessoa pessoa = new Pessoa();
        pessoa.setNomePessoa(nomePessoa);
        pessoa.setEmailPessoa(emailPessoa);
        pessoa.setSituacaoPessoa("A");
        pessoa.setCodUsuario(usuario);
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa);

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

        return "redirect:/";
    }
}
