package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.PessoaInstituicaoRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public LoginController(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @GetMapping
    public String loginForm(Model model) {
        model.addAttribute("instituicoes", instituicaoRepository.findBySituacaoInstituicao("A"));
        return "login";
    }


    @PostMapping
    public String processarLogin(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha inválida.");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            return "redirect:/login";
        }

        if (usuario.getSituacaoUsuario() != null && usuario.getSituacaoUsuario().equals("B")) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário bloqueado. Consulte o administrador.");
            return "redirect:/login";
        }

        boolean temVinculos = usuario.getPessoa() != null &&
                pessoaInstituicaoRepository.existsByPessoaId(usuario.getPessoa().getId());

        if (!temVinculos) {
            session.setAttribute("usuarioPendencia", usuario);
            return "redirect:/cadastro-relacionamentos";
        }

        List<UsuarioInstituicao> vinculosAtivos =
        	    usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(usuario.getId(), "A").stream()
        	    .filter(v -> v.getInstituicao() != null && "A".equals(v.getInstituicao().getSituacaoInstituicao()))
        	    .toList();

        
        List<Instituicao> instituicoesVinculadasAtivas = vinculosAtivos.stream()
        	    .map(UsuarioInstituicao::getInstituicao)
        	    .filter(inst -> "A".equalsIgnoreCase(inst.getSituacaoInstituicao()))
        	    .collect(Collectors.toList());
        
     	System.out.println("****************************************************************************");
     	System.out.println("*** LoginController.java /login  usuario.getNivelAcessoUsuario()= " + usuario.getNivelAcessoUsuario()); 
     	System.out.println("*** LoginController.java /login  vinculosAtivos.size()= " + vinculosAtivos.size()); 
     	System.out.println("****************************************************************************");
        
        if (instituicoesVinculadasAtivas.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Seu acesso foi bloqueado ou cancelado. Consulte o administrador.");
            return "redirect:/login";
        }

        // Se for SuperUsuário, sempre exibe a escolha mesmo com 1 vínculo
        if (usuario.getNivelAcessoUsuario() == 9) {
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("instituicoes", 
            		instituicoesVinculadasAtivas.stream().toList());
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            redirectAttributes.addFlashAttribute("senha", senha);
            redirectAttributes.addFlashAttribute("exibirControleTotal", true);
            redirectAttributes.addFlashAttribute("nivelAcesso", usuario.getNivelAcessoUsuario());
            return "redirect:/login";
        }

        if (vinculosAtivos.size() == 1) {
            UsuarioInstituicao vinculo = vinculosAtivos.get(0);
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("instituicaoSelecionada", vinculo.getInstituicao());
            return redirecionarPorNivel(usuario.getNivelAcessoUsuario());
        }

        // Exibe lista de instituições para escolha
        redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
        redirectAttributes.addFlashAttribute("instituicoes", vinculosAtivos.stream()
                .map(UsuarioInstituicao::getInstituicao).toList());
        redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
        redirectAttributes.addFlashAttribute("senha", senha);
        redirectAttributes.addFlashAttribute("nivelAcesso", usuario.getNivelAcessoUsuario());
        
        List<Instituicao> instituicoesDoVinculo = vinculosAtivos.stream()
                .map(UsuarioInstituicao::getInstituicao)
                .toList();

        redirectAttributes.addFlashAttribute("instituicoes", instituicoesDoVinculo);

        return "redirect:/login";
    }

    @PostMapping("/entrar")
    public String processarEscolhaInstituicao(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam Long instituicao,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha inválida.");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            return "redirect:/login";
        }
        
     	System.out.println("****************************************************************************");
     	System.out.println("*** LoginController.java /login/entrar  usuario.getNivelAcessoUsuario()= " + usuario.getNivelAcessoUsuario()); 
     	System.out.println("*** LoginController.java /login/entrar  instituicao= " + instituicao); 
     	System.out.println("****************************************************************************");
        
        // ACESSO AO CONTROLE TOTAL (OPCAO VALOR 0)
        if (instituicao == 0 && usuario.getNivelAcessoUsuario() == 9) {
            session.setAttribute("usuarioLogado", usuario);
            session.removeAttribute("instituicaoSelecionada");
            return "redirect:/menus/menu-controle-total";
        }
        
        if (instituicao == 0 && usuario.getNivelAcessoUsuario() != 9) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Acesso ao Controle Total não permitido.");
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            redirectAttributes.addFlashAttribute("senha", senha);
            redirectAttributes.addFlashAttribute("nivelAcesso", usuario.getNivelAcessoUsuario());

            redirectAttributes.addFlashAttribute("instituicoes",
                usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(usuario.getId(), "A")
                    .stream().map(UsuarioInstituicao::getInstituicao).toList());

            return "redirect:/login";
        }

        // Valida vínculo com a instituição
        boolean vinculoAtivo = usuarioInstituicaoRepository
                .existsByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao);

        if (!vinculoAtivo) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Você não tem vínculo ativo com esta instituição.");
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            redirectAttributes.addFlashAttribute("senha", senha);
            redirectAttributes.addFlashAttribute("instituicoes",
                    usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(
                            usuario.getId(), "A"
                    ).stream().map(UsuarioInstituicao::getInstituicao).toList());
            return "redirect:/login";
        }

        // Acesso normal via instituição
        session.setAttribute("usuarioLogado", usuario);
        session.setAttribute("instituicaoSelecionada",
                instituicaoRepository.findById(instituicao).orElse(null));

        if (usuario.getNivelAcessoUsuario() == 9) {
            return "redirect:/menus/menu-superusuario";

        }

        return redirecionarPorNivel(usuario.getNivelAcessoUsuario());
    }

