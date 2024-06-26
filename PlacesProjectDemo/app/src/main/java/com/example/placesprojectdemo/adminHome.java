package com.example.placesprojectdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FirebaseAuth;

import authentication.Registration;

public class adminHome extends AppCompatActivity {

    EditText edEmail, edPassword;

    Button btnLogin, btnNoAccount;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnNoAccount = findViewById(R.id.btnNoAccount);
        btnLogin = findViewById(R.id.btnLogin);

        btnNoAccount.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(adminHome.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email, password;
                email = String.valueOf(edEmail.getText());
                password = String.valueOf(edPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(adminHome.this, "Please enter you email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(adminHome.this, "Please enter you password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 8) {
                    Toast.makeText(adminHome.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    public void onComplete(@NonNull Task<AuthResult> task){

                        if (task.isSuccessful()){
                            Toast.makeText(adminHome.this, "Welcome back to admin FlushFinder!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(adminHome.this, adminView.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Toast.makeText(adminHome.this, "Incorrect email or password!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

    }

}
