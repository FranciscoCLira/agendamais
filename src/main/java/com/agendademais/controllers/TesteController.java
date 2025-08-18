package com.agendademais.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/administrador")
public class TesteController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/teste-simples")
    public String testeSimples(Model model) {
        System.out.println("*** TESTE SIMPLES EXECUTADO ***");
        model.addAttribute("mensagem", "Teste simples funcionando!");
        return "gestao-usuarios/teste-simples";
    }

    @GetMapping("/teste-estatisticas")
    public String testeEstatisticas(Model model) {
        System.out.println("*** TESTE ESTATÍSTICAS EXECUTADO ***");
        try {
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAll();
            System.out.println("Total de usuários encontrados: " + usuarios.size());

            // Teste simples de agrupamento
            Map<String, Long> estatisticasPais = usuarios.stream()
                    .filter(ui -> ui != null && ui.getUsuario() != null &&
                            ui.getUsuario().getPessoa() != null &&
                            ui.getUsuario().getPessoa().getNomePais() != null)
                    .collect(Collectors.groupingBy(
                            ui -> ui.getUsuario().getPessoa().getNomePais(),
                            Collectors.counting()));

            System.out.println("Estatísticas por país: " + estatisticasPais);

            model.addAttribute("totalUsuarios", usuarios.size());
            model.addAttribute("estatisticasPais", estatisticasPais);
            model.addAttribute("mensagem", "Teste de estatísticas funcionando!");

        } catch (Exception e) {
            System.out.println("ERRO no teste: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("mensagem", "Erro: " + e.getMessage());
        }
        return "gestao-usuarios/teste-simples";
    }
}
