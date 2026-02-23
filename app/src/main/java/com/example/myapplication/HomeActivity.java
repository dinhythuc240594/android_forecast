package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    private TextView txtWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtWelcome = findViewById(R.id.txtWelcome);

        Intent intent = getIntent();

        if (intent != null) {
            String receivedName = intent.getStringExtra("email");
            txtWelcome.setText("Welcome, " + receivedName);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, new InputFragment())
                .replace(R.id.fragmentContainerView2, new DisplayFragment())
                .commit();

    }
}