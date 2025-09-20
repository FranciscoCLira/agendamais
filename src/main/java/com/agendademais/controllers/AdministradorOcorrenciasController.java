package com.agendademais.controllers;

import com.agendademais.entities.Autor;
import com.agendademais.entities.Atividade;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.repositories.AtividadeRepository;
import com.agendademais.repositories.AutorRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/administrador/ocorrencias")
public class AdministradorOcorrenciasController {
    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;
    @Autowired
    private AtividadeRepository atividadeRepository;
    @Autowired
    private AutorRepository autorRepository;
    @Autowired
    private com.agendademais.repositories.LogPostagemRepository logPostagemRepository;

    @GetMapping
    public String listarOcorrencias(
            @RequestParam(value = "atividadeId", required = false) String atividadeIdParam,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "situacao", required = false) String situacao,
            @RequestParam(value = "ordem", required = false) String ordem,
            @RequestParam(value = "dataInicio", required = false) String dataInicio,
            @RequestParam(value = "dataFim", required = false) String dataFim,
            @RequestParam(value = "temaOcorrencia", required = false) String temaOcorrencia,
            @RequestParam(value = "autorId", required = false) Long autorId,
            @RequestParam(value = "autorNome", required = false) String autorNome,
            @RequestParam(value = "assuntoDivulgacao", required = false) String assuntoDivulgacao,
            @RequestParam(value = "tituloAtividade", required = false) String tituloAtividade,
            @RequestParam(value = "origem", required = false) String origem,
            Model model,
            HttpSession session) {
        // Redireciona para login se sessão não existir ou usuário não estiver logado
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            return "redirect:/acesso";
        }
        // Se veio do menu, sem atividade selecionada
        Long atividadeId = null;
        if (atividadeIdParam != null && !atividadeIdParam.isEmpty()) {
            // Corrige caso venha duplicado ou como lista: "[29,29]" ou "29,29"
            String clean = atividadeIdParam.replaceAll("[\\[\\]\\s]", "");
            String[] parts = clean.split(",");
            if (parts.length > 0) {
                try {
                    atividadeId = Long.parseLong(parts[0]);
                } catch (Exception e) {
                    atividadeId = null;
                }
            }
        }
        if (atividadeId == null) {
            if (origem == null || origem.isEmpty()) {
                origem = "menu";
            }
            model.addAttribute("ocorrencias", org.springframework.data.domain.Page.empty());
            model.addAttribute("atividadeSelecionada", null);
            model.addAttribute("origem", origem);
            return "administrador/ocorrencias";
        }
        // Se veio da tela de atividades
        Optional<Atividade> atividadeOpt = atividadeRepository.findById(atividadeId);
        if (atividadeOpt.isEmpty()) {
            if (origem == null || origem.isEmpty()) {
                origem = "menu";
            }
            model.addAttribute("ocorrencias", org.springframework.data.domain.Page.empty());
            model.addAttribute("atividadeSelecionada", null);
            model.addAttribute("origem", origem);
            return "administrador/ocorrencias";
        }
        Atividade atividade = atividadeOpt.get();
        // Montar ordenação
        Sort sort = Sort.by("dataOcorrencia").descending().and(Sort.by("horaInicioOcorrencia").descending());
        if (ordem != null) {
            if (ordem.equals("situacao")) {
                sort = Sort.by("situacaoOcorrencia").ascending().and(Sort.by("dataOcorrencia").descending())
                        .and(Sort.by("horaInicioOcorrencia").descending());
            } else if (ordem.equals("autor")) {
                sort = Sort.by("idAutor.pessoa.nomePessoa").ascending().and(Sort.by("dataOcorrencia").descending())
                        .and(Sort.by("horaInicioOcorrencia").descending());
            } else if (ordem.equals("dataAsc")) {
                sort = Sort.by("dataOcorrencia").ascending().and(Sort.by("horaInicioOcorrencia").ascending());
            } else if (ordem.equals("data")) {
                sort = Sort.by("dataOcorrencia").descending().and(Sort.by("horaInicioOcorrencia").descending());
            }
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        // Filtro por instituição logada
        var instituicao = session.getAttribute("instituicaoSelecionada");
        Long instituicaoId = null;
        if (instituicao != null && instituicao instanceof com.agendademais.entities.Instituicao) {
            instituicaoId = ((com.agendademais.entities.Instituicao) instituicao).getId();
        }
        Specification<OcorrenciaAtividade> spec = com.agendademais.specs.OcorrenciaAtividadeSpecs
                .porInstituicao(instituicaoId)
                .and((root, query, cb) -> cb.equal(root.get("idAtividade"), atividade));
        if (situacao != null && !situacao.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("situacaoOcorrencia"), situacao));
        }
        if (dataInicio != null && !dataInicio.isEmpty()) {
            String[] parts = dataInicio.split(",");
            for (String part : parts) {
                String clean = part.trim();
                if (clean.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    try {
                        java.time.LocalDate dataIni = java.time.LocalDate.parse(clean);
                        spec = spec
                                .and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataOcorrencia"), dataIni));
                        break; // só usa o primeiro válido
                    } catch (Exception e) {
                        // ignora valor inválido
                    }
                }
            }
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            String[] parts = dataFim.split(",");
            for (String part : parts) {
                String clean = part.trim();
                if (clean.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    try {
                        java.time.LocalDate dataF = java.time.LocalDate.parse(clean);
                        spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataOcorrencia"), dataF));
                        break; // só usa o primeiro válido
                    } catch (Exception e) {
                        // ignora valor inválido
                    }
                }
            }
        }
        if (temaOcorrencia != null && !temaOcorrencia.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("temaOcorrencia")),
                    "%" + temaOcorrencia.toLowerCase() + "%"));
        }
        // Filtro para assuntoDivulgacao (case-insensitive, busca parcial)
        if (assuntoDivulgacao != null && !assuntoDivulgacao.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("assuntoDivulgacao")),
                    "%" + assuntoDivulgacao.toLowerCase() + "%"));
        }
        if (autorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("idAutor").get("id"), autorId));
        } else if (autorNome != null && !autorNome.isEmpty()) {
            String nomeLower = autorNome.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("idAutor").get("pessoa").get("nomePessoa")),
                    "%" + nomeLower + "%"));
        }
        Page<OcorrenciaAtividade> ocorrencias = ocorrenciaAtividadeRepository.findAll(spec, pageable);
        model.addAttribute("atividadeSelecionada", atividade);
        model.addAttribute("ocorrencias", ocorrencias);
        // IDs de ocorrências com logs relacionados
        java.util.Set<Long> ocorrenciasComLogs = new java.util.HashSet<>();
        for (var oc : ocorrencias) {
            if (!logPostagemRepository.findByOcorrenciaAtividadeId(oc.getId()).isEmpty()) {
                ocorrenciasComLogs.add(oc.getId());
            }
        }
        model.addAttribute("ocorrenciasComLogs", ocorrenciasComLogs);
        // Lista de autores presentes em todas as ocorrências da atividade (para
        // autocomplete), ignorando filtro de autor
        Specification<OcorrenciaAtividade> specAutores = (root, query, cb) -> cb.equal(root.get("idAtividade"),
                atividade);
        if (situacao != null && !situacao.isEmpty()) {
            specAutores = specAutores.and((root, query, cb) -> cb.equal(root.get("situacaoOcorrencia"), situacao));
        }
        if (dataInicio != null && !dataInicio.isEmpty()) {
            java.time.LocalDate dataIni = java.time.LocalDate.parse(dataInicio);
            specAutores = specAutores
                    .and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataOcorrencia"), dataIni));
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            java.time.LocalDate dataF = java.time.LocalDate.parse(dataFim);
            specAutores = specAutores.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataOcorrencia"), dataF));
        }
        if (temaOcorrencia != null && !temaOcorrencia.isEmpty()) {
            specAutores = specAutores.and((root, query, cb) -> cb.like(cb.lower(root.get("temaOcorrencia")),
                    "%" + temaOcorrencia.toLowerCase() + "%"));
        }
        List<OcorrenciaAtividade> todasOcorrencias = ocorrenciaAtividadeRepository.findAll(specAutores);
        java.util.Set<Autor> autoresDasOcorrenciasSet = new java.util.HashSet<>();
        for (OcorrenciaAtividade oc : todasOcorrencias) {
            Autor a = oc.getIdAutor();
            if (a != null)
                autoresDasOcorrenciasSet.add(a);
        }
        System.out.println("[DEBUG] autoresDasOcorrenciasSet size: " + autoresDasOcorrenciasSet.size());
        for (Autor a : autoresDasOcorrenciasSet) {
            System.out.println("[DEBUG] Autor: id=" + a.getId() + ", nome="
                    + (a.getPessoa() != null ? a.getPessoa().getNomePessoa() : "null"));
        }
        List<Autor> autoresDasOcorrencias = new java.util.ArrayList<>(autoresDasOcorrenciasSet);
        model.addAttribute("autoresDasOcorrencias", autoresDasOcorrencias);
        List<java.util.Map<String, Object>> autoresDasOcorrenciasList = new java.util.ArrayList<>();
        for (Autor a : autoresDasOcorrencias) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", a.getId());
            map.put("nome", a.getPessoa() != null ? a.getPessoa().getNomePessoa() : "");
            map.put("email", a.getPessoa() != null ? a.getPessoa().getEmailPessoa() : "");
            autoresDasOcorrenciasList.add(map);
        }
        model.addAttribute("autoresDasOcorrenciasList", autoresDasOcorrenciasList);
        model.addAttribute("totalOcorrencias", ocorrencias.getTotalElements());
        // Monta a URL de origem completa com todos os filtros atuais, incluindo
        // tituloAtividade e assuntoDivulgacao
        StringBuilder origemCompleta = new StringBuilder("/administrador/ocorrencias?");
        origemCompleta.append("page=").append(page);
        if (atividadeId != null)
            origemCompleta.append("&atividadeId=").append(atividadeId);
        if (ordem != null)
            origemCompleta.append("&ordem=").append(ordem);
        if (dataInicio != null)
            origemCompleta.append("&dataInicio=").append(dataInicio);
        if (dataFim != null)
            origemCompleta.append("&dataFim=").append(dataFim);
        if (temaOcorrencia != null)
            origemCompleta.append("&temaOcorrencia=").append(temaOcorrencia);
        if (autorNome != null)
            origemCompleta.append("&autorNome=").append(autorNome);
        if (autorId != null)
            origemCompleta.append("&autorId=").append(autorId);
        if (situacao != null)
            origemCompleta.append("&situacao=").append(situacao);
        if (size != 25)
            origemCompleta.append("&size=").append(size);
        if (tituloAtividade != null)
            origemCompleta.append("&tituloAtividade=").append(tituloAtividade);
        if (assuntoDivulgacao != null)
            origemCompleta.append("&assuntoDivulgacao=").append(assuntoDivulgacao);
        model.addAttribute("origem", origemCompleta.toString());
        return "administrador/ocorrencias";
    }

    @GetMapping("/nova")
    public String novaOcorrencia(@RequestParam(value = "atividadeId", required = false) Long atividadeId,
            @RequestParam(value = "origem", required = false) String origem,
            Model model) {
        OcorrenciaAtividade ocorrencia = new OcorrenciaAtividade();
        if (atividadeId != null) {
            atividadeRepository.findById(atividadeId).ifPresent(ocorrencia::setIdAtividade);
        }
        model.addAttribute("ocorrencia", ocorrencia);
        model.addAttribute("origem", origem);
        return "administrador/ocorrencia-form";
    }

    @PostMapping("/nova")
    public String salvarOcorrencia(@ModelAttribute("ocorrencia") OcorrenciaAtividade ocorrencia,
            @RequestParam(value = "idAutorId", required = false) Long idAutorId,
            @RequestParam(value = "origem", required = false) String origem,
            Model model, HttpSession session) {
        // Buscar o Autor pelo idAutorId do formulário
        if (idAutorId != null) {
            Autor autor = autorRepository.findById(idAutorId).orElse(null);
            ocorrencia.setIdAutor(autor);
        } else {
            model.addAttribute("erroAutor", "Selecione um autor válido no autocomplete.");
            return "administrador/ocorrencia-form";
        }
        // Garantir que idAtividade não seja null se veio do form
        // Se idAtividade não veio, não salva (deve ser obrigatório via binding)
        // dataAtualizacao sempre data atual
        ocorrencia.setDataAtualizacao(java.time.LocalDate.now());
        // Campos opcionais: se em branco, salvar como null
        if (isBlank(ocorrencia.getAssuntoDivulgacao()))
            ocorrencia.setAssuntoDivulgacao(null);
        if (isBlank(ocorrencia.getBibliografia()))
            ocorrencia.setBibliografia(null);
        if (isBlank(ocorrencia.getDetalheDivulgacao()))
            ocorrencia.setDetalheDivulgacao(null);
        if (isBlank(ocorrencia.getLinkImgDivulgacao()))
            ocorrencia.setLinkImgDivulgacao(null);
        if (isBlank(ocorrencia.getLinkMaterialTema()))
            ocorrencia.setLinkMaterialTema(null);
        if (isBlank(ocorrencia.getObsEncerramento()))
            ocorrencia.setObsEncerramento(null);
        ocorrenciaAtividadeRepository.save(ocorrencia);
        // Redireciona para a lista de ocorrências da atividade
        Long atividadeId = (ocorrencia.getIdAtividade() != null && ocorrencia.getIdAtividade().getId() != null)
                ? ocorrencia.getIdAtividade().getId()
                : null;
        if (atividadeId != null) {
            String redirect = "/administrador/ocorrencias?atividadeId=" + atividadeId;
            if (origem != null && !origem.isEmpty()) {
                redirect += "&origem=" + origem;
            }
            return "redirect:" + redirect;
        } else {
            String redirect = "/administrador/ocorrencias";
            if (origem != null && !origem.isEmpty()) {
                redirect += "?origem=" + origem;
            }
            return "redirect:" + redirect;
        }
    }

    @GetMapping("/editar/{id}")
    public String editarOcorrencia(@PathVariable("id") Long id,
            @RequestParam(value = "origem", required = false) String origem,
            Model model) {
        Optional<OcorrenciaAtividade> ocorrenciaOpt = ocorrenciaAtividadeRepository.findById(id);
        if (ocorrenciaOpt.isEmpty()) {
            model.addAttribute("erro", "Ocorrência não encontrada.");
            String redirect = "/administrador/ocorrencias";
            if (origem != null)
                redirect += "?origem=" + origem;
            return "redirect:" + redirect;
        }
        model.addAttribute("ocorrencia", ocorrenciaOpt.get());
        model.addAttribute("origem", origem);
        // Também propaga o id da atividade para o formulário
        if (ocorrenciaOpt.get().getIdAtividade() != null) {
            model.addAttribute("atividadeId", ocorrenciaOpt.get().getIdAtividade().getId());
        }
        return "administrador/ocorrencia-form";
    }

    // Utilitário para checar se string está em branco
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @PostMapping("/editar/{id}")
    public String atualizarOcorrencia(@PathVariable("id") Long id,
            @ModelAttribute("ocorrencia") OcorrenciaAtividade ocorrenciaForm,
            @RequestParam(value = "idAutorId", required = false) Long idAutorId,
            @RequestParam(value = "origem", required = false) String origem,
            Model model) {
        Optional<OcorrenciaAtividade> ocorrenciaOpt = ocorrenciaAtividadeRepository.findById(id);
        if (ocorrenciaOpt.isEmpty()) {
            model.addAttribute("erro", "Ocorrência não encontrada.");
            return "redirect:/administrador/ocorrencias";
        }
        OcorrenciaAtividade ocorrencia = ocorrenciaOpt.get();
        // Atualizar todos os campos editáveis
        ocorrencia.setTemaOcorrencia(ocorrenciaForm.getTemaOcorrencia());
        ocorrencia.setSituacaoOcorrencia(ocorrenciaForm.getSituacaoOcorrencia());
        ocorrencia.setDataOcorrencia(ocorrenciaForm.getDataOcorrencia());
        ocorrencia.setHoraInicioOcorrencia(ocorrenciaForm.getHoraInicioOcorrencia());
        ocorrencia.setHoraFimOcorrencia(ocorrenciaForm.getHoraFimOcorrencia());
        ocorrencia.setAssuntoDivulgacao(
                isBlank(ocorrenciaForm.getAssuntoDivulgacao()) ? null : ocorrenciaForm.getAssuntoDivulgacao());
        ocorrencia.setDetalheDivulgacao(
                isBlank(ocorrenciaForm.getDetalheDivulgacao()) ? null : ocorrenciaForm.getDetalheDivulgacao());
        ocorrencia.setLinkImgDivulgacao(
                isBlank(ocorrenciaForm.getLinkImgDivulgacao()) ? null : ocorrenciaForm.getLinkImgDivulgacao());
        ocorrencia.setLinkMaterialTema(
                isBlank(ocorrenciaForm.getLinkMaterialTema()) ? null : ocorrenciaForm.getLinkMaterialTema());
        ocorrencia.setObsEncerramento(
                isBlank(ocorrenciaForm.getObsEncerramento()) ? null : ocorrenciaForm.getObsEncerramento());
        ocorrencia.setBibliografia(isBlank(ocorrenciaForm.getBibliografia()) ? null : ocorrenciaForm.getBibliografia());
        ocorrencia.setQtdeParticipantes(ocorrenciaForm.getQtdeParticipantes());
        // Atualizar atividade se alterada
        if (ocorrenciaForm.getIdAtividade() != null && ocorrenciaForm.getIdAtividade().getId() != null) {
            atividadeRepository.findById(ocorrenciaForm.getIdAtividade().getId()).ifPresent(ocorrencia::setIdAtividade);
        }
        // Atualizar autor
        if (idAutorId != null) {
            Autor autor = autorRepository.findById(idAutorId).orElse(null);
            ocorrencia.setIdAutor(autor);
        } else {
            ocorrencia.setIdAutor(null);
        }
        // Atualizar dataAtualizacao
        ocorrencia.setDataAtualizacao(java.time.LocalDate.now());
        ocorrenciaAtividadeRepository.save(ocorrencia);
        Long atividadeId = (ocorrencia.getIdAtividade() != null && ocorrencia.getIdAtividade().getId() != null)
                ? ocorrencia.getIdAtividade().getId()
                : null;
        // Recupera origem do request param se não veio no binding
        String origemFinal = origem;
        if ((origemFinal == null || origemFinal.isEmpty()) &&
                model.asMap().containsKey(
                        "org.springframework.web.context.request.RequestContextListener.REQUEST_ATTRIBUTES")) {
            Object req = model.asMap()
                    .get("org.springframework.web.context.request.RequestContextListener.REQUEST_ATTRIBUTES");
            if (req instanceof jakarta.servlet.http.HttpServletRequest) {
                String paramOrigem = ((jakarta.servlet.http.HttpServletRequest) req).getParameter("origem");
                if (paramOrigem != null && !paramOrigem.isEmpty()) {
                    origemFinal = paramOrigem;
                }
            }
        }
        if (atividadeId != null) {
            String redirect = "/administrador/ocorrencias?atividadeId=" + atividadeId;
            if (origemFinal != null && !origemFinal.isEmpty()) {
                redirect += "&origem=" + origemFinal;
            }
            return "redirect:" + redirect;
        } else {
            // Se não houver atividade, volta para ocorrencias sem filtro
            return "redirect:/administrador/ocorrencias";
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletarOcorrencia(@PathVariable("id") Long id,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "atividadeId", required = false) String atividadeIdParam,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "situacao", required = false) String situacao,
            @RequestParam(value = "ordem", required = false) String ordem,
            @RequestParam(value = "dataInicio", required = false) String dataInicio,
            @RequestParam(value = "dataFim", required = false) String dataFim,
            @RequestParam(value = "temaOcorrencia", required = false) String temaOcorrencia,
            @RequestParam(value = "autorId", required = false) Long autorId,
            @RequestParam(value = "autorNome", required = false) String autorNome) {
        // Corrige atividadeId vindo como string tipo "[29, 29]" ou "29,29"
        Long atividadeId = null;
        if (atividadeIdParam != null && !atividadeIdParam.isEmpty()) {
            String clean = atividadeIdParam.replaceAll("[\\[\\]\s]", "");
            String[] parts = clean.split(",");
            if (parts.length > 0) {
                try {
                    atividadeId = Long.parseLong(parts[0]);
                } catch (Exception e) {
                    atividadeId = null;
                }
            }
        }
        // Verifica se há logs relacionados à ocorrência
        java.util.List<com.agendademais.entities.LogPostagem> logsRelacionados = logPostagemRepository
                .findByOcorrenciaAtividadeId(id);
        if (logsRelacionados != null && !logsRelacionados.isEmpty()) {
            // Não permite exclusão, redireciona com mensagem de erro
            StringBuilder redirect = new StringBuilder("/administrador/ocorrencias?");
            if (atividadeId != null)
                redirect.append("atividadeId=").append(atividadeId).append("&");
            if (origem != null)
                redirect.append("origem=").append(origem).append("&");
            if (page != null)
                redirect.append("page=").append(page).append("&");
            if (size != null)
                redirect.append("size=").append(size).append("&");
            if (situacao != null)
                redirect.append("situacao=").append(situacao).append("&");
            if (ordem != null)
                redirect.append("ordem=").append(ordem).append("&");
            if (dataInicio != null)
                redirect.append("dataInicio=").append(dataInicio).append("&");
            if (dataFim != null)
                redirect.append("dataFim=").append(dataFim).append("&");
            if (temaOcorrencia != null)
                redirect.append("temaOcorrencia=").append(temaOcorrencia).append("&");
            if (autorId != null)
                redirect.append("autorId=").append(autorId).append("&");
            if (autorNome != null)
                redirect.append("autorNome=").append(autorNome).append("&");
            if (redirect.charAt(redirect.length() - 1) == '&') {
                redirect.deleteCharAt(redirect.length() - 1);
            }
            redirect.append("&erroExclusao=1");
            return "redirect:" + redirect.toString();
        }
        ocorrenciaAtividadeRepository.deleteById(id);
        StringBuilder redirect = new StringBuilder("/administrador/ocorrencias?");
        if (atividadeId != null)
            redirect.append("atividadeId=").append(atividadeId).append("&");
        if (origem != null)
            redirect.append("origem=").append(origem).append("&");
        if (page != null)
            redirect.append("page=").append(page).append("&");
        if (size != null)
            redirect.append("size=").append(size).append("&");
        if (situacao != null)
            redirect.append("situacao=").append(situacao).append("&");
        if (ordem != null)
            redirect.append("ordem=").append(ordem).append("&");
        if (dataInicio != null)
            redirect.append("dataInicio=").append(dataInicio).append("&");
        if (dataFim != null)
            redirect.append("dataFim=").append(dataFim).append("&");
        if (temaOcorrencia != null)
            redirect.append("temaOcorrencia=").append(temaOcorrencia).append("&");
        if (autorId != null)
            redirect.append("autorId=").append(autorId).append("&");
        if (autorNome != null)
            redirect.append("autorNome=").append(autorNome).append("&");
        // Remove o último & se houver
        if (redirect.charAt(redirect.length() - 1) == '&') {
            redirect.deleteCharAt(redirect.length() - 1);
        }
        return "redirect:" + redirect.toString();
    }

    @GetMapping("/autocomplete-atividade")
    @ResponseBody
    public List<Atividade> autocompleteAtividade(@RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "id", required = false) Long id,
            HttpSession session) {
        // Busca por id (caso uso específico)
        if (id != null) {
            return atividadeRepository.findById(id).map(List::of).orElse(List.of());
        }
        // Recupera instituição logada
        var instituicao = session.getAttribute("instituicaoSelecionada");
        Long instituicaoId = null;
        if (instituicao != null && instituicao instanceof com.agendademais.entities.Instituicao) {
            instituicaoId = ((com.agendademais.entities.Instituicao) instituicao).getId();
        }
        if (instituicaoId == null) {
            return List.of(); // Não retorna nada se não houver instituição logada
        }
        if (term == null || term.isBlank()) {
            // Retorna todas as atividades da instituição logada
            return atividadeRepository.findByTituloAtividadeAndInstituicaoId("", instituicaoId);
        }
        return atividadeRepository.findByTituloAtividadeAndInstituicaoId(term, instituicaoId);
    }

    @GetMapping("/autocomplete-assunto")
    @ResponseBody
    public java.util.List<String> autocompleteAssunto(@RequestParam("term") String term) {
        if (term == null || term.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return ocorrenciaAtividadeRepository.findDistinctAssuntoDivulgacaoByTerm(term);
    }

    @GetMapping("/autocomplete-autor")
    @ResponseBody
    public List<Autor> autocompleteAutor(@RequestParam("term") String term,
            @RequestParam(value = "atividadeId", required = false) Long atividadeId) {
        // Busca diretamente autores vinculados a ocorrências da atividade (ou geral),
        // filtrando por nome/email
        return ocorrenciaAtividadeRepository.findDistinctAutoresByTermAndAtividadeId(term, atividadeId).stream()
                .limit(10)
                .toList();
    }

    @GetMapping("/autocomplete-tema")
    @ResponseBody
    public List<String> autocompleteTema(@RequestParam("term") String term) {
        return ocorrenciaAtividadeRepository.findDistinctTemaOcorrenciaByTerm(term);
    }

}
