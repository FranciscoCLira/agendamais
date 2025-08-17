package com.agendademais.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilitário para validação e formatação de números de telefone
 */
public class PhoneNumberUtil {
    
    // Padrão final desejado: +55-99-99999-9999
    private static final Pattern STANDARD_PATTERN = Pattern.compile("^\\+55-\\d{2}-\\d{5}-\\d{4}$");
    
    /**
     * Valida se o número de telefone está em formato válido
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        String cleanNumber = phoneNumber.trim();
        
        // Verifica se já está no padrão final
        if (STANDARD_PATTERN.matcher(cleanNumber).matches()) {
            return true;
        }
        
        // Verifica se pode ser convertido
        return canBeFormatted(cleanNumber);
    }
    
    /**
     * Formata o número de telefone para o padrão +55-99-99999-9999
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número de telefone não pode ser vazio");
        }
        
        String cleanNumber = phoneNumber.trim().replaceAll("[\\s\\-\\(\\)\\.]", "");
        
        // Trata notação científica (ex: 3.51912E+11 -> 35191234567)
        if (cleanNumber.contains("E+") || cleanNumber.contains("e+")) {
            try {
                // Converte notação científica para número normal
                double scientificNumber = Double.parseDouble(phoneNumber.trim());
                // Remove parte decimal se for zero
                if (scientificNumber == Math.floor(scientificNumber)) {
                    cleanNumber = String.valueOf((long) scientificNumber);
                } else {
                    cleanNumber = String.valueOf(scientificNumber);
                    // Remove .0 se existir
                    if (cleanNumber.endsWith(".0")) {
                        cleanNumber = cleanNumber.substring(0, cleanNumber.length() - 2);
                    }
                }
            } catch (NumberFormatException e) {
                // Se não conseguir converter, continua com o valor original
            }
        }
        
        // Se já está no padrão correto, retorna como está
        if (STANDARD_PATTERN.matcher(phoneNumber.trim()).matches()) {
            return phoneNumber.trim();
        }
        
        // Remove +55 do início se existir
        if (cleanNumber.startsWith("+55")) {
            cleanNumber = cleanNumber.substring(3);
        } else if (cleanNumber.startsWith("55") && cleanNumber.length() >= 12) {
            cleanNumber = cleanNumber.substring(2);
        }
        
        // Remove zero inicial se existir
        if (cleanNumber.startsWith("0") && cleanNumber.length() >= 10) {
            cleanNumber = cleanNumber.substring(1);
        }
        
        // Valida o tamanho (deve ter 10 ou 11 dígitos)
        if (cleanNumber.length() < 10 || cleanNumber.length() > 11) {
            throw new IllegalArgumentException("Número de telefone deve ter 10 ou 11 dígitos: " + phoneNumber);
        }
        
        // Se tem 10 dígitos, adiciona 9 no início (celular)
        if (cleanNumber.length() == 10) {
            cleanNumber = "9" + cleanNumber;
        }
        
        // Valida se tem 11 dígitos
        if (cleanNumber.length() != 11) {
            throw new IllegalArgumentException("Erro na formatação do número: " + phoneNumber);
        }
        
        // Extrai DDD e número
        String ddd = cleanNumber.substring(0, 2);
        String numero = cleanNumber.substring(2);
        
        // Valida DDD (11 a 99)
        try {
            int dddInt = Integer.parseInt(ddd);
            if (dddInt < 11 || dddInt > 99) {
                throw new IllegalArgumentException("DDD inválido: " + ddd);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DDD deve ser numérico: " + ddd);
        }
        
        // Formata: +55-DD-99999-9999
        return String.format("+55-%s-%s-%s", 
                           ddd, 
                           numero.substring(0, 5), 
                           numero.substring(5));
    }
    
    /**
     * Verifica se o número pode ser formatado
     */
    private static boolean canBeFormatted(String phoneNumber) {
        try {
            formatPhoneNumber(phoneNumber);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Formata múltiplos números de telefone
     */
    public static List<String> formatPhoneNumbers(List<String> phoneNumbers) {
        List<String> formattedNumbers = new ArrayList<>();
        for (String number : phoneNumbers) {
            if (number != null && !number.trim().isEmpty()) {
                formattedNumbers.add(formatPhoneNumber(number));
            }
        }
        return formattedNumbers;
    }
    
    /**
     * Valida múltiplos números de telefone
     */
    public static boolean areAllValidPhoneNumbers(List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return false;
        }
        
        for (String number : phoneNumbers) {
            if (!isValidPhoneNumber(number)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Retorna mensagem de erro detalhada para números inválidos
     */
    public static String getValidationError(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Número de telefone está vazio";
        }
        
        try {
            formatPhoneNumber(phoneNumber);
            return null; // Válido
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
    
    /**
     * Método para teste de conversão de notação científica
     */
    public static void main(String[] args) {
        System.out.println("=== Teste de Conversão de Notação Científica ===");
        
        String[] testNumbers = {
            "3.51912E+11",    // Portugal
            "3.51923E+11",    // Portugal  
            "3.51935E+11",    // Portugal
            "35191234567",    // Número normal
            "+5511999999999",  // Já formatado
            "11999999999"     // Número normal brasileiro
        };
        
        for (String number : testNumbers) {
            try {
                System.out.println("Input: " + number);
                String formatted = formatPhoneNumber(number);
                System.out.println("Output: " + formatted);
                System.out.println("---");
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                System.out.println("---");
            }
        }
    }
}
