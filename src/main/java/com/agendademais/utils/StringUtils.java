package com.agendademais.utils;

import java.text.Normalizer;

public class StringUtils {

    /**
     * Formata celular só-números para exibição (+55-99-99999-9999)
     */
    public static String formatarCelularParaExibicao(String celular) {
        if (celular == null || celular.length() != 13)
            return celular;
        // Exemplo: 5511999999999 -> +55-11-99999-9999
        return String.format("+55-%s-%s-%s",
                celular.substring(2, 4), // DDD
                celular.substring(4, 9), // 5 primeiros dígitos
                celular.substring(9) // 4 últimos dígitos
        );
    }

    /**
     * Remove tudo exceto números de uma string (útil para celular, CPF, etc)
     */
    public static String somenteNumeros(String s) {
        if (s == null)
            return null;
        return s.replaceAll("\\D", "");
    }

    /**
     * Remove acentos de uma string
     */
    public static String removeAcentos(String str) {
        if (str == null)
            return null;

        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    /**
     * Verifica se duas strings são similares (ignorando acentos e case)
     */
    public static boolean containsIgnoreAccents(String text, String search) {
        if (text == null || search == null)
            return false;

        String normalizedText = removeAcentos(text);
        String normalizedSearch = removeAcentos(search);

        return normalizedText.contains(normalizedSearch);
    }
}
