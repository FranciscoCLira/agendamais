package com.agendademais.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

@Controller
@RequestMapping("/teste")
public class RelatorioUsuariosTestController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatisticas")
    public String testeEstatisticas(Model model) {
        try {
            System.out.println("*** TESTE: Iniciando sem autenticação ***");

            // Buscar todos os dados para teste
            List<UsuarioInstituicao> todasInstituicoes = usuarioInstituicaoRepository.findAll();
            System.out.println("*** TESTE: Total de registros: " + todasInstituicoes.size() + " ***");

            if (todasInstituicoes.isEmpty()) {
                model.addAttribute("erro", "Nenhum dado encontrado no sistema.");
                return "gestao-usuarios/estatistica-usuarios";
            }

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
                            Collectors.counting()));

            // === ESTATÍSTICAS POR INSTITUIÇÃO ===
            Map<String, Long> usuariosPorInstituicao = todasInstituicoes.stream()
                    .collect(Collectors.groupingBy(
                            ui -> ui.getInstituicao().getNomeInstituicao(),
                            Collectors.counting()));

            // === ESTATÍSTICAS POR STATUS ===
            Map<String, Long> usuariosPorStatus = todasInstituicoes.stream()
                    .collect(Collectors.groupingBy(
                            ui -> getStatusTexto(ui.getUsuario().getSituacaoUsuario()),
                            Collectors.counting()));

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
            Map<com.agendademais.entities.Usuario, Long> contadorPorUsuario = todasInstituicoes.stream()
                    .collect(Collectors.groupingBy(
                            UsuarioInstituicao::getUsuario,
                            Collectors.counting()));

            List<Map.Entry<com.agendademais.entities.Usuario, Long>> topUsuarios = contadorPorUsuario.entrySet()
                    .stream()
                    .sorted(Map.Entry.<com.agendademais.entities.Usuario, Long>comparingByValue().reversed())
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

            // Dados fake para o template
            model.addAttribute("nivelAcessoLogado", 9);

            System.out.println("*** TESTE: Dados preparados com sucesso ***");
            System.out.println("*** Total usuários: " + totalUsuarios + " ***");
            System.out.println("*** Total instituições: " + totalInstituicoes + " ***");
            System.out.println("*** Total vínculos: " + totalVinculos + " ***");

            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO no TesteEstatisticas ***");
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("erro", "Erro no teste: " + e.getMessage());
            return "gestao-usuarios/estatistica-usuarios";
        }
    }

    private String getNivelTexto(Integer nivel) {
        if (nivel == null)
            return "Não definido";
        switch (nivel) {
            case 1:
                return "Participante";
            case 2:
                return "Autor";
            case 5:
                return "Administrador";
            case 9:
                return "SuperUsuário";
            default:
                return "Nível " + nivel;
        }
    }

    private String getStatusTexto(String situacao) {
        if (situacao == null)
            return "Não definido";
        switch (situacao.toUpperCase()) {
            case "A":
                return "Ativo";
            case "B":
                return "Bloqueado";
            case "C":
                return "Cancelado";
            default:
                return "Status: " + situacao;
        }
    }
}
