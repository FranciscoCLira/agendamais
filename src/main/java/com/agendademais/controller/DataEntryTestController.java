package com.agendademais.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller para testes da funcionalidade de Data Entry
 */
@Controller
@RequestMapping("/test")
public class DataEntryTestController {
    
    @GetMapping("/dataentry")
    public String testePage(Model model) {
        model.addAttribute("pageTitle", "Teste - Carga Massiva");
        return "test/dataentry-test";
    }
    
    @GetMapping("/info")
    @ResponseBody
    public String infoApi() {
        return """
            {
                "status": "OK",
                "api": "DataEntry - Carga Massiva",
                "endpoints": [
                    "GET /admin/dataentry - Página principal",
                    "POST /admin/dataentry/upload - Upload e processamento",
                    "POST /admin/dataentry/validate - Validação de arquivo",
                    "GET /admin/dataentry/info - Informações da API",
                    "GET /admin/dataentry/download/{filename} - Download de arquivo"
                ],
                "formatos": ["CSV", "XLSX", "XLS"],
                "tipos_carga": ["teste", "real"],
                "exemplo_csv": "email;nome;celular;pais;estado;cidade"
            }
            """;
    }
}
