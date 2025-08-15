package com.agendademais.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agendademais.entities.Local;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador")
public class RelatorioUsuariosController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/relatorio-usuarios")
    public String mostrarRelatorio(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("*** DEBUG RelatorioUsuarios: INÍCIO ***");
            
            // Verificar sessão (igual ao EstatisticasFixedController)
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

            System.out.println("*** DEBUG: usuarioLogado = " + (usuarioLogado != null ? usuarioLogado.getUsername() : "null"));
            System.out.println("*** DEBUG: nivelAcesso = " + nivelAcesso);
            System.out.println("*** DEBUG: instituicaoSelecionada = " + (instituicaoSelecionada != null ? instituicaoSelecionada.getNomeInstituicao() : "null"));

            // Verificar permissões (igual ao GestaoUsuariosController)
            if (usuarioLogado == null || nivelAcesso == null) {
                System.out.println("*** DEBUG: Sessão inválida ***");
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirou. Faça login novamente.");
                return "redirect:/acesso";
            }
            
            if (nivelAcesso < 5) {
                System.out.println("*** DEBUG: Acesso negado ***");
                redirectAttributes.addFlashAttribute("mensagemErro", "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
                return "redirect:/acesso";
            }

            if (instituicaoSelecionada == null) {
                System.out.println("*** DEBUG: Instituição não selecionada ***");
                redirectAttributes.addFlashAttribute("mensagemErro", "Erro: Instituição não selecionada.");
                return "redirect:/acesso";
            }
            
            System.out.println("*** DEBUG: Coletando dados estatísticos ***");
            
            // Buscar dados da instituição selecionada
            List<UsuarioInstituicao> todasInstituicoes;

            if (nivelAcesso == 9) {
                System.out.println("*** DEBUG: SuperUsuário - todos da instituição ***");
                todasInstituicoes = usuarioInstituicaoRepository
                        .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
            } else {
                System.out.println("*** DEBUG: Administrador - níveis <= 5 ***");
                todasInstituicoes = usuarioInstituicaoRepository
                        .findByInstituicaoAndNivelAcessoUsuarioInstituicaoLessThanEqualOrderByNivelAcessoUsuarioInstituicaoAsc(
                                instituicaoSelecionada, 5);
            }

            System.out.println("*** DEBUG: Total de registros: " + todasInstituicoes.size() + " ***");
            
            // === ESTATÍSTICAS BÁSICAS ===
            long totalUsuarios = todasInstituicoes.stream()
                .map(ui -> ui.getUsuario())
                .distinct()
                .count();
            
            long totalInstituicoes = todasInstituicoes.stream()
                .map(ui -> ui.getInstituicao())
                .distinct()
                .count();
            
            long totalVinculos = todasInstituicoes.size();
            
            // === ESTATÍSTICAS POR NÍVEL ===
            Map<String, Long> usuariosPorNivel = todasInstituicoes.stream()
                .collect(Collectors.groupingBy(
                    ui -> getNivelTexto(ui.getNivelAcessoUsuarioInstituicao()),
                    Collectors.counting()
                ));
            
            // === ESTATÍSTICAS POR INSTITUIÇÃO ===
            Map<String, Long> usuariosPorInstituicao = todasInstituicoes.stream()
                .collect(Collectors.groupingBy(
                    ui -> ui.getInstituicao().getNomeInstituicao(),
                    Collectors.counting()
                ));
            
            // === ESTATÍSTICAS POR STATUS ===
            Map<String, Long> usuariosPorStatus = todasInstituicoes.stream()
                .collect(Collectors.groupingBy(
                    ui -> getStatusTexto(ui.getUsuario().getSituacaoUsuario()),
                    Collectors.counting()
                ));
            
            // === PREPARAR DADOS PARA CHARTS ===
            
            // Chart de Níveis
            List<String> niveisLabels = new ArrayList<>(usuariosPorNivel.keySet());
            List<Long> niveisValues = new ArrayList<>(usuariosPorNivel.values());
            
            // Chart de Instituições  
            List<String> instituicoesLabels = new ArrayList<>(usuariosPorInstituicao.keySet());
            List<Long> instituicoesValues = new ArrayList<>(usuariosPorInstituicao.values());
            
            // Chart de Status
            List<String> statusLabels = new ArrayList<>(usuariosPorStatus.keySet());
            List<Long> statusValues = new ArrayList<>(usuariosPorStatus.values());
            
            // === TOP 10 USUÁRIOS MAIS ATIVOS ===
            Map<Usuario, Long> contadorPorUsuario = todasInstituicoes.stream()
                .collect(Collectors.groupingBy(
                    UsuarioInstituicao::getUsuario,
                    Collectors.counting()
                ));
            
            List<Map.Entry<Usuario, Long>> topUsuarios = contadorPorUsuario.entrySet().stream()
                .sorted(Map.Entry.<Usuario, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
            
            // === ADICIONAR DADOS AO MODEL ===
            
            // Estatísticas básicas
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalInstituicoes", totalInstituicoes);
            model.addAttribute("totalVinculos", totalVinculos);
            
            // Charts - Níveis
            model.addAttribute("niveisLabels", niveisLabels);
            model.addAttribute("niveisValues", niveisValues);
            
            // Charts - Instituições
            model.addAttribute("instituicoesLabels", instituicoesLabels);
            model.addAttribute("instituicoesValues", instituicoesValues);
            
            // Charts - Status
            model.addAttribute("statusLabels", statusLabels);
            model.addAttribute("statusValues", statusValues);
            
            // Top usuários
            model.addAttribute("topUsuarios", topUsuarios);
            
            // Dados brutos para tabelas
            model.addAttribute("usuariosPorNivel", usuariosPorNivel);
            model.addAttribute("usuariosPorInstituicao", usuariosPorInstituicao);
            model.addAttribute("usuariosPorStatus", usuariosPorStatus);
            
            // Dados da sessão (igual aos outros controllers)
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);
            
            System.out.println("*** DEBUG: Dados preparados com sucesso ***");
            System.out.println("*** Total usuários: " + totalUsuarios + " ***");
            System.out.println("*** Total instituições: " + totalInstituicoes + " ***");
            System.out.println("*** Total vínculos: " + totalVinculos + " ***");
            
            return "gestao-usuarios/estatistica-usuarios";
            
        } catch (Exception e) {
            System.out.println("*** ERRO no RelatorioUsuariosController ***");
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("erro", "Erro ao carregar estatísticas: " + e.getMessage());
            return "gestao-usuarios/estatistica-usuarios";
        }
    }
    
    private String getNivelTexto(Integer nivel) {
        if (nivel == null) return "Não definido";
        switch (nivel) {
            case 1: return "Participante";
            case 2: return "Autor";
            case 5: return "Administrador";
            case 9: return "SuperUsuário";
            default: return "Nível " + nivel;
        }
    }
    
    private String getStatusTexto(String situacao) {
        if (situacao == null) return "Não definido";
        switch (situacao.toUpperCase()) {
            case "A": return "Ativo";
            case "B": return "Bloqueado";
            case "C": return "Cancelado";
            default: return "Status: " + situacao;
        }
    }
}
