package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.enums.FuncaoAutor;
import com.agendademais.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;

import jakarta.servlet.http.HttpSession;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller para gerenciar dados de autor do usuário
 * Disponível para usuários com nível 2 (autor) ou superior
 */
@Controller
@RequestMapping("/dados-autor")
public class DadosAutorController {

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;
    
    @Autowired
    private FuncaoAutorCustomizadaRepository funcaoCustomizadaRepository;

    /**
     * Configura os editores de propriedades para conversão de tipos
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        
        // Converter personalizado para FuncaoAutor
        binder.registerCustomEditor(FuncaoAutor.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        // Tentar converter diretamente pelo nome do enum
                        setValue(FuncaoAutor.valueOf(text.trim()));
                    } catch (IllegalArgumentException e) {
                        // Se falhar, tentar encontrar pela descrição
                        for (FuncaoAutor funcao : FuncaoAutor.values()) {
                            if (funcao.getDescricao().equals(text.trim())) {
                                setValue(funcao);
                                return;
                            }
                        }
                        setValue(null);
                    }
                }
            }
        });
    }

    /**
     * Exibe os dados de autor do usuário
     */
    @GetMapping
    public String exibirDadosAutor(
            @RequestParam(value = "origem", required = false, defaultValue = "autor") String origem,
            HttpSession session, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        if (nivelAcesso == null || nivelAcesso < 2) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para autores.");
            return "redirect:/participante";
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        // Dados para o cabeçalho
        model.addAttribute("nomeInstituicao", instituicao != null ? instituicao.getNomeInstituicao() : "");
        model.addAttribute("nomeUsuario", usuario.getUsername());
        model.addAttribute("nomePessoa", pessoa.getNomePessoa());
        
        // Adicionar origem para navegação
        model.addAttribute("origem", origem);

        // Buscar dados de autor
        Optional<Autor> autorOpt = autorRepository.findByIdPessoa(pessoa);

        if (autorOpt.isPresent()) {
            model.addAttribute("autor", autorOpt.get());
            model.addAttribute("possuiDadosAutor", true);
        } else {
            // Criar objeto vazio para o formulário
            Autor autorVazio = new Autor();
            autorVazio.setIdPessoa(pessoa);
            model.addAttribute("autor", autorVazio);
            model.addAttribute("possuiDadosAutor", false);
        }

        // Buscar vínculo com sub-instituição da instituição atual
        if (instituicao != null) {
            Optional<PessoaSubInstituicao> vinculoSubInstituicao = pessoaSubInstituicaoRepository
                .findByPessoaAndInstituicao(pessoa, instituicao);
            
            if (vinculoSubInstituicao.isPresent()) {
                model.addAttribute("vinculoSubInstituicao", vinculoSubInstituicao.get());
                model.addAttribute("possuiVinculoSubInstituicao", true);
            } else {
                model.addAttribute("possuiVinculoSubInstituicao", false);
            }
        } else {
            model.addAttribute("possuiVinculoSubInstituicao", false);
        }

        return "profile/dados-autor";
    }

    /**
     * Salva ou atualiza os dados de autor
     */
    @PostMapping("/salvar")
    public String salvarDadosAutor(
            @ModelAttribute Autor autor,
            @RequestParam(value = "funcaoAutor", required = false) String funcaoAutorParam,
            @RequestParam(value = "origem", required = false, defaultValue = "autor") String origem,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        if (nivelAcesso == null || nivelAcesso < 2) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para autores.");
            return "redirect:/participante";
        }

