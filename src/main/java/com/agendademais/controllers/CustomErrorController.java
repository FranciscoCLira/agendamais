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
        // Tentar múltiplas formas de capturar informações do erro
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object errorMessage = request.getAttribute("javax.servlet.error.message");
        Object requestUri = request.getAttribute("javax.servlet.error.request_uri");
        Object exception = request.getAttribute("javax.servlet.error.exception");
        
        // Fallback para obter status da resposta HTTP
        if (status == null) {
            status = request.getAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR");
            if (status == null) {
                // Tentar obter do parâmetro ou header
                String statusParam = request.getParameter("status");
                if (statusParam != null) {
                    status = statusParam;
                } else {
                    status = 500; // Assumir 500 se não conseguir obter
                }
            }
        }
        
        // Fallback para URI
        if (requestUri == null) {
            requestUri = request.getRequestURI();
        }

        // Debug - imprimir informações do erro
        System.out.println("*** DEBUG CUSTOM ERROR CONTROLLER ***");
        System.out.println("Status: " + status);
        System.out.println("ErrorMessage: " + errorMessage);
        System.out.println("RequestUri: " + requestUri);
        System.out.println("Exception: " + exception);
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Query String: " + request.getQueryString());
        
        // Se há uma exceção, imprimir stack trace completo
        if (exception instanceof Exception) {
            System.out.println("*** STACK TRACE COMPLETO ***");
            ((Exception) exception).printStackTrace();
        }

        // Valores padrão
        String errorCode = "Desconhecido";
        String errorTitle = "Erro";
        String errorDescription = "Ocorreu um erro inesperado.";
        
        // Adicionar informações específicas do erro no template
        if (status != null) {
            errorCode = status.toString();
            model.addAttribute("httpStatus", status);
        }
        
        if (exception instanceof Exception) {
            Exception ex = (Exception) exception;
            errorDescription = "Erro: " + ex.getClass().getSimpleName();
            if (ex.getMessage() != null) {
                errorDescription += " - " + ex.getMessage();
            }
            model.addAttribute("exceptionType", ex.getClass().getSimpleName());
            model.addAttribute("exceptionMessage", ex.getMessage());
        }
        
        if (requestUri != null) {
            model.addAttribute("requestUri", requestUri);
        }
        String errorDetails = "";
        
        // Se o status ainda for null mas há exceção, assumir erro 500
        if (status == null && exception != null) {
            status = 500;
            System.out.println("*** FORÇANDO STATUS 500 DEVIDO À EXCEÇÃO ***");
        }
        
        // Se ainda não temos status mas a URI indica erro de servidor, assumir 500
        if (status == null && requestUri != null && requestUri.toString().contains("/administrador/")) {
            status = 500;
            System.out.println("*** FORÇANDO STATUS 500 DEVIDO À URI ADMINISTRATIVA ***");
        }

        if (status != null) {
            Integer statusCode;
            try {
                statusCode = Integer.valueOf(status.toString());
                errorCode = statusCode.toString();
            } catch (NumberFormatException e) {
                // Se não conseguir converter, assume 500
                statusCode = 500;
                errorCode = "500";
                System.out.println("*** ERRO NA CONVERSÃO DO STATUS, ASSUMINDO 500 ***");
            }

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
                
                // Adicionar informações técnicas detalhadas para erro 500
                model.addAttribute("timestamp", new java.util.Date());
                model.addAttribute("httpMethod", request.getMethod());
                model.addAttribute("userAgent", request.getHeader("User-Agent"));
                model.addAttribute("remoteAddr", request.getRemoteAddr());
                model.addAttribute("sessionId", request.getSession(false) != null ? request.getSession().getId() : "N/A");
                
                // Adicionar parâmetros da requisição
                StringBuilder parameters = new StringBuilder();
                request.getParameterMap().forEach((key, values) -> {
                    parameters.append(key).append("=").append(String.join(",", values)).append("\n");
                });
                model.addAttribute("requestParameters", parameters.toString());
                
                // Adicionar headers importantes
                StringBuilder headers = new StringBuilder();
                headers.append("Accept: ").append(request.getHeader("Accept")).append("\n");
                headers.append("Accept-Language: ").append(request.getHeader("Accept-Language")).append("\n");
                headers.append("Content-Type: ").append(request.getHeader("Content-Type")).append("\n");
                headers.append("Referer: ").append(request.getHeader("Referer")).append("\n");
                model.addAttribute("requestHeaders", headers.toString());
                
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
