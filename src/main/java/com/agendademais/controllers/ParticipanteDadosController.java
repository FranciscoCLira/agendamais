package com.agendademais.controllers;

import com.agendademais.dtos.PessoaForm;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.PessoaRepository;

import jakarta.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/participante/meus-dados")
public class ParticipanteDadosController {
    
    @Autowired
    private PessoaRepository pessoaRepository;

    // --- GET: exibir dados preenchidos ---
    @GetMapping
    public String exibirMeusDados(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null || usuario.getPessoa() == null) {
            model.addAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/login";
        }

        Pessoa pessoa = usuario.getPessoa();

        // Monta o DTO para popular o formulário
        
        PessoaForm form = new PessoaForm();
        
        form.setNomePessoa(pessoa.getNomePessoa());
        form.setEmailPessoa(pessoa.getEmailPessoa());
        form.setCelularPessoa(pessoa.getCelularPessoa());
        
        form.setCurriculoPessoal(pessoa.getCurriculoPessoal());
        form.setComentarios(pessoa.getComentarios());

        // PAÍS
        String pais = pessoa.getNomePaisPessoa();
        form.setNomePaisSelect(pais != null ? pais : "");
        if ("Outro".equals(pais)) form.setPaisOutro(pais);

        if ("Brasil".equals(pais)) {
            form.setNomeEstadoSelect(pessoa.getNomeEstadoPessoa());
            form.setEstadoOutro("");
            form.setNomeCidadeSelect(pessoa.getNomeCidadePessoa());
            form.setCidadeOutro("");
        } else {
            form.setNomeEstadoSelect("");
            form.setEstadoOutro(pessoa.getNomeEstadoPessoa());
            form.setNomeCidadeSelect("");
            form.setCidadeOutro(pessoa.getNomeCidadePessoa());
        }        
        
        // Adicione a lista de UFs
        List<String> ufs = Arrays.asList("AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO");
        model.addAttribute("ufs", ufs);

        // Preencher as cidades do estado atual, se houver
        Map<String, List<String>> cidadesPorEstado = new HashMap<>();
        cidadesPorEstado.put("RJ", Arrays.asList("Rio de Janeiro", "Niterói",  "Campos", "Volta Redonda", "Tijuca"));
        cidadesPorEstado.put("SP", Arrays.asList("São Paulo", "Santos", "Mogi das Cruzes", "Campinas"));
        // ... outros estados
        
        // Se estado já selecionado, popula a lista de cidades
        String estado = form.getNomeEstadoSelect();
        List<String> cidades = cidadesPorEstado.getOrDefault(estado, Collections.emptyList());

        // TESTE 
        // form.setNomeCidadeSelect("Rio de Janeiro");
        // model.addAttribute("cidades", Arrays.asList("Rio de Janeiro", "Niterói"));
        
        System.out.println("*** ");         
        System.out.println("DTO.getNomeCidadeSelect() = " + form.getNomeCidadeSelect());
        System.out.println("Cidades: " + cidades);
        
        model.addAttribute("cidades", cidades);
        
        model.addAttribute("pessoaForm", form);

        // CONFERIR VALORES ENVIADOS PARA A VIEW NO CONSOLE 
        System.out.println("DTO enviado: '" + form.getNomePaisSelect() + "' / '" + form.getNomeEstadoSelect()   + "' / '" + form.getNomeCidadeSelect() + "'");
        System.out.println("*** "); 
        
        return "participante/meus-dados";
    }
    
    // --- POST: SALVAR ALTERAÇÕES ---
    @PostMapping("/salvar")
    public String salvarMeusDados(@ModelAttribute PessoaForm pessoaForm, HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null || usuario.getPessoa() == null) {
            return "redirect:/login";
        }
        Pessoa pessoa = usuario.getPessoa();

        pessoa.setNomePessoa(pessoaForm.getNomePessoa());
        pessoa.setEmailPessoa(pessoaForm.getEmailPessoa());
        pessoa.setCelularPessoa(pessoaForm.getCelularPessoa());
        
        pessoa.setCurriculoPessoal(pessoaForm.getCurriculoPessoal());
        pessoa.setComentarios(pessoaForm.getComentarios());

        // País/Estado/Cidade
        String paisFinal = "Outro".equals(pessoaForm.getNomePaisSelect())
                ? pessoaForm.getPaisOutro()
                : pessoaForm.getNomePaisSelect();

        pessoa.setNomePaisPessoa(paisFinal);

        String estadoFinal;
        if ("Brasil".equals(pessoaForm.getNomePaisSelect())) {
            estadoFinal = pessoaForm.getNomeEstadoSelect();
        } else {
            estadoFinal = pessoaForm.getEstadoOutro();
        }
        pessoa.setNomeEstadoPessoa(estadoFinal);

        String cidadeFinal;
        
//        if ("Brasil".equals(pessoaForm.getNomePaisSelect())) {
//            cidadeFinal = (pessoaForm.getNomeCidadeSelect() != null && !pessoaForm.getNomeCidadeSelect().isBlank())
//                    ? pessoaForm.getNomeCidadeSelect()
//                    : pessoaForm.getCidadeOutro();
//        } else {
//            cidadeFinal = pessoaForm.getCidadeOutro();
//        }
//        pessoa.setNomeCidadePessoa(cidadeFinal);

        if ("Outro".equals(pessoaForm.getNomeCidadeSelect())) {
            cidadeFinal = pessoaForm.getCidadeOutro();
        } else {
            cidadeFinal = pessoaForm.getNomeCidadeSelect();
        }
        pessoa.setNomeCidadePessoa(cidadeFinal);        
        
        
        // Salvar alterações (persistência automática se estiver usando session, senão salve via repositório)
        pessoaRepository.save(pessoa);

        // Atualiza também a sessão
        session.setAttribute("usuarioLogado", usuario);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados alterados com sucesso.");
        return "redirect:/participante/meus-dados";
    }
}

