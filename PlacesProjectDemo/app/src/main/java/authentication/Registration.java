package authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.placesprojectdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class Registration extends AppCompatActivity {

    EditText edEmail, edPassword;
    Button btnRegistration, btnAlreadyHaveAnAccount;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnAlreadyHaveAnAccount = findViewById(R.id.btnAlreadyHaveAnAccount);
        btnRegistration = findViewById(R.id.btnRegister);

        btnAlreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = edEmail.getText().toString().trim();
                final String password = edPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Registration.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Registration.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 8) {
                    Toast.makeText(Registration.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }


                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                            if (isNewUser) {

                                registerUser(email, password);
                            } else {

                                Toast.makeText(Registration.this, "Email is already registered. Please login.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Registration.this, Login.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {

                            Toast.makeText(Registration.this, "Failed to check email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Registration.this, "You have successfully signed up! Welcome to FlushFinder!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Registration.this, Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Registration.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
