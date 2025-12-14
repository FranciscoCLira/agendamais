package com.agendademais.controllers;

import com.agendademais.entities.Regiao;
import com.agendademais.entities.Local;
import com.agendademais.dto.LocalDTO;
import com.agendademais.service.RegiaoService;
import com.agendademais.repositories.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;
import com.agendademais.entities.Instituicao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para gerenciamento de Regiões
 * Base URL: /administrador/regioes
 */
@Controller
@RequestMapping("/administrador/regioes")
public class RegiaoController {

    @Autowired
    private RegiaoService regiaoService;

    @Autowired
    private LocalRepository localRepository;

    /**
     * GET /administrador/regioes
     * Lista todas as regiões
     */
    @GetMapping
    public String listar(Model model, HttpSession session) {
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        List<Regiao> regioes = (instituicaoSelecionada != null)
                ? regiaoService.listarPorInstituicao(instituicaoSelecionada)
                : regiaoService.listarTodas();
        model.addAttribute("regioes", regioes);
        return "administrador/regioes";
    }

    /**
     * GET /administrador/regioes/novo
     * Exibe formulário para criar nova região
     */
    @GetMapping("/novo")
    public String novaRegiao(Model model) {
        Regiao regiao = new Regiao();
        model.addAttribute("regiao", regiao);
        carregarDadosFormulario(model, regiao);
        return "administrador/regiao-form";
    }

    /**
     * GET /administrador/regioes/{id}/editar
     * Exibe formulário para editar região existente
     */
    @GetMapping("/{id}/editar")
    public String editarRegiao(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        try {
            Regiao regiao = regiaoService.obterPorId(id);
            model.addAttribute("regiao", regiao);
            carregarDadosFormulario(model, regiao);
            return "administrador/regiao-form";
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("erro", "Região não encontrada");
            return "redirect:/administrador/regioes";
        }
    }

    /**
     * POST /administrador/regioes/salvar
     * Salva uma nova região ou atualiza existente
     */
    @PostMapping("/salvar")
    public String salvarRegiao(@ModelAttribute Regiao regiao,
            @RequestParam(required = false) Long paisId,
            @RequestParam(required = false) Long estadoId,
            @RequestParam(required = false) Long[] cidadeIds,
            Model model,
            HttpSession session,
            RedirectAttributes attributes) {
        try {
            // Vincula instituição logada
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
            if (instituicaoSelecionada == null) {
                throw new IllegalArgumentException("Nenhuma instituição selecionada na sessão");
            }
            regiao.setInstituicao(instituicaoSelecionada);
            // Carrega os Locais do banco de dados
            if (paisId != null) {
                Local pais = localRepository.findById(paisId)
                        .orElseThrow(() -> new IllegalArgumentException("País não encontrado"));
                regiao.setPais(pais);
            }

            if (estadoId != null) {
                Local estado = localRepository.findById(estadoId)
                        .orElseThrow(() -> new IllegalArgumentException("Estado não encontrado"));
                regiao.setEstado(estado);
            }

            // Carrega as cidades
            if (cidadeIds != null && cidadeIds.length > 0) {
                List<Local> cidades = localRepository.findAllById(List.of(cidadeIds));
                regiao.setCidades(cidades);
            } else {
                regiao.setCidades(List.of());
            }

            // Valida se país e estado estão definidos
            if (regiao.getPais() == null || regiao.getEstado() == null) {
                model.addAttribute("erro", "País e Estado são obrigatórios");
                model.addAttribute("regiao", regiao);
                carregarDadosFormulario(model, regiao);
                return "administrador/regiao-form";
            }

            if (regiao.getId() == null) {
                // Nova região
                regiaoService.criarRegiao(regiao);
                attributes.addFlashAttribute("sucesso", "Região criada com sucesso!");
            } else {
                // Atualizar região existente
                regiaoService.atualizarRegiao(regiao.getId(), regiao);
                attributes.addFlashAttribute("sucesso", "Região atualizada com sucesso!");
            }

            return "redirect:/administrador/regioes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("regiao", regiao);
            carregarDadosFormulario(model, regiao);
            return "administrador/regiao-form";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar região: " + e.getMessage());
            model.addAttribute("regiao", regiao);
            carregarDadosFormulario(model, regiao);
            return "administrador/regiao-form";
        }
    }

