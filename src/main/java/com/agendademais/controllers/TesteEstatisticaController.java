package com.agendademais.controllers;

import java.util.List;
import java.util.Map;
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
public class TesteEstatisticaController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatistica-basica")
    public String estatisticaBasica(Model model) {

        System.out.println("*** TESTE ESTATÍSTICA BÁSICA ***");

        try {
            // Buscar todos os usuários (teste sem filtro de instituição)
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAll();
            System.out.println("Total de usuários encontrados: " + usuarios.size());

            // Calcular estatísticas básicas
            long totalUsuarios = usuarios.size();

            // Estatísticas simples por país (com proteção contra nulos)
            Map<String, Long> estatisticasPais = usuarios.stream()
                    .filter(ui -> ui != null && ui.getUsuario() != null &&
                            ui.getUsuario().getPessoa() != null &&
                            ui.getUsuario().getPessoa().getNomePais() != null)
                    .collect(Collectors.groupingBy(
                            ui -> ui.getUsuario().getPessoa().getNomePais(),
                            Collectors.counting()));

            System.out.println("Estatísticas por país: " + estatisticasPais);

            // Adicionar ao modelo
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("estatisticasPais", estatisticasPais);
            model.addAttribute("teste", "Teste de Estatísticas Básicas");

            return "gestao-usuarios/teste-estatistica";

        } catch (Exception e) {
            System.out.println("*** ERRO no teste de estatísticas ***");
            System.out.println("Tipo: " + e.getClass().getName());
            System.out.println("Mensagem: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("erro", "Erro: " + e.getMessage());
            model.addAttribute("teste", "Teste com erro");
            return "gestao-usuarios/teste-estatistica";
        }
    }
}
