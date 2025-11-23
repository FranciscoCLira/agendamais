package com.agendademais.controller;

import com.agendademais.dto.*;
import com.agendademais.entities.Instituicao;
import com.agendademais.service.InscricaoMassivaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller para carga massiva de inscrições em tipos de atividade
 * Acessível para administradores (nivelAcessoAtual >= 5)
 */
@Controller
@RequestMapping("/administrador/inscricao-massiva")
public class InscricaoMassivaController {

    @Autowired
    private InscricaoMassivaService inscricaoMassivaService;

    /**
     * Exibe formulário de carga massiva de inscrições
     */
    @GetMapping
    public String formulario(Model model, HttpSession session) {
        // Verifica se usuário está logado e é administrador (nível >= 5)
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        if (nivelAcesso == null || nivelAcesso > 5) {
            return "redirect:/acesso";
        }

        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            return "redirect:/acesso";
        }

        model.addAttribute("pageTitle", "Carga Massiva de Inscrições");
        model.addAttribute("instituicao", instituicaoSelecionada);
        
        return "administrador/inscricao-massiva";
    }

    /**
     * Valida arquivo antes do processamento (retorna apenas estatísticas e erros)
     */
    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<InscricaoMassivaResponse> validarArquivo(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("subInstituicaoId") Long subInstituicaoId,
            @RequestParam("tipoAtividadeId") Long tipoAtividadeId,
            HttpSession session) {
        
        try {
            // Verifica acesso
            Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
            if (nivelAcesso == null || nivelAcesso > 5) {
                InscricaoMassivaResponse response = new InscricaoMassivaResponse();
                response.addError("Acesso negado");
                return ResponseEntity.status(403).body(response);
            }

            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
            if (instituicaoSelecionada == null) {
                InscricaoMassivaResponse response = new InscricaoMassivaResponse();
                response.addError("Instituição não selecionada");
                return ResponseEntity.status(400).body(response);
            }

            // Cria request apenas para validação (não processa)
            InscricaoMassivaRequest request = new InscricaoMassivaRequest();
            request.setArquivo(arquivo);
            request.setSubInstituicaoId(subInstituicaoId);
            request.setTipoAtividadeId(tipoAtividadeId);
            request.setTipoCarga("validacao"); // Modo validação apenas
            request.setGerarArquivoResultado(false);

            // Chama service apenas para validar
            InscricaoMassivaResponse response = inscricaoMassivaService.validarArquivo(
                request, 
                instituicaoSelecionada.getId()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            InscricaoMassivaResponse response = new InscricaoMassivaResponse();
            response.addError("Erro ao validar arquivo: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Processa upload e carga massiva de inscrições
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<InscricaoMassivaResponse> uploadFile(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("subInstituicaoId") Long subInstituicaoId,
            @RequestParam("tipoAtividadeId") Long tipoAtividadeId,
            @RequestParam(value = "tipoCarga", defaultValue = "producao") String tipoCarga,
            @RequestParam(value = "gerarArquivoResultado", defaultValue = "true") boolean gerarArquivoResultado,
            HttpSession session) {

        // Verifica se usuário está logado e é administrador
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        if (nivelAcesso == null || nivelAcesso > 5) {
            InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
            errorResponse.addError("Acesso negado. Apenas administradores podem realizar esta operação.");
            return ResponseEntity.status(403).body(errorResponse);
        }

        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
            errorResponse.addError("Instituição não selecionada");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Valida arquivo
            if (arquivo.isEmpty()) {
                InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
                errorResponse.addError("Arquivo não informado");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Cria request
            InscricaoMassivaRequest request = new InscricaoMassivaRequest();
            request.setArquivo(arquivo);
            request.setSubInstituicaoId(subInstituicaoId);
            request.setTipoAtividadeId(tipoAtividadeId);
            request.setTipoCarga(tipoCarga);
            request.setGerarArquivoResultado(gerarArquivoResultado);

            // Processa a carga
            InscricaoMassivaResponse response = inscricaoMassivaService.processarCargaInscricoes(
                    request, instituicaoSelecionada.getId());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
            errorResponse.addError("Erro durante processamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Reverte uma carga massiva de inscrições (exclui registros criados)
     * EXCEÇÃO: NÃO exclui entidades Local
     */
    @PostMapping("/reverter")
    @ResponseBody
    public ResponseEntity<InscricaoMassivaResponse> reverterCarga(
            @RequestBody InscricaoMassivaResponse dadosCarga,
            HttpSession session) {

        // Verifica se usuário está logado e é administrador
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        if (nivelAcesso == null || nivelAcesso < 5) {
            InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
            errorResponse.addError("Acesso negado. Apenas administradores podem realizar esta operação.");
            return ResponseEntity.status(403).body(errorResponse);
        }

        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
            errorResponse.addError("Instituição não selecionada");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Valida dados
            if (dadosCarga.getInscricoesIds() == null || dadosCarga.getInscricoesIds().isEmpty()) {
                InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
                errorResponse.addError("Nenhuma inscrição para reverter");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Chama service para reverter
            InscricaoMassivaResponse response = inscricaoMassivaService.reverterCarga(
                dadosCarga.getInscricoesIds(),
                dadosCarga.getUsuariosIds(),
                dadosCarga.getPessoasIds()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            InscricaoMassivaResponse errorResponse = new InscricaoMassivaResponse();
            errorResponse.addError("Erro durante reversão: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
