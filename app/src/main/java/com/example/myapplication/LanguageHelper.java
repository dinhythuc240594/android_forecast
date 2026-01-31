package com.example.myapplication;

import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageHelper {
    public static void changeLocale(Resources res, String locale) {

        Configuration config;
        config = new Configuration(res.getConfiguration());

        switch (locale){
            case "en":
                config.locale = new Locale("en");
                break;
            case "vi":
                config.locale = new Locale("vi");
                break;
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
