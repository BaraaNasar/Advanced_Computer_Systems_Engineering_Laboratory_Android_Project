package com.personal.finance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.personal.finance.R;
import com.personal.finance.data.sqlite.UserDb;
import com.personal.finance.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private UserDb userDb;

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);

        if ("DARK".equals(sessionManager.getTheme())) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // already logged in? go main
        if (sessionManager.isLoggedIn()) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        userDb = new UserDb(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        Button btnLogin = findViewById(R.id.btnLogin);

        // New alternative sign up button to match the premium design
        findViewById(R.id.btnSignUpAlternative)
                .setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));

        btnLogin.setOnClickListener(v -> login());

        setupTextWatchers();

        // Prefill email (remember me)
        String remembered = sessionManager.getRememberedEmail();
        if (remembered != null && !remembered.trim().isEmpty()) {
            etEmail.setText(remembered);
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
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            hasError = true;
        } else {
            tilPassword.setError(null);
        }

        if (hasError)
            return;

        // SQLite check
        boolean ok = userDb.validateLogin(email, password);

        if (ok) {
            // Remember me: email only
            if (cbRememberMe.isChecked()) {
                sessionManager.setRememberedEmail(email);
            } else {
                sessionManager.setRememberedEmail(null);
            }

            // Session
            sessionManager.setLogin(true, email);

            startMainActivity();
        } else {
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
        }
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

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}