package com.unipi.talescast;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    String username, password;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
    }

    public void signIn(View view) {
        getCreds();
        if (!username.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(username,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showMessage("Success!", "Welcome back!");
                                user = auth.getCurrentUser();
                                goToNext();
                            } else {
                                showMessage("Error", task.getException() != null ? task.getException().getLocalizedMessage() : "Error while signing in. Please check internet connection.");
                            }
                        }
                    });
        } else {
            showMessage("Sign in error!", "Please provide a valid email-password pair.");
        }
    }

    public void signUp(View view) {
        getCreds();
        if (!username.isEmpty() && !password.isEmpty()) {
            auth.createUserWithEmailAndPassword(username,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showMessage("Success!", "Welcome aboard!");
                                user = auth.getCurrentUser();
                                goToNext();
                            } else {
                                showMessage("Error", task.getException() != null ? task.getException().getLocalizedMessage() : "Error while signing up. Please check internet connection.");
                            }
                        }
                    });
        } else {
            showMessage("Sign up error!", "Please provide a valid email-password pair.");
        }
    }

    public void logOut(View view) {
        if (user != null) {
            auth.signOut();
            showMessage("Exit!","User signed out.");
            return;
        }
        showMessage("Info", "User already signed out.");
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    private void getCreds() {
        username = ((EditText)findViewById(R.id.usernameEditText)).getText().toString();
        password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
    }

    private void goToNext() {
        Intent intent = new Intent(this, Stories.class);
        startActivity(intent);
    }
}