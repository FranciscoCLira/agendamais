package com.agendademais.advice;

import com.agendademais.exceptions.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Se for redirect, joga mensagem no flash
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("inscricao-tipo-atividade")) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/inscricao-tipo-atividade";
        }
        // Se não for redirect, mostra erro direto na página atual
        model.addAttribute("mensagemErro", ex.getMessage());
        return "erro";
    }

    // Interceptar e tratar erro HTTP 405 (Method Not Allowed)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, Model model) {
        model.addAttribute("mensagemErro",
                "Ação não permitida. Por favor, retorne à página inicial ou faça login novamente.");
        return "acesso-negado"; // Use uma página amigável!
    }
}
