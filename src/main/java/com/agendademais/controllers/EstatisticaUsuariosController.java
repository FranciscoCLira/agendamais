package com.agendademais.controllers;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador")
public class EstatisticaUsuariosController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatistica-usuarios")
    public String estatisticaUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

            // Verificar permissões
            if (usuarioLogado == null || nivelAcesso == null) {
                // Sessão expirada - redirecionar para acesso com mensagem amigável
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirou. Faça login novamente.");
                return "redirect:/acesso";
            }
            
            if (nivelAcesso < 5) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
                return "redirect:/acesso";
            }

            // Buscar usuários da instituição selecionada
            List<UsuarioInstituicao> usuarios;
            if (nivelAcesso == 9) {
                usuarios = usuarioInstituicaoRepository.findAll();
            } else {
                usuarios = usuarioInstituicaoRepository.findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
            }

            // REMOVER O USUÁRIO LOGADO DA LISTA (ele acessa seus dados via "Meus Dados")
            usuarios.removeIf(usuarioInst -> usuarioInst.getUsuario().getId().equals(usuarioLogado.getId()));

            // Calcular estatísticas
            long totalUsuarios = usuarios.size();
            
            // Estatísticas hierárquicas por País/Estado/Cidade
            Map<String, Map<String, Map<String, Long>>> estatisticasHierarquicas = calcularEstatisticasHierarquicas(usuarios);
            
            // Estatísticas por sub-instituição (incluindo usuários sem sub-instituição)
            Map<String, Long> estatisticasSubInstituicao = usuarios.stream()
                .collect(Collectors.groupingBy(
                    ui -> {
                        try {
                            // Encontrar a sub-instituição da instituição atual
                            return ui.getUsuario().getPessoa().getPessoaSubInstituicao().stream()
                                .filter(psi -> psi.getInstituicao().getId().equals(instituicaoSelecionada.getId()))
                                .map(psi -> psi.getSubInstituicao() != null ? 
                                           psi.getSubInstituicao().getNomeSubInstituicao() : "Nenhuma")
                                .findFirst()
                                .orElse("Nenhuma");
                        } catch (Exception e) {
                            return "Nenhuma";
                        }
                    },
                    Collectors.counting()));

            // Estatísticas simples para gráficos
            Map<String, Long> estatisticasPais = usuarios.stream()
                .filter(ui -> ui.getUsuario().getPessoa().getNomePais() != null)
                .collect(Collectors.groupingBy(
                    ui -> ui.getUsuario().getPessoa().getNomePais(),
                    Collectors.counting()));

            // Adicionar ao modelo
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("estatisticasHierarquicas", estatisticasHierarquicas);
            model.addAttribute("estatisticasSubInstituicao", estatisticasSubInstituicao);
            model.addAttribute("estatisticasPais", estatisticasPais);
            model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
            model.addAttribute("nivelAcessoLogado", nivelAcesso);

            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO DETALHADO em estatisticaUsuarios ***");
            System.out.println("Tipo: " + e.getClass().getName());
            System.out.println("Mensagem: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao carregar estatísticas de usuários: " + e.getMessage());
            return "redirect:/administrador";
        }
    }

    /**
     * Calcula estatísticas hierárquicas por País → Estado → Cidade
     */
    private Map<String, Map<String, Map<String, Long>>> calcularEstatisticasHierarquicas(List<UsuarioInstituicao> usuarios) {
        return usuarios.stream()
            .filter(ui -> ui.getUsuario().getPessoa().getNomePais() != null)
            .collect(Collectors.groupingBy(
                ui -> ui.getUsuario().getPessoa().getNomePais() != null ? 
                      ui.getUsuario().getPessoa().getNomePais() : "Não informado",
                Collectors.groupingBy(
                    ui -> ui.getUsuario().getPessoa().getNomeEstado() != null ? 
                          ui.getUsuario().getPessoa().getNomeEstado() : "Não informado",
                    Collectors.groupingBy(
                        ui -> ui.getUsuario().getPessoa().getNomeCidade() != null ? 
                              ui.getUsuario().getPessoa().getNomeCidade() : "Não informado",
                        Collectors.counting()
                    )
                )
            ));
    }
}