    /**
     * GET /administrador/regioes/{id}/deletar
     * Deleta uma região
     */
    @GetMapping("/{id}/deletar")
    public String deletarRegiao(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            regiaoService.deletarRegiao(id);
            attributes.addFlashAttribute("sucesso", "Região deletada com sucesso!");
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("erro", "Região não encontrada");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", "Erro ao deletar região: " + e.getMessage());
        }
        return "redirect:/administrador/regioes";
    }

    /**
     * API: GET /administrador/regioes/api/estados/{paisId}
     * Retorna todos os estados de um país
     */
    @GetMapping("/api/estados/{paisId}")
    @ResponseBody
    public List<LocalDTO> obterEstadosPorPais(@PathVariable Long paisId) {
        Local pais = localRepository.findById(paisId)
                .orElseThrow(() -> new IllegalArgumentException("País não encontrado"));
        List<Local> estados = localRepository.findByTipoLocalAndLocalPai(2, pais);
        return estados.stream()
                .map(e -> new LocalDTO(e.getId(), e.getTipoLocal(), e.getNomeLocal(), e.getRevisadoLocal()))
                .collect(Collectors.toList());
    }

    /**
     * API: GET /administrador/regioes/api/cidades/{estadoId}
     * Retorna todas as cidades de um estado
     */
    @GetMapping("/api/cidades/{estadoId}")
    @ResponseBody
    public List<LocalDTO> obterCidadesPorEstado(@PathVariable Long estadoId) {
        Local estado = localRepository.findById(estadoId)
                .orElseThrow(() -> new IllegalArgumentException("Estado não encontrado"));
        List<Local> cidades = localRepository.findByTipoLocalAndLocalPai(3, estado);
        return cidades.stream()
                .map(c -> new LocalDTO(c.getId(), c.getTipoLocal(), c.getNomeLocal(), c.getRevisadoLocal()))
                .collect(Collectors.toList());
    }

        /**
         * API: GET /administrador/regioes/api/{regiaoId}/cidades
         * Retorna todas as cidades de uma região específica
         */
        @GetMapping("/api/{regiaoId}/cidades")
        @ResponseBody
        public List<LocalDTO> obterCidadesPorRegiao(@PathVariable Long regiaoId) {
        Regiao regiao = regiaoService.obterPorId(regiaoId);
        return regiao.getCidades().stream()
            .map(c -> new LocalDTO(c.getId(), c.getTipoLocal(), c.getNomeLocal(), c.getRevisadoLocal()))
            .collect(Collectors.toList());
        }

    /**
     * API: GET /administrador/regioes/api/todos
     * Retorna todas as regiões em formato JSON
     */
    @GetMapping("/api/todos")
    @ResponseBody
    public List<Regiao> obterTodasRegioes() {
        return regiaoService.listarTodas();
    }

    /**
     * Carrega dados necessários para o formulário
     */
    private void carregarDadosFormulario(Model model, Regiao regiao) {
        try {
            // Lista todos os países (tipoLocal = 1)
            List<Local> paises = localRepository.findByTipoLocal(1);
            if (paises == null || paises.isEmpty()) {
                throw new IllegalArgumentException("Nenhum país cadastrado no sistema");
            }
            model.addAttribute("paises", paises);
            List<Local> estados = List.of();
            List<Local> cidades = List.of();
            List<Long> cidadeIdsSelecionadas = List.of();

            if (regiao != null && regiao.getPais() != null) {
                estados = localRepository.findByTipoLocalAndLocalPai(2, regiao.getPais());
            }

            if (regiao != null && regiao.getEstado() != null) {
                cidades = localRepository.findByTipoLocalAndLocalPai(3, regiao.getEstado());
            }

            if (regiao != null && regiao.getCidades() != null) {
                cidadeIdsSelecionadas = regiao.getCidades().stream()
                        .map(Local::getId)
                        .collect(Collectors.toList());
            }

            model.addAttribute("estados", estados);
            model.addAttribute("cidades", cidades);
            model.addAttribute("cidadeIdsSelecionadas", cidadeIdsSelecionadas);
        } catch (Exception e) {
            System.err.println("Erro ao carregar países: " + e.getMessage());
            model.addAttribute("paises", List.of());
            model.addAttribute("estados", List.of());
            model.addAttribute("cidades", List.of());
            model.addAttribute("cidadeIdsSelecionadas", List.of());
            model.addAttribute("erro", "Erro ao carregar países: " + e.getMessage());
        }
    }
}
