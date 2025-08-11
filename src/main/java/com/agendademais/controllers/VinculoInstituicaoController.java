package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller para gerenciar os vínculos do usuário com instituições
 * Baseado no modelo /cadastro-relacionamentos
 */
@Controller
@RequestMapping("/vinculo-instituicao")
public class VinculoInstituicaoController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private PessoaInstituicaoRepository pessoaInstituicaoRepository;

    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    /**
     * Exibe a lista de vínculos do usuário com instituições
     */
    @GetMapping
    public String exibirVinculos(HttpSession session, Model model, RedirectAttributes redirectAttributes,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicaoAtual = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        // Buscar vínculos do usuário com instituições
        List<UsuarioInstituicao> vinculosUsuario = usuarioInstituicaoRepository.findByUsuario(usuario);

        // Buscar vínculos da pessoa com instituições
        List<PessoaInstituicao> vinculosPessoa = pessoaInstituicaoRepository.findByPessoa(pessoa);

        // Buscar vínculos da pessoa com sub-instituições
        List<PessoaSubInstituicao> vinculosSubInstituicao = pessoaSubInstituicaoRepository.findByPessoa(pessoa);

        // Dados para o cabeçalho
        model.addAttribute("nomeInstituicao", instituicaoAtual != null ? instituicaoAtual.getNomeInstituicao() : "");
        model.addAttribute("nomeUsuario", usuario.getUsername());
        model.addAttribute("nomePessoa", pessoa.getNomePessoa());

        // Dados dos vínculos
        model.addAttribute("vinculosUsuario", vinculosUsuario);
        model.addAttribute("vinculosPessoa", vinculosPessoa);
        model.addAttribute("vinculosSubInstituicao", vinculosSubInstituicao);

        // Adicionar origem para navegação
        model.addAttribute("origem", origem);

        // Como removemos a funcionalidade de vínculos múltiplos, redirecionamos para dados-autor
        return "redirect:/dados-autor?origem=" + origem;
    }

    /**
     * Exibe o formulário para editar vínculos com instituições
     */
    @GetMapping("/editar")
    public String exibirFormularioEdicao(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicaoAtual = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        // Dados para o cabeçalho
        model.addAttribute("nomeInstituicao", instituicaoAtual != null ? instituicaoAtual.getNomeInstituicao() : "");
        model.addAttribute("nomeUsuario", usuario.getUsername());
        model.addAttribute("nomePessoa", pessoa.getNomePessoa());

        // Buscar vínculos atuais
        List<UsuarioInstituicao> vinculosUsuario = usuarioInstituicaoRepository.findByUsuario(usuario);
        List<PessoaInstituicao> vinculosPessoa = pessoaInstituicaoRepository.findByPessoa(pessoa);

        // Buscar todas as instituições e sub-instituições ativas para seleção
        List<Instituicao> instituicoesAtivas = instituicaoRepository.findBySituacaoInstituicao("A");
        List<SubInstituicao> subInstituicoesAtivas = subInstituicaoRepository.findBySituacaoSubInstituicao("A");

        model.addAttribute("vinculosUsuario", vinculosUsuario);
        model.addAttribute("vinculosPessoa", vinculosPessoa);
        model.addAttribute("instituicoes", instituicoesAtivas);
        model.addAttribute("subInstituicoes", subInstituicoesAtivas);
        model.addAttribute("usuario", usuario);

        return "profile/vinculo-instituicao-editar";
    }

    /**
     * API endpoint para buscar sub-instituições por termo de pesquisa (autocomplete)
     */
    @GetMapping("/api/sub-instituicoes")
    @ResponseBody
    public List<SubInstituicao> buscarSubInstituicoes(@RequestParam("termo") String termo) {
        if (termo == null || termo.trim().length() < 2) {
            return List.of();
        }
        
        // Buscar sub-instituições ativas que contenham o termo no nome
        return subInstituicaoRepository.findByNomeSubInstituicaoContainingIgnoreCaseAndSituacaoSubInstituicao(
            termo.trim(), "A");
    }

    /**
     * Adiciona novo vínculo com sub-instituição
     */
    @PostMapping("/adicionar-sub-instituicao")
    public String adicionarVinculoSubInstituicao(
            @RequestParam("subInstituicaoId") Long subInstituicaoId,
            @RequestParam("identificacao") String identificacao,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null || usuario.getPessoa() == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida.");
                return "redirect:/acesso";
            }

            Pessoa pessoa = usuario.getPessoa();
            SubInstituicao subInstituicao = subInstituicaoRepository.findById(subInstituicaoId).orElse(null);
            
            if (subInstituicao == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sub-instituição não encontrada.");
                return "redirect:/vinculo-instituicao?origem=" + origem;
            }

            // Verificar se já existe vínculo
            boolean jaExiste = pessoaSubInstituicaoRepository.existsByPessoaAndSubInstituicao(pessoa, subInstituicao);
            if (jaExiste) {
                redirectAttributes.addFlashAttribute("mensagemErro", 
                    "Você já possui vínculo com esta sub-instituição.");
                return "redirect:/vinculo-instituicao?origem=" + origem;
            }

            // Criar novo vínculo
            PessoaSubInstituicao novoVinculo = new PessoaSubInstituicao();
            novoVinculo.setPessoa(pessoa);
            novoVinculo.setSubInstituicao(subInstituicao);
            novoVinculo.setInstituicao(subInstituicao.getInstituicao());
            novoVinculo.setIdentificacaoPessoaSubInstituicao(identificacao);
            novoVinculo.setDataAfiliacao(java.time.LocalDate.now());
            novoVinculo.setDataUltimaAtualizacao(java.time.LocalDate.now());

            pessoaSubInstituicaoRepository.save(novoVinculo);

            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                "Vínculo com sub-instituição adicionado com sucesso: " + subInstituicao.getNomeSubInstituicao());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Erro ao adicionar vínculo: " + e.getMessage());
        }

        return "redirect:/vinculo-instituicao?origem=" + origem;
    }

    /**
     * Atualiza vínculo existente com sub-instituição
     */
    @PostMapping("/atualizar-sub-instituicao/{id}")
    public String atualizarVinculoSubInstituicao(
            @PathVariable Long id,
            @RequestParam("identificacao") String identificacao,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null || usuario.getPessoa() == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida.");
                return "redirect:/acesso";
            }

            PessoaSubInstituicao vinculo = pessoaSubInstituicaoRepository.findById(id).orElse(null);
            if (vinculo == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Vínculo não encontrado.");
                return "redirect:/vinculo-instituicao?origem=" + origem;
            }

            // Verificar se o vínculo pertence ao usuário
            if (!vinculo.getPessoa().getId().equals(usuario.getPessoa().getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Você não tem permissão para alterar este vínculo.");
                return "redirect:/vinculo-instituicao?origem=" + origem;
            }

            vinculo.setIdentificacaoPessoaSubInstituicao(identificacao);
            vinculo.setDataUltimaAtualizacao(java.time.LocalDate.now());
            
            pessoaSubInstituicaoRepository.save(vinculo);

            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                "Vínculo atualizado com sucesso: " + vinculo.getSubInstituicao().getNomeSubInstituicao());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Erro ao atualizar vínculo: " + e.getMessage());
        }

        return "redirect:/vinculo-instituicao?origem=" + origem;
    }

    /**
     * Remove vínculos selecionados com sub-instituições
     */
    @PostMapping("/remover-sub-instituicoes")
    public String removerVinculosSubInstituicao(
            @RequestParam("vinculosSelecionados") List<Long> vinculosSelecionados,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null || usuario.getPessoa() == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida.");
                return "redirect:/acesso";
            }

            int removidos = 0;
            for (Long vinculoId : vinculosSelecionados) {
                PessoaSubInstituicao vinculo = pessoaSubInstituicaoRepository.findById(vinculoId).orElse(null);
                if (vinculo != null && vinculo.getPessoa().getId().equals(usuario.getPessoa().getId())) {
                    pessoaSubInstituicaoRepository.delete(vinculo);
                    removidos++;
                }
            }

            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                removidos + " vínculo(s) removido(s) com sucesso.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Erro ao remover vínculos: " + e.getMessage());
        }

        return "redirect:/vinculo-instituicao?origem=" + origem;
    }
}
