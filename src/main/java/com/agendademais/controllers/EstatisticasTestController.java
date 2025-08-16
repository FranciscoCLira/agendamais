package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller TESTE para Estatísticas (sem autenticação)
 */
@Controller
@RequestMapping("/teste")
public class EstatisticasTestController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @GetMapping("/estatistica-usuarios")
    public String estatisticasTest(Model model) {

        try {
            System.out.println("*** DEBUG TESTE: Iniciando sem autenticação ***");

            // Dados fictícios para teste
            List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository.findAll();
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Instituicao> instituicoes = instituicaoRepository.findAll();

            // Estatísticas básicas
            model.addAttribute("totalUsuarios", usuarios.size());
            model.addAttribute("totalInstituicoes", instituicoes.size());
            model.addAttribute("totalVinculos", vinculos.size());

            // Dados para gráficos (exemplo)
            model.addAttribute("niveisLabels", Arrays.asList("Usuário", "Moderador", "Admin", "Super Admin"));
            model.addAttribute("niveisValues", Arrays.asList(10, 5, 3, 1));
            model.addAttribute("statusLabels", Arrays.asList("Ativo", "Inativo"));
            model.addAttribute("statusValues", Arrays.asList(15, 4));

            // Usuários por nível (simulado)
            Map<String, Long> usuariosPorNivel = new HashMap<>();
            usuariosPorNivel.put("Usuário Comum", 10L);
            usuariosPorNivel.put("Moderador", 5L);
            usuariosPorNivel.put("Admin Local", 3L);
            usuariosPorNivel.put("Super Admin", 1L);
            model.addAttribute("usuariosPorNivel", usuariosPorNivel);

            // Usuários por status (simulado)
            Map<String, Long> usuariosPorStatus = new HashMap<>();
            usuariosPorStatus.put("Ativo", 15L);
            usuariosPorStatus.put("Inativo", 4L);
            model.addAttribute("usuariosPorStatus", usuariosPorStatus);

            // Top 10 usuários (simulado)
            List<Map.Entry<Usuario, Long>> topUsuarios = new ArrayList<>();
            if (!usuarios.isEmpty()) {
                topUsuarios.add(new AbstractMap.SimpleEntry<>(usuarios.get(0), 5L));
            }
            model.addAttribute("topUsuarios", topUsuarios);

            System.out.println("*** DEBUG TESTE: Dados enviados ao template ***");
            System.out.println("Total Usuários: " + usuarios.size());
            System.out.println("Total Instituições: " + instituicoes.size());
            System.out.println("Total Vínculos: " + vinculos.size());

            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO NO CONTROLLER TESTE ***");
            e.printStackTrace();
            model.addAttribute("erro", "Erro ao carregar estatísticas: " + e.getMessage());
            return "gestao-usuarios/estatistica-usuarios";
        }
    }
}