//    @GetMapping("/recuperar-login-email")
//    public String exibirFormularioRecuperacaoEmail(Model model) {
//        model.addAttribute("email", "");
//        model.addAttribute("mensagemErro", null);
//        return "recuperar-login-email"; // view com input de e-mail
//    }

      // exibe a tela 
      @GetMapping("/recuperar-login-email")
      public String mostrarFormRecuperarLoginPorEmail(Model model) {
    	  // Limpa campos da tela com model.addAttribute
          //   model.addAttribute("email", "");
          //   model.addAttribute("mensagemErro", null);
          // Apenas retorna a view, atributos são carregados automaticamente
          return "recuperar-login-email";
      }
    
    
    @PostMapping("/recuperar-login-email")
    public String recuperarLoginPorEmail(@RequestParam String email, 
                                         RedirectAttributes redirectAttributes) {
    	
    	List<Usuario> usuarios = usuarioRepository.findAllByPessoaEmailPessoa(email);

    	if (usuarios.isEmpty()) {
    	    redirectAttributes.addFlashAttribute("mensagemErro", "Email não cadastrado.");
    	    redirectAttributes.addFlashAttribute("email", email);
    	    return "redirect:/login/recuperar-login-email";
    	} else if (usuarios.size() > 1) {
    	    redirectAttributes.addFlashAttribute("mensagemErro",
    	        "Mais de um cadastro com esse email. Entre em contato com o suporte.");
    	    redirectAttributes.addFlashAttribute("email", email);
    	    return "redirect:/login/recuperar-login-email";
    	}
    	
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Email não cadastrado.");
            redirectAttributes.addFlashAttribute("email", email); // mantém o valor
            return "redirect:/login/recuperar-login-email";
        }

        // Redireciona para a tela de redefinir senha com o CodUsuario já preenchido
        Usuario usuario = usuarioOpt.get();
        redirectAttributes.addFlashAttribute("codUsuario", usuario.getCodUsuario());
        return "redirect:/recuperar-senha";
    }
    
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private String redirecionarPorNivel(int nivel) {
        return switch (nivel) {
	    //  case 1 -> "redirect:/participante-form";
	    //  case 2 -> "redirect:/autor-form";
	    //  case 5 -> "redirect:/administrador-form";
	    //  default -> "redirect:/participante-form";
        
            case 1 -> "redirect:/menus/menu-participante";
            case 2 -> "redirect:/menus/menu-autor";
            case 5 -> "redirect:/menus/menu-administrador";
            default -> "redirect:/menus/menu-participante";
        };
    }
}
