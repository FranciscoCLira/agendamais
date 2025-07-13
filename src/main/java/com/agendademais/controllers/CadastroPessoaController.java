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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cadastro-pessoa")
public class CadastroPessoaController {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    // private final InstituicaoRepository instituicaoRepository;


    public CadastroPessoaController(
            UsuarioRepository usuarioRepository,
            PessoaRepository pessoaRepository,
         //   InstituicaoRepository instituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        // this.instituicaoRepository = instituicaoRepository;
    }

    @GetMapping
    public String mostrarFormulario(Model model,
                  @RequestParam(required = false) String codUsuario,
                  @RequestParam(required = false) String senha) {

        if (!model.containsAttribute("pessoa")) {
             model.addAttribute("pessoa", new Pessoa());
        }

        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("senha", senha);
        return "cadastro-pessoa";
    }
    @Transactional
    @PostMapping
    public String processarCadastroPessoa(
    		@ModelAttribute Pessoa pessoa,
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
            Model model,
            HttpSession session) {

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
        
        List<Pessoa> existentes = pessoaRepository.findAllByEmailPessoa(emailPessoa);

        if (!existentes.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                "Este Email já possui cadastro. <a href='/login/recuperar-login-email'>Quer recuperá-lo?</a>");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            redirectAttributes.addFlashAttribute("senha", senha);
            redirectAttributes.addFlashAttribute("pessoa", pessoa); // Mantém os dados preenchidos
            
//          System.out.println("*** CadastroPessoaController.java /cadastro-pessoa  =" + "Erro inesperado ao processar o cadastro."); 
//         	System.out.println("****************************************************************************");

            return "redirect:/cadastro-pessoa";
        }
        
        if (curriculoPessoal != null && curriculoPessoal.isBlank()) {
            curriculoPessoal = null;
        }
        if (comentarios != null && comentarios.isBlank()) {
            comentarios = null;
        }

        // Usa o objeto já preenchido e completa com dados adicionais
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

        // Cria e salva o usuário 
        Usuario usuario = new Usuario();
        usuario.setCodUsuario(codUsuario);
        usuario.setSenha(senha);
        usuario.setNivelAcessoUsuario(1);
        usuario.setSituacaoUsuario("A");
        usuario.setDataUltimaAtualizacao(LocalDate.now());
        usuario.setPessoa(pessoa);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Informações salvas. Agora escolha suas instituições.");
        // redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
        
        
        // SALVA NA SESSÃO
        session.setAttribute("usuarioPendencia", usuario);

        return "redirect:/cadastro-relacionamentos?codUsuario=" + codUsuario;
    }
}
