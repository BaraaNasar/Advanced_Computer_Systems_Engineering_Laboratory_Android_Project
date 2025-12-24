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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ضبط الثيم حسب القيمة المخزنة
        com.personal.finance.utils.SessionManager sessionManager = new com.personal.finance.utils.SessionManager(this);
        if ("DARK".equals(sessionManager.getTheme())) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmailSignUp);
        etPassword = findViewById(R.id.etPasswordSignUp);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> register());
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
            etFirstName.setError("3-10 characters required");
            isValid = false;
        }

        if (last.length() < 3 || last.length() > 10) {
            etLastName.setError("3-10 characters required");
            isValid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid Email");
            isValid = false;
        }

        if (pass.length() < 6 || pass.length() > 12 || !isValidPassword(pass)) {
            etPassword.setError("6-12 chars, must contain number, lower & uppercase");
            isValid = false;
        }

        if (!pass.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$");
        return pattern.matcher(password).matches();
    }
}
