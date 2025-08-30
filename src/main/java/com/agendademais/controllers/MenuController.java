package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MenuController {

    // Endpoints principais para cada nível de usuário
    @GetMapping("/participante")
    public String participante(HttpSession session, Model model) {
        System.out.println("*** MenuController.participante() - Entrada ***");
        System.out.println("*** Sessão ID: " + session.getId());
        System.out.println("*** usuarioLogado: " + (session.getAttribute("usuarioLogado") != null ? "existe" : "null"));
        System.out.println("*** instituicaoSelecionada: "
                + (session.getAttribute("instituicaoSelecionada") != null ? "existe" : "null"));
        System.out.println("*** nivelAcessoAtual: " + session.getAttribute("nivelAcessoAtual"));

        return "menus/menu-participante";
    }

    @GetMapping("/autor")
    public String autor() {
        return "menus/menu-autor";
    }

    @GetMapping("/superusuario")
    public String superusuario() {
        return "menus/menu-superusuario";
    }

    @GetMapping("/controle-total")
    public String controleTotal() {
        return "menus/menu-controle-total";
    }

    // === ENDPOINTS EM CONSTRUÇÃO PARA CONTROLE TOTAL ===

    @GetMapping("/gerenciar-instituicoes")
    public String gerenciarInstituicoes() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/gestao-usuarios")
    public String gestaoUsuarios() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/backup-restore")
    public String backupRestore() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/logs-sistema")
    public String logsSistema() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/configuracoes-avancadas")
    public String configuracoesAvancadas() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/migracao-dados")
    public String migracaoDados() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/modo-manutencao")
    public String modoManutencao() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/relatorios-gerais")
    public String relatoriosGerais() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/log-auditoria")
    public String logAuditoria() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/estatisticas-sistema")
    public String estatisticasSistema() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/vinculos-instituicoes")
    public String vinculosInstituicoes() {
        return "info/em-construcao"; // Página em construção
    }

    /**
     * Determina o tipo de usuário baseado no nível de acesso da sessão
     */
    private String determinaTipoUsuario(HttpSession session) {
        Integer nivel = (Integer) session.getAttribute("nivelAcessoAtual");

        if (nivel == null) {
            return "participante";
        }

        switch (nivel) {
            case 2:
                return "autor";
            case 5:
                return "administrador";
            case 9:
                return "superusuario";
            case 0:
                return "controle-total";
            default:
                return "participante";
        }
    }

    // Endpoints para funcionalidades do administrador
    @GetMapping("/administrador/instituicoes")
    public String administradorInstituicoes() {
        return "info/em-construcao";
    }

    @GetMapping("/administrador/subinstituicoes")
    public String gerenciarSubInstituicoes() {
        return "info/em-construcao";
    }

    // @GetMapping("/administrador/atividades")
    // public String gerenciarAtividades() {
    // return "redirect:/atividades";
    // }


    @GetMapping("/administrador/postagens")
    public String gerenciarPostagens() {
        return "info/em-construcao";
    }

    @GetMapping("/administrador/log-postagens")
    public String logPostagens() {
        return "info/em-construcao";
    }

    // === ENDPOINTS EM CONSTRUÇÃO PARA SUPERUSUARIO ===

    @GetMapping("/superusuario/backup")
    public String backupInstituicao() {
        return "info/em-construcao"; // Página em construção
    }

    @GetMapping("/superusuario/logs")
    public String logsInstituicao() {
        return "info/em-construcao"; // Página em construção
    }

    // Endpoint de teste para debug AJAX
    @GetMapping("/teste-ajax")
    public String testeAjax() {
        return "teste-ajax";
    }
}
