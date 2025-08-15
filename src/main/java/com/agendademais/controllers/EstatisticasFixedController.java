package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.entities.PessoaSubInstituicao;
import com.agendademais.entities.Local;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.PessoaSubInstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller SIMPLIFICADO de Estatísticas de Usuários
 */
@Controller
@RequestMapping("/administrador")
public class EstatisticasFixedController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;
    
    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    @GetMapping("/estatistica-usuarios")
    public String estatisticasFixed(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("*** DEBUG FIXED: Iniciando ***");
            
            // Verificar sessão
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");

            System.out.println("*** DEBUG FIXED: usuarioLogado = " + (usuarioLogado != null ? usuarioLogado.getUsername() : "null"));
            System.out.println("*** DEBUG FIXED: nivelAcesso = " + nivelAcesso);
            System.out.println("*** DEBUG FIXED: instituicaoSelecionada = " + (instituicaoSelecionada != null ? instituicaoSelecionada.getNomeInstituicao() : "null"));

            // Verificar permissões (igual ao GestaoUsuariosController)
            if (usuarioLogado == null || nivelAcesso == null) {
                System.out.println("*** DEBUG FIXED: Sessão inválida ***");
                redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirou. Faça login novamente.");
                return "redirect:/acesso";
            }
            
            if (nivelAcesso < 5) {
                System.out.println("*** DEBUG FIXED: Acesso negado ***");
                redirectAttributes.addFlashAttribute("mensagemErro", "Acesso negado. Funcionalidade disponível apenas para Administradores e SuperUsuários.");
                return "redirect:/acesso";
            }

            if (instituicaoSelecionada == null) {
                System.out.println("*** DEBUG FIXED: Instituição não selecionada ***");
                redirectAttributes.addFlashAttribute("mensagemErro", "Erro: Instituição não selecionada.");
                return "redirect:/acesso";
            }

            System.out.println("*** DEBUG FIXED: Permissões OK ***");
            
            // === BUSCAR DADOS E PROCESSAR ESTATÍSTICAS ===
            try {
                System.out.println("*** DEBUG FIXED: Buscando usuários da instituição... ***");
                System.out.println("*** DEBUG FIXED: Instituição ID: " + instituicaoSelecionada.getId() + " ***");
                System.out.println("*** DEBUG FIXED: Instituição Nome: " + instituicaoSelecionada.getNomeInstituicao() + " ***");
                
                List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicaoSelecionada);
                System.out.println("*** DEBUG FIXED: " + usuarios.size() + " usuários encontrados ***");
                
                // Processar estatísticas com sessão real
                return processarEstatisticasComSessao(usuarios, model, usuarioLogado, instituicaoSelecionada, nivelAcesso);
                
            } catch (Exception e) {
                System.out.println("*** ERRO AO BUSCAR USUÁRIOS: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao carregar estatísticas: " + e.getMessage());
                return "redirect:/administrador";
            }
        } catch (Exception e) {
            System.out.println("*** DEBUG FIXED: ERRO: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao carregar estatísticas: " + e.getMessage());
            return "redirect:/administrador";
        }
    }
    
    /**
     * Método para processar estatísticas com sessão real
     */
    private String processarEstatisticasComSessao(List<UsuarioInstituicao> usuarios, Model model, Usuario usuarioLogado, Instituicao instituicao, Integer nivelAcesso) {
        System.out.println("*** DEBUG: Processando estatísticas com sessão real para " + usuarios.size() + " usuários ***");
        
        long totalUsuarios = usuarios.size();
        
        // === CALCULAR NÚMERO DE SUB-INSTITUIÇÕES ÚNICAS (não usuários) ===
        long usuariosComSubInstituicoes = 0; // Na verdade, será o número de sub-instituições únicas
        long usuariosSemSubInstituicoes = 0;
        
        try {
            // Buscar todas as sub-instituições únicas da instituição
            Set<Long> subInstituicoesUnicas = new HashSet<>();
            
            for (UsuarioInstituicao ui : usuarios) {
                if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                    Long pessoaId = ui.getUsuario().getPessoa().getId();
                    
                    // Buscar todas as sub-instituições desta pessoa
                    List<PessoaSubInstituicao> subInstituicoesPessoa = pessoaSubInstituicaoRepository.findByPessoaId(pessoaId);
                    for (PessoaSubInstituicao psi : subInstituicoesPessoa) {
                        if (psi.getSubInstituicao() != null) {
                            subInstituicoesUnicas.add(psi.getSubInstituicao().getId());
                        }
                    }
                }
            }
            
            usuariosComSubInstituicoes = subInstituicoesUnicas.size(); // Número de sub-instituições únicas
            usuariosSemSubInstituicoes = totalUsuarios - subInstituicoesUnicas.size(); // Resto dos usuários
            
            System.out.println("*** DEBUG SUB-INSTITUIÇÕES ÚNICAS: Total = " + usuariosComSubInstituicoes + " ***");
            
            // Debug detalhado dos primeiros usuários
            for (int i = 0; i < Math.min(usuarios.size(), 4); i++) {
                UsuarioInstituicao ui = usuarios.get(i);
                if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                    Long pessoaId = ui.getUsuario().getPessoa().getId();
                    boolean temSub = pessoaSubInstituicaoRepository.findFirstByPessoaId(pessoaId).isPresent();
                    System.out.println("*** DEBUG: PessoaID=" + pessoaId + " tem sub-instituição: " + temSub + " ***");
                }
            }
            
        } catch (Exception e) {
            System.out.println("*** ERRO ao calcular sub-instituições: " + e.getMessage());
            // Se houver erro, considerar todos sem sub-instituições
            usuariosSemSubInstituicoes = totalUsuarios;
            usuariosComSubInstituicoes = 0;
        }
        
        // === ESTATÍSTICAS POR SUB-INSTITUIÇÃO (filtrado por instituição) ===
        Map<String, Long> usuariosPorSubInstituicao = new HashMap<>();
        
        try {
            Long instituicaoId = instituicao != null ? instituicao.getId() : null;
            System.out.println("*** DEBUG: Processando sub-instituições para instituição ID: " + instituicaoId + " ***");
            
            for (UsuarioInstituicao ui : usuarios) {
                if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                    Long pessoaId = ui.getUsuario().getPessoa().getId();
                    
                    // Buscar todas as sub-instituições da pessoa
                    var vinculos = pessoaSubInstituicaoRepository.findByPessoa(ui.getUsuario().getPessoa());
                    
                    boolean temSubInstituicaoDaInstituicao = false;
                    
                    // Filtrar apenas sub-instituições da instituição específica
                    for (var vinculo : vinculos) {
                        if (vinculo.getSubInstituicao() != null && 
                            vinculo.getSubInstituicao().getInstituicao() != null) {
                            
                            Long subInstInstituicaoId = vinculo.getSubInstituicao().getInstituicao().getId();
                            
                            // Só contar se for da instituição específica (ou todas se não especificada)
                            if (instituicaoId == null || subInstInstituicaoId.equals(instituicaoId)) {
                                String nomeSubInst = vinculo.getSubInstituicao().getNomeSubInstituicao();
                                usuariosPorSubInstituicao.put(nomeSubInst, 
                                    usuariosPorSubInstituicao.getOrDefault(nomeSubInst, 0L) + 1);
                                temSubInstituicaoDaInstituicao = true;
                            }
                        }
                    }
                    
                    // Se não tem sub-instituição da instituição específica
                    if (!temSubInstituicaoDaInstituicao) {
                        usuariosPorSubInstituicao.put("Sem Sub-Instituição", 
                            usuariosPorSubInstituicao.getOrDefault("Sem Sub-Instituição", 0L) + 1);
                    }
                }
            }
            
            System.out.println("*** DEBUG: Estatísticas por sub-instituição (instituição " + instituicaoId + "): " + usuariosPorSubInstituicao + " ***");
            
            // DEBUG específico para contagem de sub-instituições ativas
            long contadorSubInstituicoesAtivas = usuariosPorSubInstituicao.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("Sem Sub-Instituição") && 
                                !entry.getKey().equals("Erro no processamento"))
                .count();
            System.out.println("*** DEBUG CONTAGEM: Sub-Instituições Ativas = " + contadorSubInstituicoesAtivas + " ***");
            
            // Usar o valor correto para exibição na tela
            usuariosComSubInstituicoes = contadorSubInstituicoesAtivas;
            
        } catch (Exception e) {
            System.out.println("*** ERRO ao processar sub-instituições: " + e.getMessage());
            usuariosPorSubInstituicao.put("Erro no processamento", 0L);
        }
        
        // === PROCESSAMENTO DE LOCALIZAÇÃO HIERÁRQUICA ===
        Map<String, Long> usuariosPorPais = new HashMap<>();
        Map<String, Long> usuariosPorEstado = new HashMap<>(); 
        Map<String, Long> usuariosPorCidade = new HashMap<>();
        
        // Mapas hierárquicos mais detalhados
        Map<String, Map<String, Long>> estadosPorPais = new HashMap<>();
        Map<String, Map<String, Long>> cidadesPorEstado = new HashMap<>();
        
        try {
            for (UsuarioInstituicao ui : usuarios) {
                try {
                    if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                        Usuario usuario = ui.getUsuario();
                        
                        // Local da cidade
                        Local cidade = usuario.getPessoa().getCidade();
                        if (cidade != null) {
                            String nomeCidade = cidade.getNomeLocal();
                            
                            // Estado (pai da cidade)
                            Local estado = cidade.getLocalPai();
                            if (estado != null) {
                                String nomeEstado = estado.getNomeLocal();
                                
                                // País (pai do estado)
                                Local pais = estado.getLocalPai();
                                if (pais != null) {
                                    String nomePais = pais.getNomeLocal();
                                    
                                    // === CONTADORES SIMPLES ===
                                    usuariosPorPais.put(nomePais, usuariosPorPais.getOrDefault(nomePais, 0L) + 1);
                                    
                                    // === CONTADORES HIERÁRQUICOS ===
                                    
                                    // País/Estado
                                    String chaveEstado = nomePais + " / " + nomeEstado;
                                    usuariosPorEstado.put(chaveEstado, usuariosPorEstado.getOrDefault(chaveEstado, 0L) + 1);
                                    
                                    // País/Estado/Cidade
                                    String chaveCidade = nomePais + " / " + nomeEstado + " / " + nomeCidade;
                                    usuariosPorCidade.put(chaveCidade, usuariosPorCidade.getOrDefault(chaveCidade, 0L) + 1);
                                    
                                    // === ESTRUTURAS PARA FILTROS ===
                                    
                                    // Estados por país
                                    estadosPorPais.computeIfAbsent(nomePais, k -> new HashMap<>())
                                        .put(nomeEstado, estadosPorPais.get(nomePais).getOrDefault(nomeEstado, 0L) + 1);
                                    
                                    // Cidades por estado (com chave País/Estado)
                                    cidadesPorEstado.computeIfAbsent(chaveEstado, k -> new HashMap<>())
                                        .put(nomeCidade, cidadesPorEstado.get(chaveEstado).getOrDefault(nomeCidade, 0L) + 1);
                                        
                                } else {
                                    // Estado sem país
                                    String chaveEstado = "País não informado / " + nomeEstado;
                                    usuariosPorEstado.put(chaveEstado, usuariosPorEstado.getOrDefault(chaveEstado, 0L) + 1);
                                    
                                    if (nomeCidade != null) {
                                        String chaveCidade = "País não informado / " + nomeEstado + " / " + nomeCidade;
                                        usuariosPorCidade.put(chaveCidade, usuariosPorCidade.getOrDefault(chaveCidade, 0L) + 1);
                                    }
                                }
                            } else if (nomeCidade != null) {
                                // Cidade sem estado
                                String chaveCidade = "País não informado / Estado não informado / " + nomeCidade;
                                usuariosPorCidade.put(chaveCidade, usuariosPorCidade.getOrDefault(chaveCidade, 0L) + 1);
                            }
                        } else {
                            // Usuários sem localização definida
                            usuariosPorPais.put("Não informado", usuariosPorPais.getOrDefault("Não informado", 0L) + 1);
                            usuariosPorEstado.put("Não informado / Não informado", usuariosPorEstado.getOrDefault("Não informado / Não informado", 0L) + 1);
                            usuariosPorCidade.put("Não informado / Não informado / Não informado", usuariosPorCidade.getOrDefault("Não informado / Não informado / Não informado", 0L) + 1);
                        }
                    } else {
                        usuariosPorPais.put("Dados incompletos", usuariosPorPais.getOrDefault("Dados incompletos", 0L) + 1);
                        usuariosPorEstado.put("Dados incompletos / Dados incompletos", usuariosPorEstado.getOrDefault("Dados incompletos / Dados incompletos", 0L) + 1);
                        usuariosPorCidade.put("Dados incompletos / Dados incompletos / Dados incompletos", usuariosPorCidade.getOrDefault("Dados incompletos / Dados incompletos / Dados incompletos", 0L) + 1);
                    }
                } catch (Exception e) {
                    System.out.println("*** ERRO ao processar usuário individual: " + e.getMessage());
                    // Continuar processamento mesmo com erro
                }
            }
        } catch (Exception e) {
            System.out.println("*** ERRO GERAL ao processar localização: " + e.getMessage());
            e.printStackTrace();
            // Definir valores padrão em caso de erro
            usuariosPorPais.put("Erro no processamento", 0L);
            usuariosPorEstado.put("Erro no processamento / Erro no processamento", 0L);
            usuariosPorCidade.put("Erro no processamento / Erro no processamento / Erro no processamento", 0L);
        }
        
        long totalPaises = usuariosPorPais.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("Não informado") && 
                           !entry.getKey().equals("Dados incompletos") && 
                           !entry.getKey().equals("Erro no processamento"))
            .count();
        
        System.out.println("*** DEBUG: Processamento concluído - Países válidos: " + totalPaises + " (total com todos: " + usuariosPorPais.size() + ")");
        System.out.println("*** DEBUG: Países encontrados: " + usuariosPorPais.keySet());
        System.out.println("*** DEBUG: Estados: " + usuariosPorEstado.size() + ", Cidades: " + usuariosPorCidade.size() + " ***");
        
        // === ADICIONAR DADOS AO MODEL ===
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosComSubInstituicoes", usuariosComSubInstituicoes);
        model.addAttribute("usuariosSemSubInstituicoes", usuariosSemSubInstituicoes);
        model.addAttribute("totalPaises", totalPaises);
        
        // === NOVA: Estatísticas por sub-instituição ===
        model.addAttribute("usuariosPorSubInstituicao", usuariosPorSubInstituicao);
        
        // Estatísticas por localização (hierárquicas)
        model.addAttribute("usuariosPorPais", usuariosPorPais);
        model.addAttribute("usuariosPorEstado", usuariosPorEstado);
        model.addAttribute("usuariosPorCidade", usuariosPorCidade);
        
        // Estruturas para filtros
        model.addAttribute("estadosPorPais", estadosPorPais);
        model.addAttribute("cidadesPorEstado", cidadesPorEstado);
        
        // Lista de países para filtros
        model.addAttribute("listaPaises", new ArrayList<>(usuariosPorPais.keySet()));
        
        // Dados da sessão reais
        String nomeUsuario = usuarioLogado.getPessoa() != null ? usuarioLogado.getPessoa().getNomePessoa() : usuarioLogado.getUsername();
        model.addAttribute("usuarioLogado", nomeUsuario);
        model.addAttribute("instituicaoSelecionada", instituicao.getNomeInstituicao());
        model.addAttribute("nivelAcessoAtual", nivelAcesso);
        
        System.out.println("*** DEBUG: Todos os dados adicionados ao model - retornando template ***");
        return "gestao-usuarios/estatistica-usuarios-simples";
    }
    
    /**
     * Método centralizado para processar estatísticas (versão para bypass)
     */
    private String processarEstatisticas(List<UsuarioInstituicao> usuarios, Model model, Long instituicaoId, String nomeInstituicao) {
        System.out.println("*** DEBUG: Processando estatísticas para " + usuarios.size() + " usuários ***");
        
        long totalUsuarios = usuarios.size();
        
        // === CALCULAR ESTATÍSTICAS DE SUB-INSTITUIÇÕES (usando dados reais) ===
        long usuariosComSubInstituicoes = 0;
        long usuariosSemSubInstituicoes = 0;
        
        try {
            for (UsuarioInstituicao ui : usuarios) {
                if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                    Long pessoaId = ui.getUsuario().getPessoa().getId();
                    
                    // Verificar se a pessoa tem vínculos com sub-instituições usando o repositório real
                    boolean temSubInstituicao = pessoaSubInstituicaoRepository.findFirstByPessoaId(pessoaId).isPresent();
                    
                    if (temSubInstituicao) {
                        usuariosComSubInstituicoes++;
                    } else {
                        usuariosSemSubInstituicoes++;
                    }
                } else {
                    usuariosSemSubInstituicoes++;
                }
            }
            
            System.out.println("*** DEBUG SUB-INSTITUIÇÕES (sem sessão): Com = " + usuariosComSubInstituicoes + ", Sem = " + usuariosSemSubInstituicoes + " ***");
            
        } catch (Exception e) {
            System.out.println("*** ERRO ao calcular sub-instituições: " + e.getMessage());
            usuariosSemSubInstituicoes = totalUsuarios;
            usuariosComSubInstituicoes = 0;
        }
        
        // === ESTATÍSTICAS POR SUB-INSTITUIÇÃO (sem sessão - filtrado por instituição ID=1) ===
        Map<String, Long> usuariosPorSubInstituicao = new HashMap<>();
        
        try {
            Long instituicaoIdFiltro = 1L; // Usar instituição ID=1 para debug
            System.out.println("*** DEBUG: Processando sub-instituições para instituição ID: " + instituicaoIdFiltro + " (modo debug) ***");
            
            for (UsuarioInstituicao ui : usuarios) {
                if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                    // Buscar todas as sub-instituições da pessoa
                    var vinculos = pessoaSubInstituicaoRepository.findByPessoa(ui.getUsuario().getPessoa());
                    
                    boolean temSubInstituicaoDaInstituicao = false;
                    
                    // Filtrar apenas sub-instituições da instituição específica
                    for (var vinculo : vinculos) {
                        if (vinculo.getSubInstituicao() != null && 
                            vinculo.getSubInstituicao().getInstituicao() != null) {
                            
                            Long subInstInstituicaoId = vinculo.getSubInstituicao().getInstituicao().getId();
                            
                            // Só contar se for da instituição ID=1
                            if (subInstInstituicaoId.equals(instituicaoIdFiltro)) {
                                String nomeSubInst = vinculo.getSubInstituicao().getNomeSubInstituicao();
                                usuariosPorSubInstituicao.put(nomeSubInst, 
                                    usuariosPorSubInstituicao.getOrDefault(nomeSubInst, 0L) + 1);
                                temSubInstituicaoDaInstituicao = true;
                            }
                        }
                    }
                    
                    // Se não tem sub-instituição da instituição específica
                    if (!temSubInstituicaoDaInstituicao) {
                        usuariosPorSubInstituicao.put("Sem Sub-Instituição", 
                            usuariosPorSubInstituicao.getOrDefault("Sem Sub-Instituição", 0L) + 1);
                    }
                }
            }
            
            System.out.println("*** DEBUG: Estatísticas por sub-instituição (instituição " + instituicaoIdFiltro + "): " + usuariosPorSubInstituicao + " ***");
            
            // DEBUG específico para contagem de sub-instituições ativas
            long contadorSubInstituicoesAtivas = usuariosPorSubInstituicao.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("Sem Sub-Instituição") && 
                                !entry.getKey().equals("Erro no processamento"))
                .count();
            System.out.println("*** DEBUG CONTAGEM (sem sessão): Sub-Instituições Ativas = " + contadorSubInstituicoesAtivas + " ***");
            
            // Usar o valor correto para exibição na tela
            usuariosComSubInstituicoes = contadorSubInstituicoesAtivas;
            
        } catch (Exception e) {
            System.out.println("*** ERRO ao processar sub-instituições (sem sessão): " + e.getMessage());
            usuariosPorSubInstituicao.put("Erro no processamento", 0L);
        }
        
        // === PROCESSAMENTO DE LOCALIZAÇÃO HIERÁRQUICA ===
        Map<String, Long> usuariosPorPais = new HashMap<>();
        Map<String, Long> usuariosPorEstado = new HashMap<>(); 
        Map<String, Long> usuariosPorCidade = new HashMap<>();
        
        // Mapas hierárquicos mais detalhados
        Map<String, Map<String, Long>> estadosPorPais = new HashMap<>();
        Map<String, Map<String, Long>> cidadesPorEstado = new HashMap<>();
        
        try {
            for (UsuarioInstituicao ui : usuarios) {
                try {
                    if (ui.getUsuario() != null && ui.getUsuario().getPessoa() != null) {
                        Usuario usuario = ui.getUsuario();
                        
                        // Local da cidade
                        Local cidade = usuario.getPessoa().getCidade();
                        if (cidade != null) {
                            String nomeCidade = cidade.getNomeLocal();
                            
                            // Estado (pai da cidade)
                            Local estado = cidade.getLocalPai();
                            if (estado != null) {
                                String nomeEstado = estado.getNomeLocal();
                                
                                // País (pai do estado)
                                Local pais = estado.getLocalPai();
                                if (pais != null) {
                                    String nomePais = pais.getNomeLocal();
                                    
                                    // === CONTADORES SIMPLES ===
                                    usuariosPorPais.put(nomePais, usuariosPorPais.getOrDefault(nomePais, 0L) + 1);
                                    
                                    // === CONTADORES HIERÁRQUICOS ===
                                    
                                    // País/Estado
                                    String chaveEstado = nomePais + " / " + nomeEstado;
                                    usuariosPorEstado.put(chaveEstado, usuariosPorEstado.getOrDefault(chaveEstado, 0L) + 1);
                                    
                                    // País/Estado/Cidade
                                    String chaveCidade = nomePais + " / " + nomeEstado + " / " + nomeCidade;
                                    usuariosPorCidade.put(chaveCidade, usuariosPorCidade.getOrDefault(chaveCidade, 0L) + 1);
                                    
                                    // === ESTRUTURAS PARA FILTROS ===
                                    
                                    // Estados por país
                                    estadosPorPais.computeIfAbsent(nomePais, k -> new HashMap<>())
                                        .put(nomeEstado, estadosPorPais.get(nomePais).getOrDefault(nomeEstado, 0L) + 1);
                                    
                                    // Cidades por estado (com chave País/Estado)
                                    cidadesPorEstado.computeIfAbsent(chaveEstado, k -> new HashMap<>())
                                        .put(nomeCidade, cidadesPorEstado.get(chaveEstado).getOrDefault(nomeCidade, 0L) + 1);
                                        
                                } else {
                                    // Estado sem país
                                    String chaveEstado = "País não informado / " + nomeEstado;
                                    usuariosPorEstado.put(chaveEstado, usuariosPorEstado.getOrDefault(chaveEstado, 0L) + 1);
                                    
                                    if (nomeCidade != null) {
                                        String chaveCidade = "País não informado / " + nomeEstado + " / " + nomeCidade;
                                        usuariosPorCidade.put(chaveCidade, usuariosPorCidade.getOrDefault(chaveCidade, 0L) + 1);
                                    }
                                }
                            } else if (nomeCidade != null) {
                                // Cidade sem estado
                                String chaveCidade = "País não informado / Estado não informado / " + nomeCidade;
                                usuariosPorCidade.put(chaveCidade, usuariosPorCidade.getOrDefault(chaveCidade, 0L) + 1);
                            }
                        } else {
                            // Usuários sem localização definida
                            usuariosPorPais.put("Não informado", usuariosPorPais.getOrDefault("Não informado", 0L) + 1);
                            usuariosPorEstado.put("Não informado / Não informado", usuariosPorEstado.getOrDefault("Não informado / Não informado", 0L) + 1);
                            usuariosPorCidade.put("Não informado / Não informado / Não informado", usuariosPorCidade.getOrDefault("Não informado / Não informado / Não informado", 0L) + 1);
                        }
                    } else {
                        usuariosPorPais.put("Dados incompletos", usuariosPorPais.getOrDefault("Dados incompletos", 0L) + 1);
                        usuariosPorEstado.put("Dados incompletos / Dados incompletos", usuariosPorEstado.getOrDefault("Dados incompletos / Dados incompletos", 0L) + 1);
                        usuariosPorCidade.put("Dados incompletos / Dados incompletos / Dados incompletos", usuariosPorCidade.getOrDefault("Dados incompletos / Dados incompletos / Dados incompletos", 0L) + 1);
                    }
                } catch (Exception e) {
                    System.out.println("*** ERRO ao processar usuário individual: " + e.getMessage());
                    // Continuar processamento mesmo com erro
                }
            }
        } catch (Exception e) {
            System.out.println("*** ERRO GERAL ao processar localização: " + e.getMessage());
            e.printStackTrace();
            // Definir valores padrão em caso de erro
            usuariosPorPais.put("Erro no processamento", 0L);
            usuariosPorEstado.put("Erro no processamento / Erro no processamento", 0L);
            usuariosPorCidade.put("Erro no processamento / Erro no processamento / Erro no processamento", 0L);
        }
        
        long totalPaises = usuariosPorPais.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("Não informado") && 
                           !entry.getKey().equals("Dados incompletos") && 
                           !entry.getKey().equals("Erro no processamento"))
            .count();
        
        System.out.println("*** DEBUG: Processamento concluído ***");
        System.out.println("*** DEBUG: Países válidos encontrados: " + totalPaises + " (total com todos: " + usuariosPorPais.size() + ")");
        System.out.println("*** DEBUG: Lista de países: " + usuariosPorPais.keySet());
        System.out.println("*** DEBUG: Estados encontrados: " + usuariosPorEstado.size() + " ***");
        System.out.println("*** DEBUG: Cidades encontradas: " + usuariosPorCidade.size() + " ***");
        
        // === ADICIONAR DADOS AO MODEL ===
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosComSubInstituicoes", usuariosComSubInstituicoes);
        model.addAttribute("usuariosSemSubInstituicoes", usuariosSemSubInstituicoes);
        model.addAttribute("totalPaises", totalPaises);
        
        // === NOVA: Estatísticas por sub-instituição ===
        model.addAttribute("usuariosPorSubInstituicao", usuariosPorSubInstituicao);
        
        // Estatísticas por localização (hierárquicas)
        model.addAttribute("usuariosPorPais", usuariosPorPais);
        model.addAttribute("usuariosPorEstado", usuariosPorEstado);
        model.addAttribute("usuariosPorCidade", usuariosPorCidade);
        
        // Estruturas para filtros
        model.addAttribute("estadosPorPais", estadosPorPais);
        model.addAttribute("cidadesPorEstado", cidadesPorEstado);
        
        // Lista de países para filtros
        model.addAttribute("listaPaises", new ArrayList<>(usuariosPorPais.keySet()));
        
        // Dados da sessão simulados para bypass
        model.addAttribute("usuarioLogado", "DEMONSTRAÇÃO");
        model.addAttribute("instituicaoSelecionada", nomeInstituicao);
        model.addAttribute("nivelAcessoAtual", 5);
        
        System.out.println("*** DEBUG: Todos os dados adicionados ao model - retornando template ***");
        return "gestao-usuarios/estatistica-usuarios-simples";
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
    
    // Endpoint de debug sem autenticação (temporário)
    @GetMapping("/debug-estatisticas")
    public String debugEstatisticas(Model model) {
        try {
            System.out.println("*** DEBUG: Acesso sem autenticação ***");
            
            // Buscar todos os usuários para debug
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAll();
            System.out.println("*** DEBUG: " + usuarios.size() + " usuários encontrados ***");
            
            return processarEstatisticas(usuarios, model, null, "DEBUG - Todas as Instituições");
            
        } catch (Exception e) {
            System.out.println("*** ERRO no debug: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("erro", "Erro ao carregar estatísticas: " + e.getMessage());
            return "gestao-usuarios/estatistica-usuarios-simples";
        }
    }
}
