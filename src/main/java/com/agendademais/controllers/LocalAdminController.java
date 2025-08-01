package com.agendademais.controllers;

import com.agendademais.entities.Local;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.LocalRepository;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.utils.StringUtils;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller para administração de locais (País, Estado, Cidade)
 * Acesso: SuperUsuário (nível 9) no contexto de "Controle Total"
 */
@Controller
@RequestMapping("/gestao/locais")
public class LocalAdminController {

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    /**
     * Interceptador para verificar nível de acesso
     * Apenas usuários SuperUsuário (nível 9) no contexto de Controle Total podem
     * acessar
     */
    private boolean verificarAcessoControleTotal(HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        System.out.println("*** DEBUG VERIFICAR ACESSO CONTROLE TOTAL ***");
        System.out.println("Usuario na sessão: " + (usuario != null ? usuario.getUsername() : "null"));

        if (usuario == null) {
            System.out.println("Usuario é null - redirecionando para login");
            redirectAttributes.addFlashAttribute("mensagemErro", "Acesso negado. Faça login.");
            return false;
        }

        System.out.println("Nível do usuário: " + usuario.getNivelAcessoUsuario());

        // Deve ser SuperUsuário (nível 9)
        if (usuario.getNivelAcessoUsuario() != 9) {
            System.out.println("Usuário não é nível 9 - acesso negado");
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade exclusiva para SuperUsuário.");
            return false;
        }

        // Deve estar no contexto de Controle Total (sem instituição selecionada)
        Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
        System.out.println("Instituição selecionada na sessão: " + instituicaoSelecionada);

        if (instituicaoSelecionada != null) {
            System.out.println("Instituição selecionada não é null - acesso negado para Controle Total");
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Para acessar a Gestão de Locais, selecione 'Controle Total' no login.");
            return false;
        }

        System.out.println("Acesso autorizado para Controle Total");
        return true;
    }

