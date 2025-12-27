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
    private com.google.android.material.textfield.TextInputLayout tilEmail, tilPassword;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        // ضبط الثيم حسب القيمة المخزنة
        if ("DARK".equals(sessionManager.getTheme())) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
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
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        Button btnLogin = findViewById(R.id.btnLogin);
        View btnSignUp = findViewById(R.id.btnGoToSignUp);

        btnLogin.setOnClickListener(v -> login());
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));

        setupTextWatchers();

        // Pre-fill email and remember me checkbox
        if (sessionManager.isRememberMeEnabled()) {
            etEmail.setText(sessionManager.getRememberedEmail());
            cbRememberMe.setChecked(true);
        }
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean hasError = false;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            hasError = true;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            hasError = true;
        }

        if (hasError)
            return;

        authViewModel.login(email, password).observe(this, user -> {
            if (user != null) {
                // Handle "Remember Me" logic according to requirements
                sessionManager.setRememberMe(cbRememberMe.isChecked(), email);

                // Set session login
                sessionManager.setLogin(true, user.email);

                // Initialize default categories if needed
                authViewModel.initializeUserData(user.email);

                startMainActivity();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTextWatchers() {
        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
