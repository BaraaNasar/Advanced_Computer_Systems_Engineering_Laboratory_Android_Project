package com.personal.finance.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.personal.finance.R;
import com.personal.finance.data.sqlite.UserDb;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmailSignUp, etPasswordSignUp, etConfirmPassword;
    private TextInputLayout tilFirstName, tilLastName, tilEmailSignUp, tilPasswordSignUp, tilConfirmPassword;

    private UserDb userDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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

        userDb = new UserDb(this);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilEmailSignUp = findViewById(R.id.tilEmailSignUp);
        tilPasswordSignUp = findViewById(R.id.tilPasswordSignUp);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> register());

        setupTextWatchers();
    }

    private void register() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmailSignUp.getText().toString().trim();
        String password = etPasswordSignUp.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        clearErrors();

        if (!validateInput(firstName, lastName, email, password, confirmPassword)) {
            Toast.makeText(this, "Registration Failed: Please fix the errors in red", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDb.userExists(email)) {
            tilEmailSignUp.setError("Email already registered");
            return;
        }

        boolean inserted = userDb.insertUser(email, firstName, lastName, password);
        if (inserted) {
            Toast.makeText(this, "Registration Successful. Please sign in.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmailSignUp.setError(null);
        tilPasswordSignUp.setError(null);
        tilConfirmPassword.setError(null);
    }

    private boolean validateInput(String first, String last, String email, String pass, String confirm) {
        boolean isValid = true;

        if (TextUtils.isEmpty(first)) {
            tilFirstName.setError("First name is required");
            isValid = false;
        } else if (first.length() < 3 || first.length() > 10) {
            tilFirstName.setError("Must be 3-10 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(last)) {
            tilLastName.setError("Last name is required");
            isValid = false;
        } else if (last.length() < 3 || last.length() > 10) {
            tilLastName.setError("Required (3-10 characters)");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmailSignUp.setError("Email is required");
            isValid = false;
        } else if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailSignUp.setError("Invalid Email format");
            isValid = false;
        }

        if (TextUtils.isEmpty(pass)) {
            tilPasswordSignUp.setError("Password is required");
            isValid = false;
        } else if (!isValidPassword(pass)) {
            tilPasswordSignUp.setError("6-12 chars, 1 digit, 1 lower, 1 upper");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirm)) {
            tilConfirmPassword.setError("Confirmation required");
            isValid = false;
        } else if (!pass.equals(confirm)) {
            tilConfirmPassword.setError("Password do not match");
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
                    tilFirstName.setError("Must be 3-10 characters");
                } else {
                    tilFirstName.setError(null);
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
                    tilLastName.setError("Must be 3-10 characters");
                } else {
                    tilLastName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etEmailSignUp.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    tilEmailSignUp.setError("Invalid email format");
                } else {
                    tilEmailSignUp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etPasswordSignUp.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pass = s.toString();
                if (pass.length() > 0 && !isValidPassword(pass)) {
                    tilPasswordSignUp.setError("6-12 chars, 1 digit, 1 lower, 1 upper");
                } else {
                    tilPasswordSignUp.setError(null);
                }

                String confirm = etConfirmPassword.getText().toString();
                if (!confirm.isEmpty()) {
                    if (!pass.equals(confirm))
                        tilConfirmPassword.setError("Passwords do not match");
                    else
                        tilConfirmPassword.setError(null);
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
                String pass = etPasswordSignUp.getText().toString();
                if (confirm.length() > 0 && !confirm.equals(pass))
                    tilConfirmPassword.setError("Passwords do not match");
                else
                    tilConfirmPassword.setError(null);
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