package com.agendademais.controllers;

import com.agendademais.entities.Autor;
import com.agendademais.repositories.AutorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AutorFormController {

    // POST handler para salvar alterações do autor e redirecionar mantendo filtros
    // e paginação
    @org.springframework.web.bind.annotation.PostMapping("/administrador/autor-form/{id}")
    public String salvarAutor(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.ModelAttribute Autor autor,
            @org.springframework.web.bind.annotation.RequestParam(value = "origem", required = false, defaultValue = "usuarios") String origem,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroCodigo", required = false) String filtroCodigo,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroNome", required = false) String filtroNome,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroEmail", required = false) String filtroEmail,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroSubInstituicao", required = false) String filtroSubInstituicao,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroStatus", required = false) String filtroStatus,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroPais", required = false) String filtroPais,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroEstado", required = false) String filtroEstado,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroCidade", required = false) String filtroCidade,
            @org.springframework.web.bind.annotation.RequestParam(value = "size", required = false) Integer size,
            @org.springframework.web.bind.annotation.RequestParam(value = "page", required = false) Integer page,
            RedirectAttributes redirectAttributes) {
        // Salvar alterações do autor
        autor.setId(id); // Garante que o ID está correto
        autorRepository.save(autor);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Autor salvo com sucesso!");
        // Redireciona para a origem mantendo filtros e paginação
        StringBuilder redirectUrl = new StringBuilder("redirect:/administrador/usuarios?");
        if (filtroCodigo != null)
            redirectUrl.append("filtroCodigo=").append(filtroCodigo).append("&");
        if (filtroNome != null)
            redirectUrl.append("filtroNome=").append(filtroNome).append("&");
        if (filtroEmail != null)
            redirectUrl.append("filtroEmail=").append(filtroEmail).append("&");
        if (filtroSubInstituicao != null)
            redirectUrl.append("filtroSubInstituicao=").append(filtroSubInstituicao).append("&");
        if (filtroStatus != null)
            redirectUrl.append("filtroStatus=").append(filtroStatus).append("&");
        if (filtroPais != null)
            redirectUrl.append("filtroPais=").append(filtroPais).append("&");
        if (filtroEstado != null)
            redirectUrl.append("filtroEstado=").append(filtroEstado).append("&");
        if (filtroCidade != null)
            redirectUrl.append("filtroCidade=").append(filtroCidade).append("&");
        if (size != null)
            redirectUrl.append("size=").append(size).append("&");
        if (page != null)
            redirectUrl.append("page=").append(page).append("&");
        // Remove o último & se houver
        if (redirectUrl.charAt(redirectUrl.length() - 1) == '&') {
            redirectUrl.deleteCharAt(redirectUrl.length() - 1);
        }
        return redirectUrl.toString();
    }

    @Autowired
    private AutorRepository autorRepository;

    @GetMapping("/administrador/autor-form/{id}")
    public String exibirAutorForm(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(value = "origem", required = false, defaultValue = "usuarios") String origem,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroCodigo", required = false) String filtroCodigo,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroNome", required = false) String filtroNome,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroEmail", required = false) String filtroEmail,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroSubInstituicao", required = false) String filtroSubInstituicao,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroStatus", required = false) String filtroStatus,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroPais", required = false) String filtroPais,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroEstado", required = false) String filtroEstado,
            @org.springframework.web.bind.annotation.RequestParam(value = "filtroCidade", required = false) String filtroCidade,
            @org.springframework.web.bind.annotation.RequestParam(value = "size", required = false) Integer size,
            @org.springframework.web.bind.annotation.RequestParam(value = "page", required = false) Integer page,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Autor> autorOpt = autorRepository.findById(id);
        if (autorOpt.isPresent()) {
            model.addAttribute("autor", autorOpt.get());
            model.addAttribute("origem", origem);
            model.addAttribute("filtroCodigo", filtroCodigo);
            model.addAttribute("filtroNome", filtroNome);
            model.addAttribute("filtroEmail", filtroEmail);
            model.addAttribute("filtroSubInstituicao", filtroSubInstituicao);
            model.addAttribute("filtroStatus", filtroStatus);
            model.addAttribute("filtroPais", filtroPais);
            model.addAttribute("filtroEstado", filtroEstado);
            model.addAttribute("filtroCidade", filtroCidade);
            model.addAttribute("size", size);
            model.addAttribute("page", page);
            return "administrador/autor-form";
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro", "Autor não encontrado.");
            return "redirect:/gestao-usuarios/lista-usuarios";
        }
    }
}
