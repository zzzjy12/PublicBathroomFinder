package authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.placesprojectdemo.MainActivity;
import com.example.placesprojectdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Forget extends AppCompatActivity {

    private EditText edEmail, edNewPassword, edConfirmPassword;
    private Button btnResetPassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        edEmail = findViewById(R.id.edEmail);
        edNewPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        firebaseAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String emailAddress = edEmail.getText().toString().trim();
        String newPassword = edNewPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();

        if (emailAddress.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(Forget.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(Forget.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }


        firebaseAuth.confirmPasswordReset(emailAddress, newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Forget.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Forget.this, MainActivity.class));
                            finish();
                        } else {

                            startActivity(new Intent(Forget.this, Registration.class));
                            finish();
                        }
                    }
                });
    }
}