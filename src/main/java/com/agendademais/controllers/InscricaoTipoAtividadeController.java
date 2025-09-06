package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar as inscrições do usuário em tipos de atividades
 * CRUD para entidades Inscricao e InscricaoTipoAtividade
 */
@Controller
@RequestMapping("/inscricao-tipo-atividade")
public class InscricaoTipoAtividadeController {

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private InscricaoTipoAtividadeRepository inscricaoTipoAtividadeRepository;

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    /**
     * Endpoint de teste simples
     */
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        System.out.println("*** DEBUG - /inscricao-tipo-atividade/test ==> Test endpoint funcionando!");
        return "Controller InscricaoTipoAtividadeController funcionando!";
    }

    /**
     * Exibe a lista das inscrições do usuário em tipos de atividades
     */
    @GetMapping
    public String exibirInscricoes(HttpSession session, Model model, RedirectAttributes redirectAttributes,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem) {
        System.out.println("******************************************");
        System.out.println("*** MÉTODO exibirInscricoes() CHAMADO ***");
        System.out.println("*** DEBUG - Origem: " + origem);
        System.out.println("******************************************");
        try {
            System.out.println("*** DEBUG InscricaoTipoAtividadeController.exibirInscricoes() - INÍCIO ***");

            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

            System.out.println("*** DEBUG - Usuario: " + (usuario != null ? usuario.getUsername() : "null"));
            System.out.println(
                    "*** DEBUG - Instituicao: " + (instituicao != null ? instituicao.getNomeInstituicao() : "null"));

            if (usuario == null || instituicao == null) {
                System.out.println("*** DEBUG - Sessão inválida, redirecionando para /acesso");
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
                return "redirect:/acesso";
            }

            Pessoa pessoa = usuario.getPessoa();
            System.out.println("*** DEBUG - Pessoa: " + (pessoa != null ? pessoa.getNomePessoa() : "null"));

            if (pessoa == null) {
                System.out.println("*** DEBUG - Pessoa nula, redirecionando para /acesso");
                redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
                return "redirect:/acesso";
            }

            // Dados para o cabeçalho
            model.addAttribute("nomeInstituicao", instituicao.getNomeInstituicao());
            model.addAttribute("nomeUsuario", usuario.getUsername());
            model.addAttribute("nomePessoa", pessoa.getNomePessoa());

            // Buscar inscrição da pessoa na instituição atual
            Optional<Inscricao> inscricaoOpt = null;
            try {
                System.out.println("*** DEBUG - Buscando inscrição para pessoa=" + pessoa.getId() + ", instituicao="
                        + instituicao.getId());
                inscricaoOpt = inscricaoRepository.findByPessoaAndIdInstituicao(pessoa, instituicao);
            } catch (Exception e) {
                System.err.println("*** ERRO ao buscar inscrição: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            if (inscricaoOpt.isPresent()) {
                System.out.println("*** DEBUG - Inscrição encontrada: " + inscricaoOpt.get().getId());
                Inscricao inscricao = inscricaoOpt.get();

                // Buscar tipos de atividades vinculados à inscrição
                List<InscricaoTipoAtividade> inscricoesTipoAtividade = null;
                try {
                    inscricoesTipoAtividade = inscricaoTipoAtividadeRepository.findByInscricao(inscricao);
                } catch (Exception e) {
                    System.err.println("*** ERRO ao buscar tipos de atividade da inscrição: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }

                System.out.println("*** DEBUG - Tipos de atividade encontrados: " + inscricoesTipoAtividade.size());

                model.addAttribute("inscricao", inscricao);
                model.addAttribute("inscricoesTipoAtividade", inscricoesTipoAtividade);
                model.addAttribute("minhasInscricoes", inscricoesTipoAtividade); // Para compatibilidade com template
            } else {
                System.out.println("*** DEBUG - Nenhuma inscrição encontrada");
                model.addAttribute("inscricao", null);
                model.addAttribute("inscricoesTipoAtividade", List.of());
                model.addAttribute("minhasInscricoes", List.of()); // Para compatibilidade com template
            }

            // Buscar todos os tipos de atividades da instituição para possível adição
            List<TipoAtividade> tiposAtividadeDisponiveis = null;
            try {
                System.out.println("*** DEBUG - Buscando tipos de atividade da instituição: " + instituicao.getId());
                tiposAtividadeDisponiveis = tipoAtividadeRepository.findByInstituicaoId(instituicao.getId());
                if (tiposAtividadeDisponiveis == null) {
                    tiposAtividadeDisponiveis = List.of();
                }
            } catch (Exception e) {
                System.err.println("*** ERRO ao buscar tipos de atividade: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            System.out.println("*** DEBUG - Tipos disponíveis encontrados: " + tiposAtividadeDisponiveis.size());
            model.addAttribute("tiposAtividadeDisponiveis", tiposAtividadeDisponiveis);
            model.addAttribute("tiposDisponiveis", tiposAtividadeDisponiveis); // Para compatibilidade com template

            // Adicionar origem para navegação
            model.addAttribute("origem", origem);

            System.out.println("*** DEBUG - Retornando template: profile/inscricao-tipo-atividade");
            return "profile/inscricao-tipo-atividade";

        } catch (Exception e) {
            System.err.println("*** ERRO InscricaoTipoAtividadeController.exibirInscricoes(): " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro interno: " + e.getMessage());
            return "redirect:/participante";
        }
    }

    /**
     * Adiciona uma nova inscrição em tipo de atividade
     */
    @PostMapping("/adicionar-tipo")
    public String adicionarTipoAtividade(
            @RequestParam("tipoAtividadeId") Long tipoAtividadeId,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("*** DEBUG - Adicionando inscrição no tipo: " + tipoAtividadeId);
        System.out.println("*** DEBUG - Origem: " + origem);

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

            if (usuario == null || instituicao == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida.");
                return "redirect:/acesso";
            }

            Pessoa pessoa = usuario.getPessoa();
            if (pessoa == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
                return "redirect:/acesso";
            }

            // Buscar o tipo de atividade
            Optional<TipoAtividade> tipoOpt = tipoAtividadeRepository.findById(tipoAtividadeId);
            if (!tipoOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Tipo de atividade não encontrado.");
                return "redirect:/inscricao-tipo-atividade";
            }

            TipoAtividade tipoAtividade = tipoOpt.get();

            // Verificar se o tipo pertence à instituição atual
            if (!tipoAtividade.getInstituicao().getId().equals(instituicao.getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Tipo de atividade não pertence à instituição atual.");
                return "redirect:/inscricao-tipo-atividade";
            }

            // Buscar ou criar inscrição da pessoa na instituição
            Optional<Inscricao> inscricaoOpt = inscricaoRepository.findByPessoaAndIdInstituicao(pessoa, instituicao);
            Inscricao inscricao;

            if (inscricaoOpt.isPresent()) {
                inscricao = inscricaoOpt.get();
            } else {
                // Criar nova inscrição
                inscricao = new Inscricao();
                inscricao.setPessoa(pessoa);
                inscricao.setIdInstituicao(instituicao);
                inscricao.setDataInclusao(LocalDate.now());
                inscricao.setDataUltimaAtualizacao(LocalDate.now());
                inscricao = inscricaoRepository.save(inscricao);
                System.out.println("*** DEBUG - Nova inscrição criada: " + inscricao.getId());
            }

            // Verificar se já existe inscrição neste tipo de atividade
            List<InscricaoTipoAtividade> existentes = inscricaoTipoAtividadeRepository.findByInscricao(inscricao);
            boolean jaInscrito = existentes.stream()
                    .anyMatch(ita -> ita.getTipoAtividade().getId().equals(tipoAtividadeId));

            if (jaInscrito) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Você já está inscrito neste tipo de atividade.");
                return "redirect:/inscricao-tipo-atividade";
            }

            // Criar nova inscrição no tipo de atividade
            InscricaoTipoAtividade inscricaoTipoAtividade = new InscricaoTipoAtividade();
            inscricaoTipoAtividade.setInscricao(inscricao);
            inscricaoTipoAtividade.setTipoAtividade(tipoAtividade);

            inscricaoTipoAtividadeRepository.save(inscricaoTipoAtividade);

            System.out.println("*** DEBUG - Inscrição em tipo de atividade criada: " + inscricaoTipoAtividade.getId());
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Inscrição realizada com sucesso em: " + tipoAtividade.getTituloTipoAtividade());

        } catch (Exception e) {
            System.err.println("*** ERRO ao adicionar inscrição: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao realizar inscrição: " + e.getMessage());
        }

        return "redirect:/inscricao-tipo-atividade?origem=" + origem;
    }

    /**
     * Remove uma inscrição individual
     */
    @PostMapping("/remover-tipo/{id}")
    public String removerTipoAtividade(
            @PathVariable Long id,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("*** DEBUG - Removendo inscrição ID: " + id);

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

            if (usuario == null || instituicao == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida.");
                return "redirect:/acesso";
            }

            Optional<InscricaoTipoAtividade> inscricaoOpt = inscricaoTipoAtividadeRepository.findById(id);
            if (!inscricaoOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Inscrição não encontrada.");
                return "redirect:/inscricao-tipo-atividade";
            }

            InscricaoTipoAtividade inscricao = inscricaoOpt.get();

            // Debug detalhado da verificação de propriedade
            Long pessoaUsuario = usuario.getPessoa().getId();
            Long pessoaInscricao = inscricao.getInscricao().getPessoa().getId();
            System.out.println("*** DEBUG - ID da pessoa do usuário: " + pessoaUsuario);
            System.out.println("*** DEBUG - ID da pessoa da inscrição: " + pessoaInscricao);
            System.out.println("*** DEBUG - São iguais? " + pessoaUsuario.equals(pessoaInscricao));

            // Verificar se a inscrição pertence ao usuário logado
            if (!inscricao.getInscricao().getPessoa().getId().equals(usuario.getPessoa().getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Você não tem permissão para remover esta inscrição.");
                return "redirect:/inscricao-tipo-atividade?origem=" + origem;
            }

            String tituloTipo = inscricao.getTipoAtividade().getTituloTipoAtividade();
            Inscricao inscricaoPai = inscricao.getInscricao();
            inscricaoTipoAtividadeRepository.delete(inscricao);

            // Verificar se ainda existem outros tipos de atividade para esta inscrição
            List<InscricaoTipoAtividade> outrasInscricoes = inscricaoTipoAtividadeRepository
                    .findByInscricao(inscricaoPai);
            if (outrasInscricoes.isEmpty()) {
                // Se não há mais tipos de atividade, remover a inscrição principal
                inscricaoRepository.delete(inscricaoPai);
                System.out.println("*** DEBUG - Inscrição principal removida em cascata: " + inscricaoPai.getId());
            }

            System.out.println("*** DEBUG - Inscrição removida com sucesso: " + id);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Inscrição removida com sucesso: " + tituloTipo);

        } catch (Exception e) {
            System.err.println("*** ERRO ao remover inscrição: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao remover inscrição: " + e.getMessage());
        }

        return "redirect:/inscricao-tipo-atividade?origem=" + origem;
    }

    /**
     * Remove múltiplas inscrições selecionadas
     */
    @PostMapping("/remover-multiplas")
    public String removerMultiplasInscricoes(
            @RequestParam("inscricoesSelecionadas") List<Long> inscricoesSelecionadas,
            @RequestParam(value = "origem", required = false, defaultValue = "participante") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("*** DEBUG - Removendo múltiplas inscrições: " + inscricoesSelecionadas);
        System.out.println("*** DEBUG - Origem: " + origem);

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

            if (usuario == null || instituicao == null) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida.");
                return "redirect:/acesso";
            }

            if (inscricoesSelecionadas == null || inscricoesSelecionadas.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Nenhuma inscrição foi selecionada.");
                return "redirect:/inscricao-tipo-atividade";
            }

            int removidas = 0;
            for (Long id : inscricoesSelecionadas) {
                System.out.println("*** DEBUG - Processando ID: " + id);
                Optional<InscricaoTipoAtividade> inscricaoOpt = inscricaoTipoAtividadeRepository.findById(id);
                if (inscricaoOpt.isPresent()) {
                    InscricaoTipoAtividade inscricao = inscricaoOpt.get();

                    // Debug detalhado da verificação de propriedade
                    Long pessoaUsuario = usuario.getPessoa().getId();
                    Long pessoaInscricao = inscricao.getInscricao().getPessoa().getId();
                    System.out.println("*** DEBUG - ID da pessoa do usuário: " + pessoaUsuario);
                    System.out.println("*** DEBUG - ID da pessoa da inscrição: " + pessoaInscricao);
                    System.out.println("*** DEBUG - São iguais? " + pessoaUsuario.equals(pessoaInscricao));

                    // Verificar se a inscrição pertence ao usuário logado
                    if (inscricao.getInscricao().getPessoa().getId().equals(usuario.getPessoa().getId())) {
                        Inscricao inscricaoPai = inscricao.getInscricao();
                        inscricaoTipoAtividadeRepository.delete(inscricao);
                        removidas++;
                        System.out.println("*** DEBUG - Inscrição ID " + id + " removida com sucesso");

                        // Verificar se ainda existem outros tipos de atividade para esta inscrição
                        List<InscricaoTipoAtividade> outrasInscricoes = inscricaoTipoAtividadeRepository
                                .findByInscricao(inscricaoPai);
                        if (outrasInscricoes.isEmpty()) {
                            // Se não há mais tipos de atividade, remover a inscrição principal
                            inscricaoRepository.delete(inscricaoPai);
                            System.out.println(
                                    "*** DEBUG - Inscrição principal removida em cascata: " + inscricaoPai.getId());
                        }
                    } else {
                        System.out.println("*** DEBUG - Inscrição ID " + id + " NÃO pertence ao usuário");
                    }
                } else {
                    System.out.println("*** DEBUG - Inscrição ID " + id + " não encontrada");
                }
            }

            System.out.println("*** DEBUG - " + removidas + " inscrições removidas com sucesso");
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    removidas + " inscrição(ões) removida(s) com sucesso.");

        } catch (Exception e) {
            System.err.println("*** ERRO ao remover múltiplas inscrições: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao remover inscrições: " + e.getMessage());
        }

        return "redirect:/inscricao-tipo-atividade?origem=" + origem;
    }
}
