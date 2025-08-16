package com.agendademais.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Utilitário para validação de dados do CSV
 */
public class CsvValidationUtil {
    
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private String severity; // "error", "warning", "info"
        
        public ValidationResult(boolean valid, String message, String severity) {
            this.valid = valid;
            this.message = message;
            this.severity = severity;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getSeverity() { return severity; }
    }
    
    /**
     * Valida um email
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email é obrigatório", "error");
        }
        
        email = email.trim();
        if (!email.contains("@") || !email.contains(".")) {
            return new ValidationResult(false, "Formato de email inválido: " + email, "error");
        }
        
        if (email.length() > 100) {
            return new ValidationResult(false, "Email muito longo (máx. 100 caracteres)", "error");
        }
        
        return new ValidationResult(true, "Email válido", "info");
    }
    
    /**
     * Valida um nome
     */
    public static ValidationResult validateNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return new ValidationResult(false, "Nome é obrigatório", "error");
        }
        
        nome = nome.trim();
        if (nome.length() < 2) {
            return new ValidationResult(false, "Nome muito curto (mín. 2 caracteres)", "error");
        }
        
        if (nome.length() > 100) {
            return new ValidationResult(false, "Nome muito longo (máx. 100 caracteres)", "error");
        }
        
        return new ValidationResult(true, "Nome válido", "info");
    }
    
    /**
     * Valida dados geográficos
     */
    public static List<ValidationResult> validateGeografia(String pais, String estado, String cidade) {
        List<ValidationResult> results = new ArrayList<>();
        
        // Validação de país
        if (pais == null || pais.trim().isEmpty()) {
            results.add(new ValidationResult(false, "País é obrigatório", "error"));
        } else if (pais.trim().length() > 50) {
            results.add(new ValidationResult(false, "Nome do país muito longo", "warning"));
        }
        
        // Validação de estado
        if (estado == null || estado.trim().isEmpty()) {
            results.add(new ValidationResult(false, "Estado é obrigatório", "error"));
        } else if (estado.trim().length() > 50) {
            results.add(new ValidationResult(false, "Nome do estado muito longo", "warning"));
        }
        
        // Validação de cidade
        if (cidade == null || cidade.trim().isEmpty()) {
            results.add(new ValidationResult(false, "Cidade é obrigatória", "error"));
        } else if (cidade.trim().length() > 100) {
            results.add(new ValidationResult(false, "Nome da cidade muito longo", "warning"));
        }
        
        // Se tudo OK
        if (results.isEmpty()) {
            results.add(new ValidationResult(true, "Dados geográficos válidos", "info"));
        }
        
        return results;
    }
    
    /**
     * Valida IDs numéricos
     */
    public static ValidationResult validateNumericId(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return new ValidationResult(true, fieldName + " não informado (opcional)", "info");
        }
        
        try {
            Long.parseLong(value.trim());
            return new ValidationResult(true, fieldName + " válido", "info");
        } catch (NumberFormatException e) {
            return new ValidationResult(false, fieldName + " deve ser numérico: " + value, "error");
        }
    }
    
    /**
     * Valida linha completa do CSV
     */
    public static List<ValidationResult> validateCsvLine(
            String email, String nome, String celular, 
            String pais, String estado, String cidade,
            String instituicaoId, String subInstituicaoId) {
        
        List<ValidationResult> results = new ArrayList<>();
        
        // Validações obrigatórias
        results.add(validateEmail(email));
        results.add(validateNome(nome));
        
        // Telefone
        if (celular != null && !celular.trim().isEmpty()) {
            String phoneError = PhoneNumberUtil.getValidationError(celular);
            if (phoneError != null) {
                results.add(new ValidationResult(false, "Celular: " + phoneError, "warning"));
            } else {
                results.add(new ValidationResult(true, "Celular válido", "info"));
            }
        } else {
            results.add(new ValidationResult(false, "Celular é obrigatório", "error"));
        }
        
        // Geografia
        results.addAll(validateGeografia(pais, estado, cidade));
        
        // IDs opcionais
        results.add(validateNumericId(instituicaoId, "ID da Instituição"));
        results.add(validateNumericId(subInstituicaoId, "ID da Sub-Instituição"));
        
        return results;
    }
}
