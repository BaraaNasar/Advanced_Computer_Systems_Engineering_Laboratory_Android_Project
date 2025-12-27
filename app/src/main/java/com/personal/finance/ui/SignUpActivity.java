package com.personal.finance.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.personal.finance.R;
import com.personal.finance.data.model.User;
import com.personal.finance.ui.viewmodel.AuthViewModel;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private com.google.android.material.textfield.TextInputLayout tilFirst, tilLast, tilEmail, tilPass, tilConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ضبط الثيم حسب القيمة المخزنة
        com.personal.finance.utils.SessionManager sessionManager = new com.personal.finance.utils.SessionManager(this);
        if ("DARK".equals(sessionManager.getTheme())) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmailSignUp);
        etPassword = findViewById(R.id.etPasswordSignUp);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilFirst = findViewById(R.id.tilFirstName);
        tilLast = findViewById(R.id.tilLastName);
        tilEmail = findViewById(R.id.tilEmailSignUp);
        tilPass = findViewById(R.id.tilPasswordSignUp);
        tilConfirm = findViewById(R.id.tilConfirmPassword);

        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> register());
        setupTextWatchers();
    }

    private void register() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateInput(firstName, lastName, email, password, confirmPassword)) {
            User user = new User(email, firstName, lastName, password);
            authViewModel.register(user);
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean validateInput(String first, String last, String email, String pass, String confirm) {
        boolean isValid = true;

        if (first.length() < 3 || first.length() > 10) {
            tilFirst.setError("Required (3-10 characters)");
            isValid = false;
        }

        if (last.length() < 3 || last.length() > 10) {
            tilLast.setError("Required (3-10 characters)");
            isValid = false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid Email format");
            isValid = false;
        }

        if (!isValidPassword(pass)) {
            tilPass.setError("6-12 chars, 1 digit, 1 lower, 1 upper");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirm)) {
            tilConfirm.setError("Confirmation required");
            isValid = false;
        } else if (!pass.equals(confirm)) {
            tilConfirm.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void setupTextWatchers() {
        etFirstName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && (s.length() < 3 || s.length() > 10)) {
                    tilFirst.setError("Must be 3-10 characters");
                } else {
                    tilFirst.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etLastName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && (s.length() < 3 || s.length() > 10)) {
                    tilLast.setError("Must be 3-10 characters");
                } else {
                    tilLast.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    tilEmail.setError("Invalid email format");
                } else {
                    tilEmail.setError(null);
                }
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
                String pass = s.toString();
                if (pass.length() > 0 && !isValidPassword(pass)) {
                    tilPass.setError("6-12 chars, 1 digit, 1 lower, 1 upper");
                } else {
                    tilPass.setError(null);
                }
                // Also check confirm password if it's already filled
                String confirm = etConfirmPassword.getText().toString();
                if (!confirm.isEmpty()) {
                    if (!pass.equals(confirm)) {
                        tilConfirm.setError("Passwords do not match");
                    } else {
                        tilConfirm.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String confirm = s.toString();
                String pass = etPassword.getText().toString();
                if (confirm.length() > 0 && !confirm.equals(pass)) {
                    tilConfirm.setError("Passwords do not match");
                } else {
                    tilConfirm.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$");
        return pattern.matcher(password).matches();
    }
}
