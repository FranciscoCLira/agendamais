package com.agendademais.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object errorMessage = request.getAttribute("javax.servlet.error.message");
        Object requestUri = request.getAttribute("javax.servlet.error.request_uri");
        Object exception = request.getAttribute("javax.servlet.error.exception");

        // Debug - imprimir informações do erro
        System.out.println("*** DEBUG CUSTOM ERROR CONTROLLER ***");
        System.out.println("Status: " + status);
        System.out.println("ErrorMessage: " + errorMessage);
        System.out.println("RequestUri: " + requestUri);
        System.out.println("Exception: " + (exception != null ? exception.getClass().getSimpleName() : "null"));

        // Valores padrão
        String errorCode = "Desconhecido";
        String errorTitle = "Erro";
        String errorDescription = "Ocorreu um erro inesperado.";
        String errorDetails = "";

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            errorCode = statusCode.toString();

            System.out.println("StatusCode processado: " + statusCode);

            // Adicionar detalhes do erro
            if (requestUri != null) {
                errorDetails += "URL: " + requestUri.toString() + "\n";
            }
            if (errorMessage != null && !errorMessage.toString().isEmpty()) {
                errorDetails += "Mensagem: " + errorMessage.toString() + "\n";
            }
            if (exception != null) {
                errorDetails += "Tipo: " + exception.getClass().getSimpleName() + "\n";
            }

            if (statusCode == 404) {
                errorTitle = "Página não encontrada (404)";
                errorDescription = "A página que você está procurando não existe ou foi removida.";
                model.addAttribute("homeLink", "/acesso");
                model.addAttribute("homeLinkText", "Voltar ao Login");
                model.addAttribute("errorCode", errorCode);
                model.addAttribute("errorTitle", errorTitle);
                model.addAttribute("errorMessage", errorDescription);
                model.addAttribute("errorDetails", errorDetails);
                return "error/404";
            } else if (statusCode == 500) {
                errorTitle = "Erro interno do servidor (500)";
                errorDescription = "Ocorreu um erro interno no servidor. Tente novamente em alguns minutos.";
                model.addAttribute("homeLink", "/acesso");
                model.addAttribute("homeLinkText", "Voltar ao Login");
                model.addAttribute("errorCode", errorCode);
                model.addAttribute("errorTitle", errorTitle);
                model.addAttribute("errorMessage", errorDescription);
                model.addAttribute("errorDetails", errorDetails);
                return "error/500";
            } else if (statusCode == 403) {
                errorTitle = "Acesso negado (403)";
                errorDescription = "Você não tem permissão para acessar este recurso.";
                model.addAttribute("homeLink", "/acesso");
                model.addAttribute("homeLinkText", "Voltar ao Login");
                model.addAttribute("errorCode", errorCode);
                model.addAttribute("errorTitle", errorTitle);
                model.addAttribute("errorMessage", errorDescription);
                model.addAttribute("errorDetails", errorDetails);
                return "acesso-negado";
            } else {
                // Outros códigos de erro
                errorTitle = "Erro " + statusCode;
                errorDescription = "Ocorreu um erro HTTP " + statusCode + ".";

                // Adicionar descrições específicas para outros códigos
                switch (statusCode) {
                    case 400:
                        errorDescription = "Requisição inválida (400). Verifique os dados enviados.";
                        break;
                    case 401:
                        errorDescription = "Não autorizado (401). Faça login para continuar.";
                        break;
                    case 405:
                        errorDescription = "Método não permitido (405). Operação não suportada.";
                        break;
                    case 415:
                        errorDescription = "Tipo de mídia não suportado (415).";
                        break;
                }
            }
        }

        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorDescription);
        model.addAttribute("errorDetails", errorDetails);
        model.addAttribute("homeLink", "/acesso");
        model.addAttribute("homeLinkText", "Voltar ao Login");

        // Debug - imprimir valores finais
        System.out.println("*** VALORES FINAIS ENVIADOS AO TEMPLATE ***");
        System.out.println("errorCode: " + errorCode);
        System.out.println("errorTitle: " + errorTitle);
        System.out.println("errorMessage: " + errorDescription);
        System.out.println("Template retornado: error/generic");

        return "error/generic";
    }
}
