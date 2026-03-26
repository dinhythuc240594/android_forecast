package com.example.myapplication;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public final class LanguageHelper {

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_VIETNAMESE = "vi";

    private LanguageHelper() {
    }

    public static void applySavedLanguage(Context context) {
        String savedLanguage = WeatherPreferences.getLanguage(context);
        if (TextUtils.isEmpty(savedLanguage)) {
            return;
        }

        String normalizedLanguage = normalizeLanguage(savedLanguage);
        // Compare base language codes — toLanguageTags() is often "vi-VN" / "en-US" while prefs store "vi" / "en".
        // Mismatch caused setApplicationLocales() on every Activity start → repeated recreate and ANRs.
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        String currentLanguage = "";
        if (!appLocales.isEmpty() && appLocales.get(0) != null) {
            currentLanguage = normalizeLanguage(appLocales.get(0).getLanguage());
        }
        if (!normalizedLanguage.equals(currentLanguage)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizedLanguage));
        }
    }

    public static void applyLanguage(String languageTag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizeLanguage(languageTag)));
    }

    public static String getCurrentLanguage(Context context) {
        String savedLanguage = WeatherPreferences.getLanguage(context);
        if (!TextUtils.isEmpty(savedLanguage)) {
            return normalizeLanguage(savedLanguage);
        }

        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        if (!appLocales.isEmpty() && appLocales.get(0) != null) {
            return normalizeLanguage(appLocales.get(0).getLanguage());
        }

        Locale currentLocale = context.getResources().getConfiguration().getLocales().get(0);
        return normalizeLanguage(currentLocale.getLanguage());
    }

    private static String normalizeLanguage(String languageTag) {
        return LANGUAGE_VIETNAMESE.equals(languageTag) ? LANGUAGE_VIETNAMESE : LANGUAGE_ENGLISH;
    }
}
