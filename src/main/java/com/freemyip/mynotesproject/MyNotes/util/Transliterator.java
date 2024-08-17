package com.freemyip.mynotesproject.MyNotes.util;

import org.springframework.stereotype.Component;

@Component
public class Transliterator {
    public String transliterate(String text) {
        String rules =
                "ь > ; ъ > ; " +
                        "ш > sh; щ > shch; ч > ch; " +
                        "ю > yu; я > ya; " +
                        ":: Any-Latin; " +
                        ":: Latin-ASCII;";
        com.ibm.icu.text.Transliterator transliterator = com.ibm.icu.text.Transliterator.createFromRules("Custom-Latin", rules, com.ibm.icu.text.Transliterator.FORWARD);

        return transliterator.transliterate(text).toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
