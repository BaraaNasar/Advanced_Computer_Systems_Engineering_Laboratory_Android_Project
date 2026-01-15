package com.personal.finance.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Category;
import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.model.Transaction;
import com.personal.finance.data.repository.FinanceRepository;

import com.personal.finance.data.model.User;
import java.util.List;

public class FinanceViewModel extends AndroidViewModel {

    private final FinanceRepository repository;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        repository = new FinanceRepository(application);
    }

    // -------- Transactions (read) --------
    public List<Transaction> getTransactions(String email) {
        return repository.getAllTransactions(email);
    }

    public List<Transaction> getIncomes(String email) {
        return repository.getIncomes(email);
    }

    public List<Transaction> getExpenses(String email) {
        return repository.getExpenses(email);
    }

    public double getTotalIncome(String email) {
        return repository.getTotalIncome(email);
    }

    public double getTotalExpense(String email) {
        return repository.getTotalExpense(email);
    }

    public double getTotalIncomeByDate(String email, long startDate, long endDate) {
        return repository.getTotalIncomeByDate(email, startDate, endDate);
    }

    public double getTotalExpenseByDate(String email, long startDate, long endDate) {
        return repository.getTotalExpenseByDate(email, startDate, endDate);
    }

    public List<CategorySum> getCategoryGroupedSums(String email, String type, long startDate, long endDate) {
        return repository.getCategoryGroupedSums(email, type, startDate, endDate);
    }

    // -------- Transactions (write async in repository) --------
    public void addTransaction(Transaction transaction) {
        repository.insertTransaction(transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);
    }

    public List<Transaction> getTransactionsList(String email) {
        return repository.getAllTransactionsList(email);
    }

    // -------- Categories --------
    public void insertCategory(Category category) {
        repository.insertCategory(category);
    }

    public void deleteCategory(Category category) {
        repository.deleteCategory(category);
    }

    public void updateCategory(Category oldCategory, String newName) {
        repository.updateCategory(oldCategory, newName);
    }

    public List<Category> getAllCategories(String email) {
        return repository.getAllCategories(email);
    }

    public List<Category> getCategoriesByType(String email, String type) {
        return repository.getCategoriesByType(email, type);
    }

    public void initializeUserData(String email) {
        repository.prePopulateCategories(email);
    }

    // -------- Budgets --------
    public List<Budget> getBudgets(String email) {
        return repository.getAllBudgets(email);
    }

    public void addBudget(Budget budget) {
        repository.insertBudget(budget);
    }

    public void deleteBudget(Budget budget) {
        repository.deleteBudget(budget);
    }

    public void updateBudget(Budget budget) {
        repository.updateBudget(budget);
    }

    public double getSpentAmountForCategory(String email, String category) {
        List<Transaction> allTransactions = repository.getAllTransactions(email);
        double totalSpent = 0.0;
        for (Transaction transaction : allTransactions) {
            if ("EXPENSE".equalsIgnoreCase(transaction.getType()) &&
                    category.equalsIgnoreCase(transaction.getCategory())) {
                totalSpent += transaction.getAmount();
            }
        }
        return totalSpent;
    }

    public double getTotalExpenseForCategoryByDate(String email, String category, long startDate, long endDate) {
        return repository.getTotalExpenseForCategoryByDate(email, category, startDate, endDate);
    }

    public void updateBudgetAlerts(long budgetId, int alert50Sent, int alert100Sent) {
        repository.updateBudgetAlerts(budgetId, alert50Sent, alert100Sent);
    }

    // -------- User --------
    public User getUser(String email) {
        return repository.getUser(email);
    }

    public void updateUser(User user) {
        repository.updateUser(user);
    }
}