package com.agendademais.controllers;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller de Estatísticas de Usuários - Versão Final
 */
@Controller
@RequestMapping("/administrador")
public class EstatisticaUsuariosFinalController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatistica-usuarios-final")
    public String estatisticasUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            // Coleta todos os vínculos
            List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository.findAll();
            int totalUsuarios = (int) vinculos.stream().map(UsuarioInstituicao::getUsuario).distinct().count();
            int totalInstituicoes = (int) vinculos.stream().map(UsuarioInstituicao::getInstituicao).distinct().count();
            int totalVinculos = vinculos.size();

            // Estatística por nível de acesso
            Map<Integer, Long> porNivel = vinculos.stream()
                    .collect(Collectors.groupingBy(UsuarioInstituicao::getNivelAcessoUsuarioInstituicao,
                            Collectors.counting()));

            // Estatística por situação
            Map<String, Long> porSituacao = vinculos.stream()
                    .collect(Collectors.groupingBy(UsuarioInstituicao::getSitAcessoUsuarioInstituicao,
                            Collectors.counting()));

            // Prepara dados para o gráfico de pizza (níveis)
            List<String> nivelLabels = new ArrayList<>();
            List<Long> nivelCounts = new ArrayList<>();
            for (Integer nivel : porNivel.keySet()) {
                nivelLabels.add(getNivelTexto(nivel));
                nivelCounts.add(porNivel.get(nivel));
            }

            // Prepara dados para o gráfico de pizza (situação)
            List<String> situacaoLabels = new ArrayList<>();
            List<Long> situacaoCounts = new ArrayList<>();
            for (String sit : porSituacao.keySet()) {
                situacaoLabels.add(getStatusTexto(sit));
                situacaoCounts.add(porSituacao.get(sit));
            }

            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalInstituicoes", totalInstituicoes);
            model.addAttribute("totalVinculos", totalVinculos);
            model.addAttribute("nivelLabels", nivelLabels);
            model.addAttribute("nivelCounts", nivelCounts);
            model.addAttribute("situacaoLabels", situacaoLabels);
            model.addAttribute("situacaoCounts", situacaoCounts);
            model.addAttribute("mensagem", "Estatísticas atualizadas!");

            return "gestao-usuarios/estatistica-usuarios";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro crítico: " + e.getMessage());
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
