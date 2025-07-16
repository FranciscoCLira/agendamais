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
            @RequestParam(required = false) String nomeEstadoSelect,
            @RequestParam(required = false) String estadoOutro,
            @RequestParam(required = false) String cidadeSelect,
            @RequestParam(required = false) String cidadeOutro,
            @RequestParam(required = false) String curriculoPessoal,
            @RequestParam(required = false) String comentarios,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {

    	String paisFinal = "Outro".equals(nomePaisSelect) ? paisOutro : nomePaisSelect;

    	// String estadoFinal = (nomeEstadoSelect == null || "Outro".equals(nomeEstadoSelect)) && estadoOutro != null
    	//        ? estadoOutro.trim()
    	//        : nomeEstadoSelect;
    	
    	String estadoFinal;
    	if ("Brasil".equals(nomePaisSelect)) {
    	    estadoFinal = nomeEstadoSelect;
    	} else {
    	    estadoFinal = estadoOutro;
    	}

//    	String cidadeFinal = (cidadeSelect == null || "Outro".equals(cidadeSelect)) && cidadeOutro != null
//    	        ? cidadeOutro.trim()
//    	        : cidadeSelect;

    	String cidadeFinal;
    	if ("Brasil".equals(nomePaisSelect)) {
    	    cidadeFinal = "Outro".equals(cidadeSelect) || cidadeSelect == null ? cidadeOutro : cidadeSelect;
    	} else {
    	    cidadeFinal = cidadeOutro;
    	}
    	
        
        if (paisFinal == null || paisFinal.isBlank()) {
            model.addAttribute("mensagemErro", "Informe o País.");
            preencherModelComDadosForm(model, codUsuario, senha, nomePessoa, emailPessoa, celularPessoa,
                    nomePaisSelect, paisOutro, nomeEstadoSelect, estadoOutro, cidadeSelect, cidadeOutro,
                    curriculoPessoal, comentarios);
            return "cadastro-pessoa";
        }

//      if (estadoFinal == null || estadoFinal.isBlank()) {
        if (estadoFinal == null || estadoFinal.trim().isEmpty()) {
            model.addAttribute("mensagemErro", "Informe o Estado.");
            preencherModelComDadosForm(model, codUsuario, senha, nomePessoa, emailPessoa, celularPessoa,
                    nomePaisSelect, paisOutro, nomeEstadoSelect, estadoOutro, cidadeSelect, cidadeOutro,
                    curriculoPessoal, comentarios);
            return "cadastro-pessoa";
        }
        
//      if (cidadeFinal == null || cidadeFinal.isBlank()) {
        if (cidadeFinal == null || cidadeFinal.trim().isEmpty()) {
            model.addAttribute("mensagemErro", "Informe a Cidade.");
            preencherModelComDadosForm(model, codUsuario, senha, nomePessoa, emailPessoa, celularPessoa,
                    nomePaisSelect, paisOutro, nomeEstadoSelect, estadoOutro, cidadeSelect, cidadeOutro,
                    curriculoPessoal, comentarios);
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
            preencherModelComDadosForm(model, codUsuario, senha, nomePessoa, emailPessoa, celularPessoa,
                    nomePaisSelect, paisOutro, nomeEstadoSelect, estadoOutro, cidadeSelect, cidadeOutro,
                    curriculoPessoal, comentarios);
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

            preencherModelComDadosForm(model, codUsuario, senha, nomePessoa, emailPessoa, celularPessoa,
                    nomePaisSelect, paisOutro, nomeEstadoSelect, estadoOutro, cidadeSelect, cidadeOutro,
                    curriculoPessoal, comentarios);            
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
    
    private void preencherModelComDadosForm(Model model,
            String codUsuario,
            String senha,
            String nomePessoa,
            String emailPessoa,
            String celularPessoa,
            String nomePaisSelect,
            String paisOutro,
            String nomeEstadoSelect,
            String estadoOutro,
            String cidadeSelect,
            String cidadeOutro,
            String curriculoPessoal,
            String comentarios) {
		model.addAttribute("codUsuario", codUsuario);
		model.addAttribute("senha", senha);
		model.addAttribute("nomePessoa", nomePessoa);
		model.addAttribute("emailPessoa", emailPessoa);
		model.addAttribute("celularPessoa", celularPessoa);
		model.addAttribute("nomePaisSelect", nomePaisSelect);
		model.addAttribute("paisOutro", paisOutro);
		model.addAttribute("nomeEstadoSelect", nomeEstadoSelect);
		model.addAttribute("estadoOutro", estadoOutro);
		model.addAttribute("cidadeSelect", cidadeSelect);
		model.addAttribute("cidadeOutro", cidadeOutro);
		model.addAttribute("curriculoPessoal", curriculoPessoal);
		model.addAttribute("comentarios", comentarios);
     }
}
