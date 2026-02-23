package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPwd;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        edtEmail = findViewById(R.id.editEmail);
        edtPwd = findViewById(R.id.editPassword);

        btnLogin = findViewById(R.id.btnSignin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String pwd = edtPwd.getText().toString().trim();

                if(email.isEmpty() || pwd.isEmpty()){
                    edtEmail.setError("Vui lòng nhập email và password");
                    return;
                }

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

                intent.putExtra("email", email);
                intent.putExtra("pwd", pwd);

                startActivity(intent);
            }
        });
    }

}
