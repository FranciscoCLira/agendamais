package com.agendademais.utils;

import java.text.Normalizer;

public class StringUtils {

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
