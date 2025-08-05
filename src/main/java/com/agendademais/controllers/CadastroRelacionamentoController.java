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
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cadastro-relacionamentos")
public class CadastroRelacionamentoController {

	private final UsuarioRepository usuarioRepository;
	private final PessoaRepository pessoaRepository;	
    private final InstituicaoRepository instituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public CadastroRelacionamentoController(
    	    UsuarioRepository usuarioRepository,
    	    PessoaRepository pessoaRepository,
            InstituicaoRepository instituicaoRepository,
            SubInstituicaoRepository subInstituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository,
            PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository,
            UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
    	this.usuarioRepository = usuarioRepository;
    	this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.pessoaSubInstituicaoRepository = pessoaSubInstituicaoRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @GetMapping
    public String mostrarFormulario(@RequestParam(required = false) String username,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
    	
        Usuario usuario = (Usuario) session.getAttribute("usuarioPendencia");
        
//     	System.out.println("****************************************************************************");
//     	System.out.println("*** CadastroRelacionamentoController.java /cadastro-relacionamentos  usuario=" + usuario ); 
//     	System.out.println("****************************************************************************");
    	
        // Tentar recuperar via username se não estiver na session
        if (usuario == null && username != null) {
            Optional<Usuario> existente = usuarioRepository.findByUsername(username);
            if (existente.isPresent()) {
                usuario = existente.get();
                session.setAttribute("usuarioPendencia", usuario);
            }
        }
        
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro não previsto. Consulte o gestor do sistema.");
        	return "redirect:/acesso";
        }

        model.addAttribute("username", usuario.getUsername());
        model.addAttribute("nomeUsuario",
                usuario.getPessoa() != null ? usuario.getPessoa().getNomePessoa() : "");
        
        // Listar somente instituições e subinstituições ativas
        model.addAttribute("instituicoes", instituicaoRepository.findBySituacaoInstituicao("A"));
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findBySituacaoSubInstituicao("A"));