        try {
            // Processar função do autor
            if (funcaoAutorParam != null) {
                if (funcaoAutorParam.startsWith("CUSTOM_")) {
                    // É uma função personalizada
                    Long customId = Long.parseLong(funcaoAutorParam.substring(7));
                    FuncaoAutorCustomizada customFunc = funcaoCustomizadaRepository
                        .findById(customId).orElse(null);
                    if (customFunc != null) {
                        autor.setFuncaoAutorCustomizada(customFunc);
                        autor.setFuncaoAutor(null); // Limpar a enum
                        autor.setFuncaoAutorOutra(null); // Limpar "outra" quando for personalizada
                    }
                } else {
                    // É uma função padrão (enum)
                    try {
                        FuncaoAutor funcaoEnum = FuncaoAutor.valueOf(funcaoAutorParam);
                        autor.setFuncaoAutor(funcaoEnum);
                        autor.setFuncaoAutorCustomizada(null); // Limpar a personalizada
                        // Se não for "OUTRA", limpar o campo funcaoAutorOutra
                        if (funcaoEnum != FuncaoAutor.OUTRA) {
                            autor.setFuncaoAutorOutra(null);
                        }
                    } catch (IllegalArgumentException e) {
                        // Valor inválido, usar null
                        autor.setFuncaoAutor(null);
                        autor.setFuncaoAutorCustomizada(null);
                    }
                }
            }

            Pessoa pessoa = usuario.getPessoa();

            // Buscar autor existente ou criar novo
            Optional<Autor> autorExistenteOpt = autorRepository.findByIdPessoa(pessoa);

            if (autorExistenteOpt.isPresent()) {
                // Atualizar autor existente
                Autor autorExistente = autorExistenteOpt.get();
                autorExistente.setFuncaoAutor(autor.getFuncaoAutor());
                autorExistente.setFuncaoAutorCustomizada(autor.getFuncaoAutorCustomizada());
                autorExistente.setFuncaoAutorOutra(autor.getFuncaoAutorOutra());
                autorExistente.setSituacaoAutor("A"); // SEMPRE ATIVO - campo removido da view
                autorExistente.setCurriculoFuncaoAutor(autor.getCurriculoFuncaoAutor());
                autorExistente.setLinkImgAutor(autor.getLinkImgAutor());
                autorExistente.setLinkMaterialAutor(autor.getLinkMaterialAutor());
                autorExistente.setDataUltimaAtualizacao(LocalDate.now());

                autorRepository.save(autorExistente);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados de autor atualizados com sucesso!");

            } else {
                // Criar novo autor
                autor.setIdPessoa(pessoa);
                autor.setSituacaoAutor("A"); // SEMPRE ATIVO - campo removido da view
                autor.setDataUltimaAtualizacao(LocalDate.now());

                autorRepository.save(autor);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados de autor criados com sucesso!");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao salvar dados de autor: " + ex.getMessage());
        }

        return "redirect:/dados-autor?origem=" + origem;
    }

    /**
     * Exibe formulário para editar dados de autor
     */
    @GetMapping("/editar")
    public String exibirFormularioEdicao(
            @RequestParam(value = "origem", required = false, defaultValue = "autor") String origem,
            HttpSession session, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Instituicao instituicao = (Instituicao) session.getAttribute("instituicaoSelecionada");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        if (nivelAcesso == null || nivelAcesso < 2) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Acesso negado. Funcionalidade disponível apenas para autores.");
            return "redirect:/participante";
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não possui cadastro de pessoa.");
            return "redirect:/acesso";
        }

        // Dados para o cabeçalho
        model.addAttribute("nomeInstituicao", instituicao != null ? instituicao.getNomeInstituicao() : "");
        model.addAttribute("nomeUsuario", usuario.getUsername());
        model.addAttribute("nomePessoa", pessoa.getNomePessoa());
        
        // Adicionar origem para navegação
        model.addAttribute("origem", origem);

        // Buscar dados de autor
        Optional<Autor> autorOpt = autorRepository.findByIdPessoa(pessoa);

        if (autorOpt.isPresent()) {
            model.addAttribute("autor", autorOpt.get());
        } else {
            // Criar objeto vazio para o formulário
            Autor autorVazio = new Autor();
            autorVazio.setIdPessoa(pessoa);
            autorVazio.setSituacaoAutor("A");
            model.addAttribute("autor", autorVazio);
        }

        // Buscar vínculo com sub-instituição da instituição atual
        if (instituicao != null) {
            Optional<PessoaSubInstituicao> vinculoSubInstituicao = pessoaSubInstituicaoRepository
                .findByPessoaAndInstituicao(pessoa, instituicao);
            
            if (vinculoSubInstituicao.isPresent()) {
                model.addAttribute("vinculoSubInstituicao", vinculoSubInstituicao.get());
                model.addAttribute("possuiVinculoSubInstituicao", true);
            } else {
                model.addAttribute("possuiVinculoSubInstituicao", false);
            }
        } else {
            model.addAttribute("possuiVinculoSubInstituicao", false);
        }

        // Buscar funções personalizadas ativas
        model.addAttribute("funcoesPersonalizadas", funcaoCustomizadaRepository.findByAtivaTrue());

        return "profile/dados-autor-editar";
    }
}
