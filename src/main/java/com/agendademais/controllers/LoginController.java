package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.PessoaInstituicaoRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.services.RecuperacaoLoginService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;
    private final RecuperacaoLoginService recuperacaoLoginService;

    public LoginController(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository,
            RecuperacaoLoginService recuperacaoLoginService) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
        this.recuperacaoLoginService = recuperacaoLoginService;
    }

    @GetMapping
    public String loginForm(Model model) {
/*     	List<Instituicao> lista = instituicaoRepository.findBySituacaoInstituicao("A");
    	     model.addAttribute("instituicoes", lista);
 */
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
        	    .toList();
        
        instituicoesVinculadasAtivas.forEach(inst -> 
        System.out.println("*** Instituição VÁLIDA: " + inst.getId() + " - " + inst.getNomeInstituicao()));

        
     	System.out.println("****************************************************************************");
     	System.out.println("*** LoginController.java /login/processarLogin usuario.getNivelAcessoUsuario()  = " + usuario.getNivelAcessoUsuario()); 
     	System.out.println("*** LoginController.java /login/processarLogin vinculosAtivos.size()            = " + vinculosAtivos.size()); 
     	System.out.println("*** LoginController.java /login/processarLogin temVinculos Pessoa               = " + temVinculos); 
     	System.out.println("*** LoginController.java /login/processarLogin vinculosAtivos.stream()          = " + vinculosAtivos.stream()); 
     	System.out.println("*** LoginController.java /login/processarLogin instituicoesVinculadasAtivas.size= " + instituicoesVinculadasAtivas.size()); 
     	System.out.println("*** LoginController.java /login/processarLogin instituicoesVinculadasAtivas     = " + instituicoesVinculadasAtivas); 
     	System.out.println("****************************************************************************");

     	// usando o método toString():
     	for (Instituicao inst : instituicoesVinculadasAtivas) {
     	    System.out.println("*** Instituição vinculada: " + inst);
     	}    
     	    
        System.out.println("****************************************************************************");
     	
        
        if (instituicoesVinculadasAtivas.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Seu acesso foi bloqueado ou cancelado. Consulte o administrador.");
            return "redirect:/login";
        }

        // SuperUsuário, sempre exibe a escolha mesmo com 1 vínculo
        if (usuario.getNivelAcessoUsuario() == 9) {
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("instituicoes", instituicoesVinculadasAtivas);
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            redirectAttributes.addFlashAttribute("senha", senha);
            redirectAttributes.addFlashAttribute("exibirControleTotal", true);
            redirectAttributes.addFlashAttribute("nivelAcesso", usuario.getNivelAcessoUsuario());
            return "redirect:/login";
        }
        
        // APENAS 1 VINCULO ATIVO 
        if (instituicoesVinculadasAtivas.size() == 1) {
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("instituicaoSelecionada", instituicoesVinculadasAtivas.get(0));
            return redirecionarPorNivel(usuario.getNivelAcessoUsuario());
        }

        // Mais de um vínculo ativo: exibir seleção - Lista
        redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
        redirectAttributes.addFlashAttribute("instituicoes", instituicoesVinculadasAtivas);
        redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
        redirectAttributes.addFlashAttribute("senha", senha);
        redirectAttributes.addFlashAttribute("nivelAcesso", usuario.getNivelAcessoUsuario());

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
     	System.out.println("*** LoginController.java /login/entrar         instituicao                    = " + instituicao); 
     	System.out.println("*** LoginController.java /login/entrar         usuario.getNivelAcessoUsuario()= " + usuario.getNivelAcessoUsuario()); 
    //	System.out.println("*** LoginController.java /login/entrar         vinculosAtivos.size()= " + vinculosAtivos.size()); 
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
        
        Optional<UsuarioInstituicao> vinculoOpt =
        	    usuarioInstituicaoRepository.findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao);

     	System.out.println("****************************************************************************");
        System.out.println("*** LoginController.java /login/entrar           vinculoOpt= " +
                usuarioInstituicaoRepository.findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao));
     	System.out.println("***  ");
     	System.out.println("****************************************************************************");
        
        if (vinculoOpt.isEmpty()
        	|| !"A".equals(vinculoOpt.get().getSitAcessoUsuarioInstituicao())
        	|| !"A".equals(vinculoOpt.get().getInstituicao().getSituacaoInstituicao())) {

            System.out.println("*** LoginController.java /login/entrar  Entrou => vinculoOpt Empty ou SitUsuarioInstitucao A ou Sit Institucao A");
        	
        	redirectAttributes.addFlashAttribute("mensagemErro",
        	    "Você não tem vínculo ativo com esta instituição.");
        	redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
        	redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
        	redirectAttributes.addFlashAttribute("senha", senha);
        	redirectAttributes.addFlashAttribute("instituicoes",
        	    usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(
        	            usuario.getId(), "A"
        	    ).stream()
        	     .filter(v -> v.getInstituicao() != null && "A".equals(v.getInstituicao().getSituacaoInstituicao()))
        	     .map(UsuarioInstituicao::getInstituicao)
        	     .toList());
        	
         	System.out.println("*** LoginController.java /login/entrar         instituicoes= " + 
            	    usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(
            	            usuario.getId(), "A"
            	    ).stream()
            	     .filter(v -> v.getInstituicao() != null && "A".equals(v.getInstituicao().getSituacaoInstituicao()))
            	     .map(UsuarioInstituicao::getInstituicao)
            	     .toList());

         			
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
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
    	
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
        
        // Chamar o serviço de envio de email como já feito no RecuperacaoLoginController
        recuperacaoLoginService.enviarLinkRecuperacao(email); 
        // model.addAttribute("mensagemSucesso", "Enviamos uma mensagem para o e-mail informado com as instruções para recuperar sua senha.");
        redirectAttributes.addFlashAttribute("mensagemErro",  
        		"Enviamos uma mensagem com as instruções para recuperar sua senha, para o e-mail: " + email + ".");
        return "redirect:/login/recuperar-login-email";

        
        // Redireciona para a tela de redefinir senha com o CodUsuario já preenchido
//        Usuario usuario = usuarioOpt.get();
//        redirectAttributes.addFlashAttribute("codUsuario", usuario.getCodUsuario());
//        return "redirect:/recuperar-senha";
    }
    
    @PostMapping("/confirmar")
    public String confirmarRecuperacao(@RequestParam String email,
                                       RedirectAttributes redirectAttributes) {

        List<Usuario> usuarios = usuarioRepository.findAllByPessoaEmailPessoa(email);

        if (usuarios.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Email não cadastrado.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        } else if (usuarios.size() > 1) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                "Mais de um cadastro com esse email. Entre em contato com o suporte.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        }

        // Envia o email com link e codUsuario
        recuperacaoLoginService.enviarLinkRecuperacao(email);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
            "Enviamos uma mensagem com as instruções para recuperar seu acesso para o email: " + email + ".");

        return "redirect:/login";
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