        return "cadastro-relacionamentos";
    }

    @Transactional
    @PostMapping
    public String processarCadastroRelacionamentos(
            @RequestParam Map<String, String> allParams,
            @RequestParam(name = "instituicoesSelecionadas", required = false)
            String[] instituicoesSelecionadas,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
    	
//    	System.out.println("=== CadastroRelacionamentoController - processarCadastroRelacionamentos  ====");
//    	System.out.println("======== DADOS ENVIADOS ========");
//    	allParams.forEach((k, v) -> System.out.println(k + ": " + v));
//    	System.out.println("====================================================================");

        String username = allParams.get("username");
        if (username == null || username.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Código de usuário não informado.");
            return "redirect:/acesso";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/acesso";
        }

        Usuario usuario = usuarioOpt.get();
        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        if (instituicoesSelecionadas == null || instituicoesSelecionadas.length == 0) {
            // Chama método de exclusão do cadastro
            cancelarCadastro(username, redirectAttributes, session);
            return "redirect:/acesso";
        }        
        
        // ===== 1) VALIDAÇÃO PRÉVIA - NÃO DELETA NADA AINDA =====
        for (String key : allParams.keySet()) {
            if (key.startsWith("instituicoesSelecionadas")) {
                String instIdStr = allParams.get(key);
                Long instId = Long.parseLong(instIdStr);

                // Data afiliação Instituição
                String dataAfiliacaoStr = allParams.get("dataAfiliacao_" + instId);
                if (dataAfiliacaoStr != null && !dataAfiliacaoStr.isEmpty()) {
                    LocalDate dataAfiliacao = LocalDate.parse(dataAfiliacaoStr);
                    if (dataAfiliacao.isAfter(LocalDate.now())) {
                        model.addAttribute("mensagemErro", "A data de afiliação da instituição não pode ser no futuro.");
                        prepararTela(model, username, usuario, allParams);
                        return "cadastro-relacionamentos";
                    }
                }

                // SubInstituição
                String subInstIdStr = allParams.get("subInstituicao_" + instId);
                if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                    String dataAfiliacaoSubStr = allParams.get("dataAfiliacaoSub_" + instId);
                    if (dataAfiliacaoSubStr != null && !dataAfiliacaoSubStr.isEmpty()) {
                        LocalDate dataAfiliacaoSub = LocalDate.parse(dataAfiliacaoSubStr);
                        if (dataAfiliacaoSub.isAfter(LocalDate.now())) {
                            model.addAttribute("mensagemErro", "A data de afiliação da subinstituição não pode ser no futuro.");
                            prepararTela(model, username, usuario, allParams);
                            return "cadastro-relacionamentos";
                        }
                    }
                }
            }
        }

        // ===== 2) TUDO VALIDADO: Deleta antigos =====
        usuarioInstituicaoRepository.deleteAllByUsuarioId(usuario.getId());
        pessoaInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());
        pessoaSubInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());

        // ===== 3) Insere os novos vínculos =====
        for (String instIdStr : instituicoesSelecionadas) {
            Long instId = Long.parseLong(instIdStr);

            // Data afiliação
            String dataAfiliacaoStr = allParams.get("dataAfiliacao_" + instId);
            LocalDate dataAfiliacao = (dataAfiliacaoStr != null && !dataAfiliacaoStr.isEmpty())
                    ? LocalDate.parse(dataAfiliacaoStr) : null;

            // Inclui PessoaInstituicao
            PessoaInstituicao psi = new PessoaInstituicao();
            psi.setPessoa(pessoa);
            psi.setInstituicao(instituicaoRepository.findById(instId).orElse(null));
            psi.setDataUltimaAtualizacao(LocalDate.now());
            psi.setDataAfiliacao(dataAfiliacao);

            String identificacao = allParams.get("identificacao_" + instId);
            psi.setIdentificacaoPessoaInstituicao(identificacao);
            pessoaInstituicaoRepository.save(psi);

            // Inclui UsuarioInstituicao
            UsuarioInstituicao ui = new UsuarioInstituicao();
            ui.setUsuario(usuario);
            ui.setInstituicao(psi.getInstituicao());
            ui.setSitAcessoUsuarioInstituicao("A");
            usuarioInstituicaoRepository.save(ui);

            // SubInstituição
            String subInstIdStr = allParams.get("subInstituicao_" + instId);
            if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                Long subInstId = Long.parseLong(subInstIdStr);
                SubInstituicao subInst = subInstituicaoRepository.findById(subInstId).orElse(null);

                if (subInst != null) {
                    String dataAfiliacaoSubStr = allParams.get("dataAfiliacaoSub_" + instId);
                    LocalDate dataAfiliacaoSub = (dataAfiliacaoSubStr != null && !dataAfiliacaoSubStr.isEmpty())
                            ? LocalDate.parse(dataAfiliacaoSubStr) : null;

                    PessoaSubInstituicao psiSub = new PessoaSubInstituicao();
                    psiSub.setPessoa(pessoa);
                    psiSub.setSubInstituicao(subInst);
                    psiSub.setInstituicao(subInst.getInstituicao());
                    psiSub.setDataUltimaAtualizacao(LocalDate.now());
                    psiSub.setDataAfiliacao(dataAfiliacaoSub);

                    psiSub.setIdentificacaoPessoaSubInstituicao(
                            allParams.get("identificacaoSub_" + instId)
                    );

                    pessoaSubInstituicaoRepository.save(psiSub);
                }
            }
        }

        // Remove dados de sessão
        session.removeAttribute("usuarioPendencia");
        
        // Redireciona baseado na origem do cadastro
        String origemCadastro = (String) session.getAttribute("origemCadastro");
        session.removeAttribute("origemCadastro");

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cadastro concluído com sucesso! Usuário: " 
                + usuario.getUsername()
                + " - " + (pessoa.getNomePessoa() != null ? pessoa.getNomePessoa() : ""));

        // Redireciona para onde o cadastro foi iniciado para permitir novos cadastros
        if ("administrador".equals(origemCadastro)) {
            return "redirect:/cadastro-usuario?origem=administrador";
        } else if ("superusuario".equals(origemCadastro)) {
            return "redirect:/cadastro-usuario?origem=superusuario";
        } else {
            return "redirect:/acesso";
        }
    }
    
    private void prepararTela(Model model, String username, Usuario usuario, Map<String, String> allParams) {
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findAll());
        model.addAttribute("username", username);
        model.addAttribute("nomeUsuario", usuario.getPessoa() != null ? usuario.getPessoa().getNomePessoa() : "");
        model.addAttribute("parametrosForm", allParams);
    }
    
    @GetMapping("/cancelar")
    @Transactional
    public String cancelarCadastro(@RequestParam String username, RedirectAttributes redirectAttributes, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            Pessoa pessoa = usuario.getPessoa();

            // Remove todos os vínculos (se criados, por segurança)
            usuarioInstituicaoRepository.deleteAllByUsuarioId(usuario.getId());
            if (pessoa != null) {
                pessoaInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());
                pessoaSubInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());
            }
            // 1) Exclua o usuário antes!
            usuarioRepository.delete(usuario);

            // 2) Agora pode excluir pessoa
            if (pessoa != null) {
                pessoaRepository.delete(pessoa);
            }
        }

        // Limpa sessão
        session.removeAttribute("usuarioPendencia");

        redirectAttributes.addFlashAttribute("mensagemErro", "Cadastramento cancelado.");
        return "redirect:/acesso";
    }
}
