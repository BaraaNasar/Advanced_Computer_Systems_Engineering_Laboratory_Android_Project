package com.personal.finance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.personal.finance.R;
import com.personal.finance.ui.viewmodel.AuthViewModel;
import com.personal.finance.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        // ضبط الثيم حسب القيمة المخزنة
        if ("DARK".equals(sessionManager.getTheme())) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        Button btnLogin = findViewById(R.id.btnLogin);
        View btnSignUp = findViewById(R.id.btnGoToSignUp);

        btnLogin.setOnClickListener(v -> login());
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.login(email, password).observe(this, user -> {
            if (user != null) {
                if (cbRememberMe.isChecked()) {
                    sessionManager.setLogin(true, user.email);
                } else {
                    sessionManager.setLogin(false, user.email);
                    // If not remembered, we just session login for this run (handled by session
                    // manager logic slightly differently usually, but simplistic here)
                    // Actually requirement: "next time the user does not need to re-type".
                    // If check box is checked, we set persistent login.
                    // If not checked, we should probably still pass the user email to next
                    // activity.
                    // For this app, we will rely on SessionManager storing the email mostly.
                    sessionManager.setLogin(false, user.email);
                }

                // IMPORTANT: Actually setLogin(true) means persistent. setLogin(false) means
                // maybe temporary.
                // However, let's strictly follow requirement: "save the email in shared
                // preferences".
                // We will save login state if checked.
                if (cbRememberMe.isChecked()) {
                    sessionManager.setLogin(true, user.email);
                } else {
                    // Just store basic session info if needed, but for now we just proceed
                    // We might need to handle "auto login" only if check box was checked.
                }

                startMainActivity();
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
