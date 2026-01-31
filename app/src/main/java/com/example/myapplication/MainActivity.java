package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.translation.Translator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MainActivity extends AppCompatActivity {

    String msg = "Android: ";

    private Spinner spinnerLanguage;
    private String[] languages = {"English", "Vietnamese"}; // Display names
    private String[] languageCodes = {"en", "vi"};      // Language codes

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(msg, "The onCreate() event");

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long id) {

                String selectedLanguageCode = languageCodes[pos];
                setAppLocale(selectedLanguageCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

            private void setAppLocale(String languageCode) {
                // Use the modern AppCompat way to set the locale
                LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
                AppCompatDelegate.setApplicationLocales(appLocale);

                // Recreate the activity for changes to take effect immediately in the UI
                recreate();
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(msg, "The onStart() event");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(msg, "The onResume() event");
    }

    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(msg, "The onPause() event");
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(msg, "The onStop() event");
    }

    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(msg, "The onDestroy() event");
    }

    public void startServ(View view){
        startService(new Intent(getBaseContext(), MyService.class));
    }

    public void stopServ(View view){
        stopService(new Intent(getBaseContext(), MyService.class));
    }

}
