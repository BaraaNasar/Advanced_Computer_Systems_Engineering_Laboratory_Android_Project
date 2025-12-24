package com.personal.finance.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.personal.finance.data.model.User;
import com.personal.finance.data.repository.FinanceRepository;

public class AuthViewModel extends AndroidViewModel {
    private FinanceRepository repository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
    }

    public void register(User user) {
        repository.insertUser(user);
    }

    public LiveData<User> login(String email, String password) {
        return repository.login(email, password);
    }
}
