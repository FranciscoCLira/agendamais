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
import java.util.Map;
import java.util.Optional;

/**
 * Controller para permitir que o SuperUsuário se vincule a instituições
 * que ele criou e que ainda não possui vínculo
 */
@Controller
@RequestMapping("/superusuario")
public class VinculoSuperusuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    public VinculoSuperusuarioController(
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

    /**
     * Exibe formulário para superusuário se vincular a instituições
     */
    @GetMapping("/vincular-instituicoes")
    public String exibirFormulario(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar se é superusuário (nível 9) ou controle total (nível 0)
        if (usuario == null || nivelAcesso == null || (nivelAcesso != 9 && nivelAcesso != 0)) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Acesso negado. Funcionalidade disponível apenas para SuperUsuários.");
            return "redirect:/acesso";
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        // Buscar todas as instituições ativas
        List<Instituicao> todasInstituicoes = instituicaoRepository.findBySituacaoInstituicao("A");

        // Buscar instituições já vinculadas ao usuário
        List<UsuarioInstituicao> vinculosExistentes = usuarioInstituicaoRepository.findByUsuario(usuario);
        List<Long> instituicoesVinculadas = vinculosExistentes.stream()
            .map(v -> v.getInstituicao().getId())
            .toList();

        // Buscar vínculos PessoaInstituicao existentes para popular campos
        List<PessoaInstituicao> vinculosPessoa = pessoaInstituicaoRepository.findByPessoa(pessoa);

        model.addAttribute("instituicoes", todasInstituicoes);
        model.addAttribute("subInstituicoes", subInstituicaoRepository.findBySituacaoSubInstituicao("A"));
        model.addAttribute("instituicoesVinculadas", instituicoesVinculadas);
        model.addAttribute("vinculosPessoa", vinculosPessoa);
        model.addAttribute("username", usuario.getUsername());
        model.addAttribute("nomeUsuario", pessoa.getNomePessoa());

        return "superusuario/vincular-instituicoes";
    }

    /**
     * Processa a criação de novos vínculos do superusuário com instituições
     */
    @Transactional
    @PostMapping("/vincular-instituicoes")
    public String processarVinculos(
            @RequestParam Map<String, String> allParams,
            @RequestParam(name = "instituicoesSelecionadas", required = false) String[] instituicoesSelecionadas,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar se é superusuário (nível 9) ou controle total (nível 0)
        if (usuario == null || nivelAcesso == null || (nivelAcesso != 9 && nivelAcesso != 0)) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Acesso negado. Funcionalidade disponível apenas para SuperUsuários.");
            return "redirect:/acesso";
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        if (instituicoesSelecionadas == null || instituicoesSelecionadas.length == 0) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Selecione ao menos uma instituição para vincular.");
            return "redirect:/superusuario/vincular-instituicoes";
        }

        try {
            int vinculosCriados = 0;

            for (String instIdStr : instituicoesSelecionadas) {
                Long instId = Long.parseLong(instIdStr);

                // Verificar se já existe vínculo UsuarioInstituicao
                Optional<UsuarioInstituicao> vinculoExistente = 
                    usuarioInstituicaoRepository.findByUsuarioIdAndInstituicaoId(usuario.getId(), instId);

                if (vinculoExistente.isPresent()) {
                    continue; // Já existe vínculo, pula para próxima
                }

                // Buscar instituição
                Optional<Instituicao> instituicaoOpt = instituicaoRepository.findById(instId);
                if (instituicaoOpt.isEmpty()) {
                    continue;
                }
                Instituicao instituicao = instituicaoOpt.get();

                // Data afiliação
                String dataAfiliacaoStr = allParams.get("dataAfiliacao_" + instId);
                LocalDate dataAfiliacao = (dataAfiliacaoStr != null && !dataAfiliacaoStr.isEmpty())
                        ? LocalDate.parse(dataAfiliacaoStr)
                        : LocalDate.now();

                if (dataAfiliacao.isAfter(LocalDate.now())) {
                    redirectAttributes.addFlashAttribute("mensagemErro", 
                        "A data de afiliação não pode ser no futuro.");
                    return "redirect:/superusuario/vincular-instituicoes";
                }

                // Criar ou atualizar PessoaInstituicao
                Optional<PessoaInstituicao> pessoaInstOpt = 
                    pessoaInstituicaoRepository.findByPessoaIdAndInstituicaoId(pessoa.getId(), instId);

                PessoaInstituicao psi;
                if (pessoaInstOpt.isPresent()) {
                    psi = pessoaInstOpt.get();
                } else {
                    psi = new PessoaInstituicao();
                    psi.setPessoa(pessoa);
                    psi.setInstituicao(instituicao);
                    psi.setDataAfiliacao(dataAfiliacao);
                }

                String identificacao = allParams.get("identificacao_" + instId);
                psi.setIdentificacaoPessoaInstituicao(identificacao);
                psi.setDataUltimaAtualizacao(LocalDate.now());
                pessoaInstituicaoRepository.save(psi);

                // Criar UsuarioInstituicao com nível 9 (SuperUsuário)
                UsuarioInstituicao ui = new UsuarioInstituicao();
                ui.setUsuario(usuario);
                ui.setInstituicao(instituicao);
                ui.setSitAcessoUsuarioInstituicao("A");
                ui.setNivelAcessoUsuarioInstituicao(9); // SuperUsuário
                usuarioInstituicaoRepository.save(ui);

                vinculosCriados++;

                // Processar SubInstituição se selecionada
                String subInstIdStr = allParams.get("subInstituicao_" + instId);
                if (subInstIdStr != null && !subInstIdStr.isEmpty()) {
                    Long subInstId = Long.parseLong(subInstIdStr);
                    SubInstituicao subInst = subInstituicaoRepository.findById(subInstId).orElse(null);

                    if (subInst != null) {
                        String dataAfiliacaoSubStr = allParams.get("dataAfiliacaoSub_" + instId);
                        LocalDate dataAfiliacaoSub = (dataAfiliacaoSubStr != null && !dataAfiliacaoSubStr.isEmpty())
                                ? LocalDate.parse(dataAfiliacaoSubStr)
                                : LocalDate.now();

                        if (dataAfiliacaoSub.isAfter(LocalDate.now())) {
                            redirectAttributes.addFlashAttribute("mensagemErro", 
                                "A data de afiliação da subinstituição não pode ser no futuro.");
                            return "redirect:/superusuario/vincular-instituicoes";
                        }

                        PessoaSubInstituicao psiSub = new PessoaSubInstituicao();
                        psiSub.setPessoa(pessoa);
                        psiSub.setSubInstituicao(subInst);
                        psiSub.setInstituicao(subInst.getInstituicao());
                        psiSub.setDataUltimaAtualizacao(LocalDate.now());
                        psiSub.setDataAfiliacao(dataAfiliacaoSub);
                        psiSub.setIdentificacaoPessoaSubInstituicao(
                                allParams.get("identificacaoSub_" + instId));
                        pessoaSubInstituicaoRepository.save(psiSub);
                    }
                }
            }

            if (vinculosCriados > 0) {
                redirectAttributes.addFlashAttribute("mensagemSucesso",
                        "Vínculo(s) criado(s) com sucesso! Total: " + vinculosCriados + 
                        ". As novas instituições aparecerão no seu próximo login.");
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Nenhum vínculo novo foi criado. Você já está vinculado às instituições selecionadas.");
            }

            return "redirect:/controle-total";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Erro ao criar vínculos: " + e.getMessage());
            return "redirect:/superusuario/vincular-instituicoes";
        }
    }
}
