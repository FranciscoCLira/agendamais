package com.agendademais.controllers;

import com.agendademais.dto.InscricaoForm;
import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import com.agendademais.services.*;

import com.agendademais.exceptions.BusinessException;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/participante/inscricao-form")
public class InscricaoController {

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;
    
    
    // INJETAR OS SERVICOS 
    
    @Autowired
    private InscricaoService inscricaoService;

    @Autowired
    private TipoAtividadeService tipoAtividadeService;

    @Autowired
    private InscricaoTipoAtividadeService inscricaoTipoAtividadeService;
    
    
    @GetMapping
    public String exibirFormulario(Model model, HttpSession session, RedirectAttributes ra) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuario == null || instituicao == null) {
            ra.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/login";
        }

        List<TipoAtividade> atividades = tipoAtividadeRepository.findByInstituicaoId(instituicao.getId());
        model.addAttribute("atividades", atividades);

        // Busca inscrição existente
        Optional<Inscricao> optInscricao = inscricaoRepository.findByIdPessoaAndIdInstituicao(usuario.getPessoa(), instituicao);

        InscricaoForm form = new InscricaoForm();
        if (optInscricao.isPresent()) {
            Inscricao inscricao = optInscricao.get();
            // Preenche os IDs já inscritos para marcar os checkboxes
            List<Long> jaInscritas = inscricao.getTiposAtividade().stream()
                .map(ita -> ita.getTipoAtividade().getId())
                .toList();
            form.setTiposAtividadeIds(jaInscritas);
            form.setComentarios(inscricao.getComentarios());
        }

        model.addAttribute("inscricaoForm", form);

        // Mensagens inteligentes:
        if (form.getTiposAtividadeIds().isEmpty()) {
            model.addAttribute("statusMsg", "Você ainda não está inscrito em nenhuma atividade.");
        } else {
            model.addAttribute("statusMsg", "Você está inscrito em atividades.");
        }

        return "participante/inscricao-form";
    }


    
    @PostMapping("/salvar")
    public String salvarInscricao(@ModelAttribute("inscricaoForm") InscricaoForm form, HttpSession session, RedirectAttributes ra) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuario == null || instituicao == null) {
            ra.addFlashAttribute("mensagemErro", "Sessão inválida.");
            return "redirect:/login";
        }

        // 1. Verifica se selecionou pelo menos uma atividade
        if (form.getTiposAtividadeIds() == null || form.getTiposAtividadeIds().isEmpty()) {
            ra.addFlashAttribute("mensagemErro", "Selecione uma ou mais atividades para se inscrever.");
            return "redirect:/participante/inscricao-form";
        }

        // 2. Busca inscrição existente OU cria nova
        Optional<Inscricao> optInscricao = inscricaoRepository.findByIdPessoaAndIdInstituicao(usuario.getPessoa(), instituicao);
        Inscricao inscricao;
        if (optInscricao.isPresent()) {
            // Se já existe, atualiza a inscrição existente (adicionando/removendo atividades)
            inscricao = optInscricao.get();
        } else {
            // Senão, cria nova inscrição
            inscricao = new Inscricao();
            inscricao.setIdPessoa(usuario.getPessoa());
            inscricao.setIdInstituicao(instituicao);
            inscricao.setDataInclusao(LocalDate.now());
        }
        inscricao.setDataUltimaAtualizacao(LocalDate.now());
        inscricao.setComentarios(form.getComentarios());

        // 3. Atualiza atividades (marca/desmarca)
        Set<InscricaoTipoAtividade> novasAtividades = new HashSet<>();
        for (Long idTipo : form.getTiposAtividadeIds()) {
            TipoAtividade tipo = tipoAtividadeRepository.findById(idTipo)
                .orElseThrow(() -> new RuntimeException("Tipo não encontrado"));
            // Checa se já está na inscrição (se sim, mantém, se não, adiciona)
            boolean jaExiste = inscricao.getTiposAtividade().stream()
                .anyMatch(ita -> ita.getTipoAtividade().getId().equals(tipo.getId()));
            if (!jaExiste) {
                InscricaoTipoAtividade ita = new InscricaoTipoAtividade();
                ita.setInscricao(inscricao);
                ita.setTipoAtividade(tipo);
                novasAtividades.add(ita);
            }
        }
        // Remove atividades desmarcadas
        inscricao.getTiposAtividade().removeIf(ita ->
            form.getTiposAtividadeIds().stream()
                .noneMatch(id -> id.equals(ita.getTipoAtividade().getId()))
        );
        inscricao.getTiposAtividade().addAll(novasAtividades);

        // 4. Não salvar se não ficou nenhuma atividade marcada!
        if (inscricao.getTiposAtividade().isEmpty()) {
            ra.addFlashAttribute("mensagemErro", "Selecione uma ou mais atividades para se inscrever.");
            return "redirect:/participante/inscricao-form";
        }

        inscricaoRepository.save(inscricao);

        ra.addFlashAttribute("mensagemSucesso", "Inscrição atualizada com sucesso.");
        return "redirect:/participante/inscricao-form";
    }
    
    
    // método para adicionar tipo de atividade
    
    @PostMapping("/{id}/adicionar-tipo")
    public String adicionarTipoAtividade(
        @PathVariable Long id,
        @RequestParam Long tipoAtividadeId,
        RedirectAttributes redirectAttributes
    ) {
        Inscricao inscricao = inscricaoService.findById(id);
        TipoAtividade tipoAtividade = tipoAtividadeService.findById(tipoAtividadeId);

        try {
            inscricaoTipoAtividadeService.vincularTipoAtividade(inscricao, tipoAtividade);
            redirectAttributes.addFlashAttribute("sucesso", "Tipo de atividade adicionado com sucesso!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/participante/inscricao-form";
    }
}
