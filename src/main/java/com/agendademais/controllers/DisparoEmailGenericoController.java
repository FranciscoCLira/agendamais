package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.model.DisparoEmailBatch;
import com.agendademais.model.DisparoEmailBatch.StatusDisparo;
import com.agendademais.model.DisparoEmailBatch.TipoDisparo;
import com.agendademais.service.DisparoEmailGenericoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller para gerenciar disparos genéricos de email em lote.
 */
@Controller
@RequestMapping("/disparo-emails")
public class DisparoEmailGenericoController {

    @Autowired
    private DisparoEmailGenericoService disparoService;

    /**
     * Página principal de gerenciamento de disparos.
     */
    @GetMapping
    public String index(HttpSession session, Model model) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        // Listar disparos recentes
        List<DisparoEmailBatch> disparos = disparoService.listarDisparosPorInstituicao(instituicao);

        model.addAttribute("disparos", disparos);
        model.addAttribute("tiposDisparo", TipoDisparo.values());
        model.addAttribute("statusDisparo", StatusDisparo.values());

        return "disparo-emails";
    }

    /**
     * Formulário para criar novo disparo.
     */
    @GetMapping("/novo")
    public String novoDisparo(HttpSession session, Model model) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        model.addAttribute("tiposDisparo", TipoDisparo.values());
        model.addAttribute("disparo", new DisparoEmailBatch());

        return "disparo-emails-form";
    }

    /**
     * Criar novo disparo de email.
     */
    @PostMapping("/criar")
    public String criarDisparo(
            @RequestParam("tipoDisparo") TipoDisparo tipoDisparo,
            @RequestParam("assunto") String assunto,
            @RequestParam("corpoHtml") String corpoHtml,
            @RequestParam(value = "filtroSituacao", required = false) String filtroSituacao,
            @RequestParam(value = "filtroDataInicio", required = false) String filtroDataInicio,
            @RequestParam(value = "filtroDataFim", required = false) String filtroDataFim,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuario == null || instituicao == null) {
            return "redirect:/acesso";
        }

        try {
            DisparoEmailBatch disparo = new DisparoEmailBatch();
            disparo.setTipoDisparo(tipoDisparo);
            disparo.setAssunto(assunto);
            disparo.setCorpoHtml(corpoHtml);
            disparo.setInstituicao(instituicao);
            disparo.setUsuarioCriador(usuario);

            // Aplicar filtros
            if (filtroSituacao != null && !filtroSituacao.isBlank()) {
                disparo.setFiltroSituacaoUsuario(filtroSituacao);
            }

            if (filtroDataInicio != null && !filtroDataInicio.isBlank()) {
                disparo.setFiltroDataInscricaoInicio(LocalDate.parse(filtroDataInicio));
            }

            if (filtroDataFim != null && !filtroDataFim.isBlank()) {
                disparo.setFiltroDataInscricaoFim(LocalDate.parse(filtroDataFim));
            }

            // Criar disparo (calcula destinatários)
            DisparoEmailBatch disparoCriado = disparoService.criarDisparo(disparo);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Disparo criado com sucesso! Total de destinatários: " + disparoCriado.getTotalDestinatarios());

            return "redirect:/disparo-emails/" + disparoCriado.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao criar disparo: " + e.getMessage());
            return "redirect:/disparo-emails/novo";
        }
    }

    /**
     * Detalhes de um disparo específico.
     */
    @GetMapping("/{id}")
    public String detalhesDisparo(@PathVariable Long id, HttpSession session, Model model) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        DisparoEmailBatch disparo = disparoService.obterDisparo(id);

        if (disparo == null || !disparo.getInstituicao().getId().equals(instituicao.getId())) {
            model.addAttribute("mensagemErro", "Disparo não encontrado");
            return "redirect:/disparo-emails";
        }

        model.addAttribute("disparo", disparo);
        return "disparo-emails-detalhes";
    }

    /**
     * Processar disparo (iniciar envio).
     */
    @PostMapping("/{id}/processar")
    public String processarDisparo(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        try {
            DisparoEmailBatch disparo = disparoService.obterDisparo(id);

            if (disparo == null || !disparo.getInstituicao().getId().equals(instituicao.getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Disparo não encontrado");
                return "redirect:/disparo-emails";
            }

            if (disparo.getStatus() != StatusDisparo.PENDENTE) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Disparo não pode ser processado. Status atual: " + disparo.getStatus());
                return "redirect:/disparo-emails/" + id;
            }

            // Processar em background
            disparoService.processarDisparoAsync(id);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Disparo iniciado! O processamento está sendo executado em background.");

            return "redirect:/disparo-emails/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao processar disparo: " + e.getMessage());
            return "redirect:/disparo-emails/" + id;
        }
    }

    /**
     * Cancelar disparo pendente.
     */
    @PostMapping("/{id}/cancelar")
    public String cancelarDisparo(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        try {
            DisparoEmailBatch disparo = disparoService.obterDisparo(id);

            if (disparo == null || !disparo.getInstituicao().getId().equals(instituicao.getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Disparo não encontrado");
                return "redirect:/disparo-emails";
            }

            if (disparo.getStatus() != StatusDisparo.PENDENTE && disparo.getStatus() != StatusDisparo.PROCESSANDO) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Disparo não pode ser cancelado. Status atual: " + disparo.getStatus());
                return "redirect:/disparo-emails/" + id;
            }

            disparoService.cancelarDisparo(id);

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Disparo cancelado com sucesso!");
            return "redirect:/disparo-emails/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao cancelar disparo: " + e.getMessage());
            return "redirect:/disparo-emails/" + id;
        }
    }

    /**
     * Excluir disparo.
     */
    @PostMapping("/{id}/excluir")
    public String excluirDisparo(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        try {
            DisparoEmailBatch disparo = disparoService.obterDisparo(id);

            if (disparo == null || !disparo.getInstituicao().getId().equals(instituicao.getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Disparo não encontrado");
                return "redirect:/disparo-emails";
            }

            disparoService.excluirDisparo(id);

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Disparo excluído com sucesso!");
            return "redirect:/disparo-emails";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao excluir disparo: " + e.getMessage());
            return "redirect:/disparo-emails";
        }
    }

    /**
     * API: Obter progresso do disparo (AJAX).
     */
    @GetMapping("/{id}/progresso")
    @ResponseBody
    public DisparoEmailBatch obterProgresso(@PathVariable Long id, HttpSession session) {
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (instituicao == null) {
            return null;
        }

        DisparoEmailBatch disparo = disparoService.obterDisparo(id);

        if (disparo == null || !disparo.getInstituicao().getId().equals(instituicao.getId())) {
            return null;
        }

        return disparo;
    }

    /**
     * API: Carregar template HTML.
     */
    @GetMapping("/templates/{tipo}")
    @ResponseBody
    public String carregarTemplate(@PathVariable String tipo, HttpSession session) {
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (instituicao == null) {
            return "";
        }

        try {
            // Usa ThymeleafTemplateResolver para carregar template
            return disparoService.carregarTemplateHtml(tipo);
        } catch (Exception e) {
            System.err.println("Erro ao carregar template: " + e.getMessage());
            return "";
        }
    }

    /**
     * API: Contar destinatários com base nos filtros (AJAX).
     */
    @PostMapping("/contar-destinatarios")
    @ResponseBody
    public Integer contarDestinatarios(
            @RequestParam(value = "filtroSituacao", required = false) String filtroSituacao,
            @RequestParam(value = "filtroDataInicio", required = false) String filtroDataInicio,
            @RequestParam(value = "filtroDataFim", required = false) String filtroDataFim,
            HttpSession session) {

        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (instituicao == null) {
            return 0;
        }

        try {
            DisparoEmailBatch disparoTemp = new DisparoEmailBatch();
            disparoTemp.setInstituicao(instituicao);

            if (filtroSituacao != null && !filtroSituacao.isBlank()) {
                disparoTemp.setFiltroSituacaoUsuario(filtroSituacao);
            }

            if (filtroDataInicio != null && !filtroDataInicio.isBlank()) {
                disparoTemp.setFiltroDataInscricaoInicio(LocalDate.parse(filtroDataInicio));
            }

            if (filtroDataFim != null && !filtroDataFim.isBlank()) {
                disparoTemp.setFiltroDataInscricaoFim(LocalDate.parse(filtroDataFim));
            }

            return disparoService.contarDestinatarios(disparoTemp);

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Repetir disparo - Cria um novo disparo com os mesmos parâmetros e processa
     * imediatamente.
     */
    @PostMapping("/{id}/repetir")
    public String repetirDisparo(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        try {
            DisparoEmailBatch disparoOriginal = disparoService.obterDisparo(id);

            if (disparoOriginal == null || !disparoOriginal.getInstituicao().getId().equals(instituicao.getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Disparo não encontrado");
                return "redirect:/disparo-emails";
            }

            // Criar novo disparo com os mesmos dados
            DisparoEmailBatch novoDisparo = new DisparoEmailBatch();
            novoDisparo.setInstituicao(instituicao);
            novoDisparo.setUsuarioCriador((Usuario) usuarioLogado);
            novoDisparo.setTipoDisparo(disparoOriginal.getTipoDisparo());
            novoDisparo.setAssunto(disparoOriginal.getAssunto());
            novoDisparo.setCorpoHtml(disparoOriginal.getCorpoHtml());
            novoDisparo.setFiltroSituacaoUsuario(disparoOriginal.getFiltroSituacaoUsuario());
            novoDisparo.setFiltroDataInscricaoInicio(disparoOriginal.getFiltroDataInscricaoInicio());
            novoDisparo.setFiltroDataInscricaoFim(disparoOriginal.getFiltroDataInscricaoFim());
            novoDisparo.setFiltroNivelAcesso(disparoOriginal.getFiltroNivelAcesso());
            novoDisparo.setFiltroTipoAtividadeIds(disparoOriginal.getFiltroTipoAtividadeIds());
            novoDisparo.setSubInstituicao(disparoOriginal.getSubInstituicao());

            // Criar disparo (já define status PENDENTE e data criação)
            DisparoEmailBatch disparoSalvo = disparoService.criarDisparo(novoDisparo);

            // Processar imediatamente
            disparoService.processarDisparoAsync(disparoSalvo.getId());

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Disparo repetido com sucesso! ID: " + disparoSalvo.getId());
            return "redirect:/disparo-emails/" + disparoSalvo.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao repetir disparo: " + e.getMessage());
            return "redirect:/disparo-emails";
        }
    }

    /**
     * Copiar disparo - Redireciona para o formulário de novo disparo preenchido com
     * os dados do disparo original.
     */
    @GetMapping("/{id}/copiar")
    public String copiarDisparo(@PathVariable Long id, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");

        if (usuarioLogado == null || instituicao == null) {
            return "redirect:/acesso";
        }

        try {
            DisparoEmailBatch disparoOriginal = disparoService.obterDisparo(id);

            if (disparoOriginal == null || !disparoOriginal.getInstituicao().getId().equals(instituicao.getId())) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Disparo não encontrado");
                return "redirect:/disparo-emails";
            }

            // Criar objeto de disparo para o formulário
            DisparoEmailBatch disparoCopia = new DisparoEmailBatch();
            disparoCopia.setTipoDisparo(disparoOriginal.getTipoDisparo());
            disparoCopia.setAssunto(disparoOriginal.getAssunto() + " (Cópia)");
            disparoCopia.setCorpoHtml(disparoOriginal.getCorpoHtml());
            disparoCopia.setFiltroSituacaoUsuario(disparoOriginal.getFiltroSituacaoUsuario());
            disparoCopia.setFiltroDataInscricaoInicio(disparoOriginal.getFiltroDataInscricaoInicio());
            disparoCopia.setFiltroDataInscricaoFim(disparoOriginal.getFiltroDataInscricaoFim());
            disparoCopia.setFiltroNivelAcesso(disparoOriginal.getFiltroNivelAcesso());
            disparoCopia.setFiltroTipoAtividadeIds(disparoOriginal.getFiltroTipoAtividadeIds());
            disparoCopia.setSubInstituicao(disparoOriginal.getSubInstituicao());

            model.addAttribute("tiposDisparo", TipoDisparo.values());
            model.addAttribute("disparo", disparoCopia);
            model.addAttribute("mensagemInfo", "Editando cópia do disparo ID: " + id);

            return "disparo-emails-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao copiar disparo: " + e.getMessage());
            return "redirect:/disparo-emails";
        }
    }
}
