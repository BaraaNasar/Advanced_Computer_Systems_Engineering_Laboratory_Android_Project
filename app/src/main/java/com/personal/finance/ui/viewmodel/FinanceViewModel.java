package com.personal.finance.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Category;
import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.model.Transaction;
import com.personal.finance.data.repository.FinanceRepository;
import com.personal.finance.data.model.ReportRow;

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

    public double[] getMonthlySumsByType(String email, String type, long startDate, long endDate) {
        return repository.getMonthlySumsByType(email, type, startDate, endDate);
    }

    public double[] getDailySumsByType(String email, String type, long startDate, long endDate, int days) {
        return repository.getDailySumsByType(email, type, startDate, endDate, days);
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
        // Fallback or gets for current month if not specified
        return repository.getAllBudgets(email);
    }

    public List<Budget> getBudgets(String email, int month, int year) {
        return repository.getBudgetsForMonth(email, month, year);
    }

    public List<ReportRow> getDailyReport(String email, long startDate, long endDate) {
        return repository.getDailyReport(email, startDate, endDate);
    }

    public List<ReportRow> getWeeklyReport(String email, long startDate, long endDate) {
        return repository.getWeeklyReport(email, startDate, endDate);
    }

    public List<ReportRow> getMonthlyReport(String email, long startDate, long endDate) {
        return repository.getMonthlyReport(email, startDate, endDate);
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

    public double getSpentAmountForCategory(String email, String category, int month, int year) {
        List<Transaction> allTransactions = repository.getAllTransactions(email);
        double totalSpent = 0.0;
        java.util.Calendar cal = java.util.Calendar.getInstance();

        for (Transaction transaction : allTransactions) {
            cal.setTimeInMillis(transaction.getDate());
            int tMonth = cal.get(java.util.Calendar.MONTH);
            int tYear = cal.get(java.util.Calendar.YEAR);

            if ("EXPENSE".equalsIgnoreCase(transaction.getType()) &&
                    category.equalsIgnoreCase(transaction.getCategory()) &&
                    tMonth == month && tYear == year) {
                totalSpent += transaction.getAmount();
            }
        }
        return totalSpent;
    }

    public String getBudgetAlertMessage(String email, String category, int month, int year) {
        List<Budget> budgets = getBudgets(email, month, year);
        for (Budget b : budgets) {
            if (b.getCategory().equalsIgnoreCase(category)) {
                double spent = getSpentAmountForCategory(email, category, month, year);
                double limit = b.getLimitAmount();
                int percentage = (int) ((spent / limit) * 100);
                if (percentage >= 100) {
                    return "Alert: " + category + " budget exceeded!";
                } else if (percentage >= 50) {
                    return "Alert: " + category + " at " + percentage + "%";
                }
            }
        }
        return null;
    }

    public java.util.List<String> getAllBudgetAlerts(String email, int month, int year) {
        java.util.List<String> alerts = new java.util.ArrayList<>();
        List<Budget> budgets = getBudgets(email, month, year);
        for (Budget b : budgets) {
            double spent = getSpentAmountForCategory(email, b.getCategory(), month, year);
            double limit = b.getLimitAmount();
            int percentage = (int) ((spent / limit) * 100);
            if (percentage >= 100) {
                alerts.add(b.getCategory() + " exceeded");
            } else if (percentage >= 50) {
                alerts.add(b.getCategory() + " at " + percentage + "%");
            }
        }
        return alerts;
    }

    // -------- Savings Goals --------
    public void setSavingsGoal(com.personal.finance.data.model.SavingsGoal goal) {
        repository.setSavingsGoal(goal);
    }

    public com.personal.finance.data.model.SavingsGoal getSavingsGoal(String email, int month, int year) {
        return repository.getSavingsGoal(email, month, year);
    }

    // -------- User --------
    public User getUser(String email) {
        return repository.getUser(email);
    }

    public void updateUser(User user) {
        repository.updateUser(user);
    }
}