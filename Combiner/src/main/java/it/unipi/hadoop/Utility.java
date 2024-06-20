package it.unipi.hadoop;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Utility {

    public static String cleanText(String input) {
        if (input == null) {
            return null;
        }

        // Normalizzo il testo con scomposizione dei caratteri accentati in carattere base + diacritico
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // rimuovo i diacritici
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        //Rimozione di caratteri speciali e trasformazione di tutto il testo in minuscolo
        String clean = withoutDiacritics.replaceAll("[^a-zA-Z]", "").toLowerCase();

        return clean;
    }

}
