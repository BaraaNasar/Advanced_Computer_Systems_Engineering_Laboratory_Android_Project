package com.personal.finance.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.personal.finance.data.repository.FinanceRepository;

public class AuthViewModel extends AndroidViewModel {
    private FinanceRepository repository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
    }


    public void initializeUserData(String email) {
        repository.prePopulateCategories(email);
    }
}
