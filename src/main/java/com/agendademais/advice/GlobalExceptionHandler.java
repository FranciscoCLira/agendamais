package com.agendademais.advice;

import com.agendademais.exceptions.BusinessException;
import org.springframework.ui.Model;
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
        if (referer != null && referer.contains("inscricao-form")) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/participante/inscricao-form";
        }
        // Se não for redirect, mostra erro direto na página atual
        model.addAttribute("mensagemErro", ex.getMessage());
        return "erro";
    }
}
