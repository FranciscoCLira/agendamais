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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/acesso")
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;
    private final RecuperacaoLoginService recuperacaoLoginService;
    private final PasswordEncoder passwordEncoder;

    public LoginController(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository,
            RecuperacaoLoginService recuperacaoLoginService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
        this.recuperacaoLoginService = recuperacaoLoginService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String LoginForm(Model model) {

        System.out.println("****************************************************************************");
        System.out.println("***    LoginController.java GET /acesso/LoginForm - return 'login'    ");
        System.out.println("*** ");

        return "login"; // deve existir login.html em /templates
    }

    @PostMapping
    public String processarLogin(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        System.out.println("****************************************************************************");
        System.out.println("*** 1. LoginController.java POST /acesso/processarLogin          ");
        System.out.println("*** ");

        // Permitir login por username OU email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmailPessoa(username);
        }
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário ou email não encontrado.");
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/acesso";
        }
        Usuario usuario = usuarioOpt.get();

        // Sistema compatível: testa BCrypt primeiro, depois texto plano
        boolean senhaValida = false;

        // Tenta BCrypt primeiro (senhas criptografadas)
        if (usuario.getPassword().startsWith("$2a$") || usuario.getPassword().startsWith("$2b$")) {
            senhaValida = passwordEncoder.matches(password, usuario.getPassword());
        } else {
            // Sistema legado: comparação direta (senhas antigas em texto plano)
            senhaValida = usuario.getPassword().equals(password);
        }

        if (!senhaValida) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha inválida.");
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/acesso";
        }

        // Se senha padrão de carga massiva, redireciona para alterar senha
        String user = usuario.getUsername();
        if ((user != null && user.matches("^[XU][0-9]{5}$")) &&
                (password.equals(user + "$") || password.equals(user + "%"))) {
            // Redireciona para alterar senha
            redirectAttributes.addFlashAttribute("username", user);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Por favor, altere sua senha inicial.");
            return "redirect:/alterar-senha";
        }

        if (usuario.getSituacaoUsuario() != null && usuario.getSituacaoUsuario().equals("B")) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário bloqueado. Consulte o administrador.");
            return "redirect:/acesso";
        }

        boolean temVinculos = usuario.getPessoa() != null &&
                pessoaInstituicaoRepository.existsByPessoaId(usuario.getPessoa().getId());

        if (!temVinculos) {
            session.setAttribute("usuarioPendencia", usuario);

            System.out.println("*** ");
            System.out.println("*** 2. LoginController.java POST /acesso  processarLogin  temVinculos= " + temVinculos);
            // System.out.println("*** 2. LoginController.java POST /acesso processarLogin
            // redirect:/cadastro-relacionamentos");
            System.out.println("****************************************************************************");

            return "redirect:/cadastro-relacionamentos";
        }

        List<UsuarioInstituicao> vinculosAtivos = usuarioInstituicaoRepository
                .findByUsuarioIdAndSitAcessoUsuarioInstituicao(usuario.getId(), "A").stream()
                .filter(v -> {
                    if (v.getInstituicao() == null)
                        return false;
                    String sit = v.getInstituicao().getSituacaoInstituicao();
                    int nivel = v.getNivelAcessoUsuarioInstituicao();
                    // Permite acesso se instituição Ativa, Bloqueada (admin+), ou Inativa (apenas
                    // superuser)
                    if ("A".equals(sit))
                        return true;
                    if ("B".equals(sit) && nivel >= 5)
                        return true;
                    if ("I".equals(sit) && nivel == 9)
                        return true;
                    return false;
                })
                .toList();

        List<Instituicao> instituicoesVinculadasAtivas = vinculosAtivos.stream()
                .map(UsuarioInstituicao::getInstituicao)
                .toList();

        System.out.println("*** ");
        instituicoesVinculadasAtivas
                .forEach(inst -> System.out.println("*** 3. LoginController.java POST - Instituição VÁLIDA: "
                        + inst.getId() + " - " + inst.getNomeInstituicao()));
        System.out.println("*** ");

        // REMOVIDO: usuario.getNivelAcessoUsuario() - agora está em UsuarioInstituicao
        // System.out.println("*** 4. LoginController.java POST /acesso/processarLogin
        // usuario.getNivelAcessoUsuario() = "
        // + usuario.getNivelAcessoUsuario());
        System.out.println("*** 4. LoginController.java POST /acesso/processarLogin vinculosAtivos.size()            = "
                + vinculosAtivos.size());
        System.out.println("*** 4. LoginController.java POST /acesso/processarLogin temVinculos Pessoa               = "
                + temVinculos);
        // System.out.println("*** 4. LoginController.java POST /acesso/processarLogin
        // vinculosAtivos.stream() = " + vinculosAtivos.stream());
        // System.out.println("*** 4. LoginController.java POST /acesso/processarLogin
        // instituicoesVinculadasAtivas.size= " + instituicoesVinculadasAtivas.size());
        // System.out.println("*** 4. LoginController.java POST /acesso/processarLogin
        // instituicoesVinculadasAtivas = " + instituicoesVinculadasAtivas);
        System.out.println("*** ");

        // TESTE: usando o método toString():
        for (Instituicao inst : instituicoesVinculadasAtivas) {
            System.out.println("*** 5. LoginController.java POST - Instituição vinculada: " + inst);
        }

        if (instituicoesVinculadasAtivas.isEmpty()) {
            // Busca todos os vínculos para mostrar mensagem adequada
            List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository.findByUsuarioId(usuario.getId());
            String mensagem = "Seu acesso foi bloqueado ou cancelado. Consulte o administrador.";
            for (UsuarioInstituicao v : vinculos) {
                Instituicao inst = v.getInstituicao();
                if (inst != null) {
                    String sit = inst.getSituacaoInstituicao();
                    int nivel = v.getNivelAcessoUsuarioInstituicao();
                    if ((nivel < 5 && !"A".equals(sit)) || (nivel == 5 && "I".equals(sit))) {
                        mensagem = inst.getNomeInstituicao()
                                + " encontra-se no momento bloqueado ou inativo, consulte o administrador.";
                        break;
                    }
                }
            }
            redirectAttributes.addFlashAttribute("mensagemErro", mensagem);
            return "redirect:/acesso";
        }

        // Verificar se é SuperUsuário (nível 9) em alguma instituição
        boolean ehSuperUsuario = vinculosAtivos.stream()
                .anyMatch(v -> v.getNivelAcessoUsuarioInstituicao() == 9);

        if (ehSuperUsuario) {
            // Para superusuário, mostrar todas as instituições vinculadas, independente do
            // status
            List<UsuarioInstituicao> todosVinculos = usuarioInstituicaoRepository.findByUsuarioId(usuario.getId());
            List<Instituicao> todasInstituicoes = todosVinculos.stream()
                    .map(UsuarioInstituicao::getInstituicao)
                    .toList();
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("instituicoes", todasInstituicoes);
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("password", password);
            redirectAttributes.addFlashAttribute("exibirControleTotal", true);
            redirectAttributes.addFlashAttribute("nivelAcesso", 9); // SuperUsuário
            return "redirect:/acesso";
        }

        // APENAS 1 VINCULO ATIVO
        if (instituicoesVinculadasAtivas.size() == 1) {
            UsuarioInstituicao vinculoUnico = vinculosAtivos.get(0);
            int nivelAcessoUnico = vinculoUnico.getNivelAcessoUsuarioInstituicao();

            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("instituicaoSelecionada", instituicoesVinculadasAtivas.get(0));
            session.setAttribute("nivelAcessoAtual", nivelAcessoUnico);

            return redirecionarPorNivel(nivelAcessoUnico);
        }

        // Mais de um vínculo ativo: exibir seleção - Lista
        redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
        redirectAttributes.addFlashAttribute("instituicoes", instituicoesVinculadasAtivas);
        redirectAttributes.addFlashAttribute("username", username);
        redirectAttributes.addFlashAttribute("password", password);
        // Nível será determinado após escolha da instituição
        redirectAttributes.addFlashAttribute("nivelAcesso", "multiplos");

        System.out.println(
                "*** 5. LoginController.java POST /acesso  processarLogin - Mais de um vínculo ativo: exibir seleção - Lista ");
        System.out.println("*** 5. LoginController.java POST /acesso  processarLogin - return redirect:/acesso");
        System.out.println("****************************************************************************");

        return "redirect:/acesso";
    }

    @PostMapping("/entrar")
    public String processarEscolhaInstituicao(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam Long instituicao,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        System.out.println("*** ");
        System.out.println("*** A. LoginController.java POST /acesso/entrar           ");
        System.out.println("****************************************************************************");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/acesso";
        }

        Usuario usuario = usuarioOpt.get();

        boolean senhaValida = false;
        if (usuario.getPassword().startsWith("$2a$") || usuario.getPassword().startsWith("$2b$")) {
            senhaValida = passwordEncoder.matches(password, usuario.getPassword());
        } else {
            senhaValida = usuario.getPassword().equals(password);
        }
        if (!senhaValida) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha invalida.");
            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/acesso";
        }

        // System.out.println("****************************************************************************");
        // System.out.println("*** B.LoginController.java POST /acesso/entrar
        // instituicao = " + instituicao);
        // System.out.println("*** B.LoginController.java POST /acesso/entrar
        // usuario.getNivelAcessoUsuario()= " + usuario.getNivelAcessoUsuario());
        // System.out.println("****************************************************************************");

        // ACESSO AO CONTROLE TOTAL (OPCAO VALOR 0)
        // Verificar se usuário tem nível 9 (SuperUsuário) em alguma instituição
        boolean ehSuperUsuarioGlobal = usuarioInstituicaoRepository.findByUsuario(usuario).stream()
                .anyMatch(v -> v.getNivelAcessoUsuarioInstituicao() == 9
                        && "A".equals(v.getSitAcessoUsuarioInstituicao()));

        if (instituicao == 0 && ehSuperUsuarioGlobal) {
            session.setAttribute("usuarioLogado", usuario);
            session.removeAttribute("instituicaoSelecionada");
            session.setAttribute("nivelAcessoAtual", 0); // Controle Total
            return "redirect:/controle-total";
        }

        if (instituicao == 0 && !ehSuperUsuarioGlobal) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Acesso ao Controle Total não permitido.");
            redirectAttributes.addFlashAttribute("exibirInstituicoes", true);
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("password", password);
            redirectAttributes.addFlashAttribute("nivelAcesso", "negado");

            redirectAttributes.addFlashAttribute("instituicoes",
                    usuarioInstituicaoRepository.findByUsuarioIdAndSitAcessoUsuarioInstituicao(usuario.getId(), "A")
                            .stream().map(UsuarioInstituicao::getInstituicao).toList());

            return "redirect:/acesso";
        }

        // Valida vínculo com a instituição

        Optional<UsuarioInstituicao> vinculoOpt = usuarioInstituicaoRepository
                .findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao);

        if (vinculoOpt.isEmpty() ||
                !"A".equals(vinculoOpt.get().getSitAcessoUsuarioInstituicao()) ||
                !("A".equals(vinculoOpt.get().getInstituicao().getSituacaoInstituicao()) ||
                        ("B".equals(vinculoOpt.get().getInstituicao().getSituacaoInstituicao()) &&
                                vinculoOpt.get().getNivelAcessoUsuarioInstituicao() >= 5)
                        ||
                        ("I".equals(vinculoOpt.get().getInstituicao().getSituacaoInstituicao()) &&
                                vinculoOpt.get().getNivelAcessoUsuarioInstituicao() == 9))) {
            // Só permite acesso se instituição Ativa, Bloqueada (admin+), ou Inativa
            // (apenas superuser)
            session.setAttribute("usuarioPendencia", usuario);
            return "redirect:/cadastro-relacionamentos";
        }

        // Acesso normal via instituição
        UsuarioInstituicao vinculoSelecionado = vinculoOpt.get();
        session.setAttribute("usuarioLogado", usuario);
        session.setAttribute("instituicaoSelecionada",
                instituicaoRepository.findById(instituicao).orElse(null));

        // NOVO: Armazenar o nível de acesso da instituição selecionada
        int nivelAcessoAtual = vinculoSelecionado.getNivelAcessoUsuarioInstituicao();
        session.setAttribute("nivelAcessoAtual", nivelAcessoAtual);

        if (nivelAcessoAtual == 9) {
            return "redirect:/superusuario";
        }

        return redirecionarPorNivel(nivelAcessoAtual);
    }

    // exibe a tela
    @GetMapping("/recuperar-login-email")
    public String mostrarFormRecuperarLoginPorEmail(Model model) {
        // Limpa campos da tela com model.addAttribute
        // model.addAttribute("email", "");
        // model.addAttribute("mensagemErro", null);
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
            return "redirect:/acesso/recuperar-login-email";
        } else if (usuarios.size() > 1) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Mais de um cadastro com esse email. Entre em contato com o suporte.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/acesso/recuperar-login-email";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Email não cadastrado.");
            redirectAttributes.addFlashAttribute("email", email); // mantém o valor
            return "redirect:/acesso/recuperar-login-email";
        }

        // Chamar o serviço de envio de email como já feito no
        // RecuperacaoLoginController
        recuperacaoLoginService.enviarLinkRecuperacao(email);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Enviamos uma mensagem com as instruções para recuperar sua Senha, para o e-mail: " + email + ".");
        return "redirect:/acesso/recuperar-login-email";
    }

    @PostMapping("/confirmar")
    public String confirmarRecuperacao(@RequestParam String email,
            RedirectAttributes redirectAttributes) {

        List<Usuario> usuarios = usuarioRepository.findAllByPessoaEmailPessoa(email);

        if (usuarios.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Email não cadastrado.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/acesso";
        } else if (usuarios.size() > 1) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Mais de um cadastro com esse email. Entre em contato com o suporte.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/acesso";
        }

        // Envia o email com link e username
        recuperacaoLoginService.enviarLinkRecuperacao(email);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Enviamos uma mensagem com as instruções para recuperar sua Senha, para o e-mail: " + email + ".");

        return "redirect:/acesso";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/acesso";
    }

    private String redirecionarPorNivel(int nivel) {
        return switch (nivel) {
            case 1 -> "redirect:/participante";
            case 2 -> "redirect:/autor";
            case 5 -> "redirect:/administrador";
            case 9 -> "redirect:/superusuario";
            default -> "redirect:/participante";
        };
    }
}