    /**
     * Lista todos os locais com paginação e filtros
     */
    @GetMapping
    public String listarLocais(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nomeLocal") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Integer tipoLocal,
            @RequestParam(required = false) String nomeLocal,
            @RequestParam(required = false) String revisadoLocal,
            @RequestParam(required = false) Long paisId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!verificarAcessoControleTotal(session, redirectAttributes)) {
            // Se é SuperUsuário mas não está no contexto correto, volta para o menu dele
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null && usuario.getNivelAcessoUsuario() == 9) {
                return "redirect:/menus/menu-superusuario";
            }
            return "redirect:/acesso";
        }

        // Configuração da paginação
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Local> locaisPage;

        // Debug dos filtros recebidos
        System.out.println("*** DEBUG FILTROS RECEBIDOS ***");
        System.out.println("tipoLocal: " + tipoLocal);
        System.out.println("nomeLocal: '" + nomeLocal + "'");
        System.out.println("revisadoLocal: '" + revisadoLocal + "'");
        System.out.println("paisId: " + paisId);
        System.out.println("page: " + page + ", size: " + size);

        // Normalizar strings vazias para null
        String nomeLocalNorm = (nomeLocal != null && nomeLocal.trim().isEmpty()) ? null
                : (nomeLocal != null ? nomeLocal.trim() : null);
        String revisadoLocalNorm = (revisadoLocal != null && revisadoLocal.trim().isEmpty()) ? null
                : (revisadoLocal != null ? revisadoLocal.trim() : null);

        System.out.println("*** APÓS NORMALIZAÇÃO ***");
        System.out.println("tipoLocal: " + tipoLocal);
        System.out.println("nomeLocalNorm: '" + nomeLocalNorm + "'");
        System.out.println("revisadoLocalNorm: '" + revisadoLocalNorm + "'");
        System.out.println("paisId: " + paisId);

        // Aplicar filtros
        boolean temFiltros = (tipoLocal != null) || (nomeLocalNorm != null) || (revisadoLocalNorm != null)
                || (paisId != null);

        if (temFiltros) {
            System.out.println("*** APLICANDO FILTROS ***");

            // Filtro específico: Estados de um País
            if (paisId != null && tipoLocal == null && nomeLocalNorm == null && revisadoLocalNorm == null) {
                System.out.println("Filtro específico: Estados do país ID " + paisId);
                Optional<Local> paisOpt = localRepository.findById(paisId);
                if (paisOpt.isPresent()) {
                    locaisPage = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, paisOpt.get(), pageable);
                } else {
                    // País não encontrado, retorna página vazia
                    locaisPage = new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
                }
            }
            // Se apenas filtro por tipo, usar método específico
            else if (tipoLocal != null && nomeLocalNorm == null && revisadoLocalNorm == null && paisId == null) {
                System.out.println("Usando findByTipoLocal para tipo: " + tipoLocal);
                locaisPage = localRepository.findByTipoLocal(tipoLocal, pageable);
            } else if (nomeLocalNorm != null && !nomeLocalNorm.isEmpty()) {
                // Filtro com nome - usar busca com acentos
                System.out.println("Usando filtro com acentos para nome: " + nomeLocalNorm);
                List<Local> todosLocais = localRepository.findForAccentFilter(tipoLocal, revisadoLocalNorm);

                // Filtrar por nome ignorando acentos
                List<Local> locaisFiltrados = todosLocais.stream()
                        .filter(local -> StringUtils.containsIgnoreAccents(local.getNomeLocal(), nomeLocalNorm))
                        .toList();

                // Criar página manualmente
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), locaisFiltrados.size());
                List<Local> pageContent = locaisFiltrados.subList(start, end);
                locaisPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable,
                        locaisFiltrados.size());
            } else {
                // Debug específico para filtro por tipo
                if (tipoLocal != null) {
                    long countDebug = localRepository.countByTipoLocalDebug(tipoLocal);
                    System.out.println("Count debug para tipoLocal " + tipoLocal + ": " + countDebug);

                    List<Local> testList = localRepository.findByTipoLocal(tipoLocal);
                    System.out.println("findByTipoLocal retornou: " + testList.size() + " elementos");
                    if (!testList.isEmpty()) {
                        System.out.println("Primeiro elemento: " + testList.get(0).getNomeLocal());
                    }
                }

                // Usar query customizada com todos os filtros
                System.out.println("Usando findByFiltros");
                locaisPage = localRepository.findByFiltros(
                        tipoLocal,
                        nomeLocalNorm,
                        revisadoLocalNorm,
                        pageable);
            }

            System.out.println("Resultado com filtros: " + locaisPage.getTotalElements() + " elementos");
            System.out.println("Conteúdo da página: " + locaisPage.getContent().size() + " elementos");
        } else {
            // Busca todos
            System.out.println("*** SEM FILTROS - BUSCA TODOS ***");
            locaisPage = localRepository.findAll(pageable);
            System.out.println("Busca sem filtros: " + locaisPage.getTotalElements() + " elementos");
        }

        // Estatísticas gerais
        long totalLocais = localRepository.count();
        long locaisNaoRevisados = localRepository.countByRevisadoLocal("n");
        long totalPaises = localRepository.countByTipoLocal(1);
        long totalEstados = localRepository.countByTipoLocal(2);
        long totalCidades = localRepository.countByTipoLocal(3);

        // Informações específicas para filtro ativo
        String filtroAtivo = "";
        if (paisId != null) {
            Optional<Local> paisOpt = localRepository.findById(paisId);
            if (paisOpt.isPresent()) {
                Local pais = paisOpt.get();
                filtroAtivo = "Estados de: " + pais.getNomeLocal();
                // Estatísticas específicas do país selecionado
                totalEstados = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, pais).size();
                totalCidades = 0; // Não aplicável para este filtro
            }
        }

        // Dados para o template
        model.addAttribute("locais", locaisPage.getContent());
        model.addAttribute("page", locaisPage);
        model.addAttribute("totalLocais", totalLocais);
        model.addAttribute("locaisNaoRevisados", locaisNaoRevisados);
        model.addAttribute("totalPaises", totalPaises);
        model.addAttribute("totalEstados", totalEstados);
        model.addAttribute("totalCidades", totalCidades);
        model.addAttribute("filtroAtivo", filtroAtivo);

        // Manter filtros no formulário
        model.addAttribute("tipoLocal", tipoLocal);
        model.addAttribute("nomeLocal", nomeLocal);
        model.addAttribute("revisadoLocal", revisadoLocal);
        model.addAttribute("paisId", paisId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Lista de países para o filtro
        List<Local> paises = localRepository.findByTipoLocalOrderByNomeLocal(1);
        model.addAttribute("paises", paises);

        return "admin/locais/lista-local";
    }

    /**
     * Formulário para editar um local
     */
    @GetMapping("/editar/{id}")
    public String editarLocal(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!verificarAcessoControleTotal(session, redirectAttributes)) {
            // Se é SuperUsuário mas não está no contexto correto, volta para o menu dele
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null && usuario.getNivelAcessoUsuario() == 9) {
                return "redirect:/menus/menu-superusuario";
            }
            return "redirect:/acesso";
        }

        Optional<Local> localOpt = localRepository.findById(id);
        if (localOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Local não encontrado.");
            return "redirect:/gestao/locais";
        }

        Local local = localOpt.get();

        // Buscar opções de pais (para casos onde o local é estado ou cidade)
        List<Local> paises = localRepository.findByTipoLocalOrderByNomeLocal(1);
        List<Local> estados = List.of(); // Será carregado via AJAX conforme país selecionado

        if (local.getTipoLocal() == 3 && local.getLocalPai() != null) {
            // Se é cidade, buscar estados do país pai do estado atual
            Local estadoAtual = local.getLocalPai();
            if (estadoAtual.getLocalPai() != null) {
                estados = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, estadoAtual.getLocalPai());
            }
        }

        model.addAttribute("local", local);
        model.addAttribute("paises", paises);
        model.addAttribute("estados", estados);

        return "admin/locais/editar-local";
    }

    /**
     * Salvar alterações do local
     */
    @PostMapping("/editar/{id}")
    public String salvarLocal(
            @PathVariable Long id,
            @RequestParam String nomeLocal,
            @RequestParam String revisadoLocal,
            @RequestParam(required = false) Long idPai,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!verificarAcessoControleTotal(session, redirectAttributes)) {
            // Se é SuperUsuário mas não está no contexto correto, volta para o menu dele
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null && usuario.getNivelAcessoUsuario() == 9) {
                return "redirect:/menus/menu-superusuario";
            }
            return "redirect:/acesso";
        }

        Optional<Local> localOpt = localRepository.findById(id);
        if (localOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Local não encontrado.");
            return "redirect:/gestao/locais";
        }

        Local local = localOpt.get();
        String nomeOriginal = local.getNomeLocal();
        String revisaoOriginal = local.getRevisadoLocal();

        // Validações
        if (nomeLocal == null || nomeLocal.isBlank()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Nome do local é obrigatório.");
            return "redirect:/gestao/locais/editar/" + id;
        }

        // Verificar duplicatas do nome no mesmo nível hierárquico
        List<Local> duplicatas = localRepository.findByTipoLocalAndNomeLocalIgnoreCaseAndIdNot(
                local.getTipoLocal(), nomeLocal.trim(), id);

        if (!duplicatas.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Já existe outro local do mesmo tipo com este nome: " + nomeLocal);
            return "redirect:/gestao/locais/editar/" + id;
        }

        // Atualizar local pai se necessário
        if (idPai != null && !idPai.equals(local.getLocalPai() != null ? local.getLocalPai().getId() : null)) {
            Optional<Local> paiOpt = localRepository.findById(idPai);
            if (paiOpt.isPresent()) {
                local.setLocalPai(paiOpt.get());
            }
        }

        // Atualizar dados
        local.setNomeLocal(nomeLocal.trim());
        local.setRevisadoLocal(revisadoLocal);

        // Atualizar data apenas se algo mudou
        if (!nomeOriginal.equals(nomeLocal.trim()) || !revisaoOriginal.equals(revisadoLocal)) {
            local.setDataUltimaAtualizacao(LocalDate.now());
        }

        localRepository.save(local);

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        System.out.println("Local atualizado pelo SuperUsuário " + usuario.getUsername() +
                ": [" + id + "] " + nomeOriginal + " -> " + nomeLocal + " (revisado: " + revisadoLocal + ")");

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Local '" + nomeLocal + "' atualizado com sucesso.");

        return "redirect:/gestao/locais";
    }

    /**
     * API para buscar estados de um país (AJAX)
     */
    @GetMapping("/api/estados/{paisId}")
    @ResponseBody
    public List<Local> buscarEstadosPorPais(@PathVariable Long paisId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null || usuario.getNivelAcessoUsuario() != 9) {
            return List.of();
        }

        Optional<Local> paisOpt = localRepository.findById(paisId);
        if (paisOpt.isEmpty()) {
            return List.of();
        }

        return localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, paisOpt.get());
    }

    /**
     * Marcar múltiplos locais como revisados - versão GET
     */
    @GetMapping("/revisar-multiplos")
    public String revisarMultiplosGet(
            @RequestParam(required = false) List<Long> ids,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        return processarRevisaoMultipla(ids, session, redirectAttributes);
    }

    /**
     * Marcar múltiplos locais como revisados - versão POST
     */
    @PostMapping("/revisar-multiplos")
    public String revisarMultiplos(
            @RequestParam(required = false) List<Long> ids,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        return processarRevisaoMultipla(ids, session, redirectAttributes);
    }

    /**
     * Método comum para processar revisão múltipla
     */
    private String processarRevisaoMultipla(
            List<Long> ids,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("*** DEBUG REVISAR MULTIPLOS ***");
        System.out.println("IDs recebidos: " + ids);
        System.out.println("Session ID: " + session.getId());

        // Debug da sessão
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        Object instSelecionada = session.getAttribute("instituicaoSelecionada");
        System.out.println("Usuario da sessão: " + (usuarioSessao != null
                ? usuarioSessao.getUsername() + " (nível " + usuarioSessao.getNivelAcessoUsuario() + ")"
                : "null"));
        System.out.println("Instituição selecionada: " + instSelecionada);

        if (!verificarAcessoControleTotal(session, redirectAttributes)) {
            System.out.println("FALHA: Acesso negado em revisar multiplos");
            // Se é SuperUsuário mas não está no contexto correto, volta para o menu dele
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null && usuario.getNivelAcessoUsuario() == 9) {
                System.out.println("Redirecionando SuperUsuario para menu");
                return "redirect:/menus/menu-superusuario";
            }
            System.out.println("Redirecionando para acesso");
            return "redirect:/acesso";
        }

        System.out.println("SUCESSO: Acesso autorizado - processando revisão múltipla");

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Nenhum local selecionado.");
            return "redirect:/gestao/locais";
        }

        int atualizados = 0;
        for (Long id : ids) {
            Optional<Local> localOpt = localRepository.findById(id);
            if (localOpt.isPresent()) {
                Local local = localOpt.get();
                // Sempre atualiza, independente do status atual
                local.setRevisadoLocal("s");
                local.setDataUltimaAtualizacao(LocalDate.now());
                localRepository.save(local);
                atualizados++;
            }
        }

        if (atualizados > 0) {
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    atualizados + " local(is) marcado(s) como revisado(s) e data atualizada.");
        } else {
            redirectAttributes.addFlashAttribute("mensagemInfo", "Nenhum local foi encontrado para atualização.");
        }

        return "redirect:/gestao/locais";
    }

    /**
     * Visualização hierárquica de locais
     */
    @GetMapping("/hierarquia")
    public String visualizacaoHierarquica(
            @RequestParam(required = false) Long paisId,
            @RequestParam(required = false) Long estadoId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!verificarAcessoControleTotal(session, redirectAttributes)) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null && usuario.getNivelAcessoUsuario() == 9) {
                return "redirect:/menus/menu-superusuario";
            }
            return "redirect:/acesso";
        }

        // Buscar todos os países para o filtro
        List<Local> paises = localRepository.findByTipoLocalOrderByNomeLocal(1);

        // Estrutura hierárquica
        List<HierarchyNode> hierarchy = new ArrayList<>();

        if (paisId != null) {
            // Filtro por país específico
            Optional<Local> paisOpt = localRepository.findById(paisId);
            if (paisOpt.isPresent()) {
                Local pais = paisOpt.get();

                if (estadoId != null) {
                    // Filtro por país e estado específico
                    Optional<Local> estadoOpt = localRepository.findById(estadoId);
                    if (estadoOpt.isPresent()) {
                        Local estado = estadoOpt.get();
                        List<Local> cidades = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(3, estado);

                        HierarchyNode paisNode = new HierarchyNode(pais);
                        HierarchyNode estadoNode = new HierarchyNode(estado);
                        estadoNode.setCidades(cidades);
                        paisNode.getEstados().add(estadoNode);
                        hierarchy.add(paisNode);
                    }
                } else {
                    // Filtro só por país
                    List<Local> estados = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, pais);

                    HierarchyNode paisNode = new HierarchyNode(pais);
                    for (Local estado : estados) {
                        List<Local> cidades = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(3, estado);
                        HierarchyNode estadoNode = new HierarchyNode(estado);
                        estadoNode.setCidades(cidades);
                        paisNode.getEstados().add(estadoNode);
                    }
                    hierarchy.add(paisNode);
                }
            }
        } else {
            // Sem filtro - mostrar todos organizados hierarquicamente
            for (Local pais : paises) {
                List<Local> estados = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, pais);

                HierarchyNode paisNode = new HierarchyNode(pais);
                for (Local estado : estados) {
                    List<Local> cidades = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(3, estado);
                    HierarchyNode estadoNode = new HierarchyNode(estado);
                    estadoNode.setCidades(cidades);
                    paisNode.getEstados().add(estadoNode);
                }
                hierarchy.add(paisNode);
            }
        }

        // Calcular estatísticas baseadas no filtro
        long totalPaises = paisId != null ? 1 : paises.size();
        long totalEstados = 0;
        long totalCidades = 0;
        long totalNaoRevisados = 0;

        for (HierarchyNode paisNode : hierarchy) {
            totalEstados += paisNode.getEstados().size();
            for (HierarchyNode estadoNode : paisNode.getEstados()) {
                totalCidades += estadoNode.getCidades().size();

                // Contar não revisados
                if (!"s".equals(paisNode.getPais().getRevisadoLocal()))
                    totalNaoRevisados++;
                if (!"s".equals(estadoNode.getEstado().getRevisadoLocal()))
                    totalNaoRevisados++;

                for (Local cidade : estadoNode.getCidades()) {
                    if (!"s".equals(cidade.getRevisadoLocal()))
                        totalNaoRevisados++;
                }
            }
        }

        // Buscar estados para o filtro (se país selecionado)
        List<Local> estadosParaFiltro = new ArrayList<>();
        if (paisId != null) {
            Optional<Local> paisOpt = localRepository.findById(paisId);
            if (paisOpt.isPresent()) {
                estadosParaFiltro = localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, paisOpt.get());
            }
        }

        model.addAttribute("hierarchy", hierarchy);
        model.addAttribute("paises", paises);
        model.addAttribute("estadosParaFiltro", estadosParaFiltro);
        model.addAttribute("paisSelecionado", paisId);
        model.addAttribute("estadoSelecionado", estadoId);

        // Estatísticas
        model.addAttribute("totalPaises", totalPaises);
        model.addAttribute("totalEstados", totalEstados);
        model.addAttribute("totalCidades", totalCidades);
        model.addAttribute("totalNaoRevisados", totalNaoRevisados);
        model.addAttribute("totalGeral", totalPaises + totalEstados + totalCidades);

        return "admin/locais/hierarquia-local";
    }

    /**
     * API para buscar estados por país (para filtro hierárquico)
     */
    @GetMapping("/api/estados-filtro/{paisId}")
    @ResponseBody
    public List<Local> buscarEstadosParaFiltro(@PathVariable Long paisId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null || usuario.getNivelAcessoUsuario() != 9) {
            return List.of();
        }

        Optional<Local> paisOpt = localRepository.findById(paisId);
        if (paisOpt.isEmpty()) {
            return List.of();
        }

        return localRepository.findByTipoLocalAndLocalPaiOrderByNomeLocal(2, paisOpt.get());
    }

    /**
     * Endpoint para relacionar Local com Pessoas - mostra quais pessoas estão
     * associadas a um local
     */
    @GetMapping("/relacao-pessoas")
    public String relacaoPessoasLocal(
            @RequestParam(required = false) Long localId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!verificarAcessoControleTotal(session, redirectAttributes)) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null && usuario.getNivelAcessoUsuario() == 9) {
                return "redirect:/menus/menu-superusuario";
            }
            return "redirect:/acesso";
        }

        // Buscar todos os locais para o filtro
        List<Local> todosLocais = localRepository.findAll();

        // Se um local específico foi selecionado, buscar pessoas relacionadas
        List<Pessoa> pessoasRelacionadas = new ArrayList<>();
        Local localSelecionado = null;

        if (localId != null) {
            Optional<Local> localOpt = localRepository.findById(localId);
            if (localOpt.isPresent()) {
                localSelecionado = localOpt.get();

                // Buscar pessoas baseado no tipo do local
                switch (localSelecionado.getTipoLocal()) {
                    case 1: // País
                        pessoasRelacionadas = pessoaRepository.findByPais(localSelecionado);
                        break;
                    case 2: // Estado
                        pessoasRelacionadas = pessoaRepository.findByEstado(localSelecionado);
                        break;
                    case 3: // Cidade
                        pessoasRelacionadas = pessoaRepository.findByCidade(localSelecionado);
                        break;
                }
            }
        }

        model.addAttribute("todosLocais", todosLocais);
        model.addAttribute("localSelecionado", localSelecionado);
        model.addAttribute("localId", localId);
        model.addAttribute("pessoasRelacionadas", pessoasRelacionadas);

        return "admin/locais/relacao-pessoas";
    }

    // ...existing code...

    // Endpoint de debug para verificar dados (sem autenticação para debug)
    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== DEBUG LOCAIS ===<br>");

            List<Local> todos = localRepository.findAll();
            sb.append("Total de locais: ").append(todos.size()).append("<br><br>");

            for (Local local : todos) {
                sb.append("ID: ").append(local.getId())
                        .append(", Tipo: ").append(local.getTipoLocal())
                        .append(", Nome: ").append(local.getNomeLocal())
                        .append(", Pai: ")
                        .append(local.getLocalPai() != null ? local.getLocalPai().getNomeLocal() : "null")
                        .append(", Revisado: ").append(local.getRevisadoLocal())
                        .append("<br>");
            }

            sb.append("<br>=== CONTADORES ===<br>");
            sb.append("Países (tipo 1): ").append(localRepository.countByTipoLocal(1)).append("<br>");
            sb.append("Estados (tipo 2): ").append(localRepository.countByTipoLocal(2)).append("<br>");
            sb.append("Cidades (tipo 3): ").append(localRepository.countByTipoLocal(3)).append("<br>");

            return sb.toString();
        } catch (Exception e) {
            return "Erro no debug: " + e.getMessage();
        }
    }
}
