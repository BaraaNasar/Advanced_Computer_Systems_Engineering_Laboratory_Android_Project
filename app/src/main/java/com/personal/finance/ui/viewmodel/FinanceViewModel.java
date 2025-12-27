package com.personal.finance.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Transaction;
import com.personal.finance.data.model.User;
import com.personal.finance.data.repository.FinanceRepository;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {
    private FinanceRepository repository;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
    }

    public LiveData<List<Transaction>> getTransactions(String email) {
        return repository.getAllTransactions(email);
    }

    public LiveData<Double> getTotalIncome(String email) {
        return repository.getTotalIncome(email);
    }

    public LiveData<Double> getTotalExpense(String email) {
        return repository.getTotalExpense(email);
    }

    public LiveData<Double> getTotalIncomeByDate(String email, long startDate, long endDate) {
        return repository.getTotalIncomeByDate(email, startDate, endDate);
    }

    public LiveData<Double> getTotalExpenseByDate(String email, long startDate, long endDate) {
        return repository.getTotalExpenseByDate(email, startDate, endDate);
    }

    public LiveData<List<com.personal.finance.data.model.CategorySum>> getCategoryGroupedSums(String email, String type,
            long startDate, long endDate) {
        return repository.getCategoryGroupedSums(email, type, startDate, endDate);
    }

    // Category Operations
    public void insertCategory(com.personal.finance.data.model.Category category) {
        repository.insertCategory(category);
    }

    public void deleteCategory(com.personal.finance.data.model.Category category) {
        repository.deleteCategory(category);
    }

    public LiveData<List<com.personal.finance.data.model.Category>> getCategoriesByType(String email, String type) {
        return repository.getCategoriesByType(email, type);
    }

    public void addTransaction(Transaction transaction) {
        repository.insertTransaction(transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);
    }

    public void initializeUserData(String email) {
        repository.prePopulateCategories(email);
    }

    public LiveData<List<Budget>> getBudgets(String email) {
        return repository.getAllBudgets(email);
    }

    public void addBudget(Budget budget) {
        repository.insertBudget(budget);
    }

    public void deleteBudget(Budget budget) {
        repository.deleteBudget(budget);
    }

    public void updateUser(User user) {
        repository.updateUser(user);
    }
}
