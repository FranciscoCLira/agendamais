package com.agendademais.controllers;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller de teste SEM autenticação
 */
@Controller
@RequestMapping("/teste")
public class EstatisticasTesteSemAuth {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatisticas-sem-auth")
    public String testeEstatisticas(Model model) {

        try {
            System.out.println("*** TESTE SEM AUTH: Iniciando ***");

            // Buscar todos os usuários para teste
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAll();
            System.out.println("*** TESTE: " + usuarios.size() + " usuários encontrados ***");

            if (usuarios.isEmpty()) {
                model.addAttribute("erro", "Nenhum dado encontrado no banco");
                model.addAttribute("totalUsuarios", 0);
                model.addAttribute("totalVinculos", 0);
                return "gestao-usuarios/estatistica-usuarios";
            }

            // === ESTATÍSTICAS BÁSICAS ===
            long totalUsuarios = usuarios.stream()
                    .map(ui -> ui.getUsuario())
                    .distinct()
                    .count();

            long totalInstituicoes = usuarios.stream()
                    .map(ui -> ui.getInstituicao())
                    .distinct()
                    .count();

            long totalVinculos = usuarios.size();

            // === ESTATÍSTICAS POR NÍVEL ===
            Map<String, Long> usuariosPorNivel = usuarios.stream()
                    .collect(Collectors.groupingBy(
                            ui -> getNivelTexto(ui.getNivelAcessoUsuarioInstituicao()),
                            Collectors.counting()));

            // === ESTATÍSTICAS POR STATUS ===
            Map<String, Long> usuariosPorStatus = usuarios.stream()
                    .collect(Collectors.groupingBy(
                            ui -> getStatusTexto(ui.getUsuario().getSituacaoUsuario()),
                            Collectors.counting()));

            // === PREPARAR DADOS PARA CHARTS ===

            // Chart de Níveis
            List<String> niveisLabels = new ArrayList<>(usuariosPorNivel.keySet());
            List<Long> niveisValues = new ArrayList<>(usuariosPorNivel.values());

            // Chart de Status
            List<String> statusLabels = new ArrayList<>(usuariosPorStatus.keySet());
            List<Long> statusValues = new ArrayList<>(usuariosPorStatus.values());

            // === ADICIONAR DADOS AO MODEL ===

            // Estatísticas básicas
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalInstituicoes", totalInstituicoes);
            model.addAttribute("totalVinculos", totalVinculos);

            // Charts - Níveis
            model.addAttribute("niveisLabels", niveisLabels);
            model.addAttribute("niveisValues", niveisValues);

            // Charts - Status
            model.addAttribute("statusLabels", statusLabels);
            model.addAttribute("statusValues", statusValues);

            // Dados brutos para tabelas
            model.addAttribute("usuariosPorNivel", usuariosPorNivel);
            model.addAttribute("usuariosPorStatus", usuariosPorStatus);

            // Dados fake para sessão
            model.addAttribute("nivelAcessoLogado", 9);
            model.addAttribute("mensagem", "Teste sem autenticação funcionando!");

            System.out.println("*** TESTE: Dados preparados com sucesso ***");
            System.out.println("*** Total usuários: " + totalUsuarios + " ***");
            System.out.println("*** Total vínculos: " + totalVinculos + " ***");

            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO no TesteEstatisticas ***");
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("erro", "Erro no teste: " + e.getMessage());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalVinculos", 0);
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
