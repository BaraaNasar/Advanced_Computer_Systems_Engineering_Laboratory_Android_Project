package com.personal.finance.data.repository;

import android.app.Application;

import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Category;
import com.personal.finance.data.model.CategorySum;
import com.personal.finance.data.model.Transaction;
import com.personal.finance.data.sqlite.BudgetDb;
import com.personal.finance.data.sqlite.CategoryDb;
import com.personal.finance.data.sqlite.TransactionDb;
import com.personal.finance.data.sqlite.UserDb;
import com.personal.finance.data.model.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinanceRepository {

    private final TransactionDb transactionDb;
    private final BudgetDb budgetDb;
    private final CategoryDb categoryDb;
    private final UserDb userDb;

    // keep background executor (replacement for Room executor)
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public FinanceRepository(Application application) {
        transactionDb = new TransactionDb(application);
        budgetDb = new BudgetDb(application);
        categoryDb = new CategoryDb(application);
        userDb = new UserDb(application);
    }

    // ---------------- Transactions (read) ----------------
    public List<Transaction> getAllTransactions(String email) {
        return transactionDb.getAllTransactions(email);
    }

    public List<Transaction> getIncomes(String email) {
        return transactionDb.getIncomes(email);
    }

    public List<Transaction> getExpenses(String email) {
        return transactionDb.getExpenses(email);
    }

    public double getTotalIncome(String email) {
        return transactionDb.getTotalIncome(email);
    }

    public double getTotalExpense(String email) {
        return transactionDb.getTotalExpense(email);
    }

    public double getTotalIncomeByDate(String email, long startDate, long endDate) {
        return transactionDb.getTotalIncomeByDate(email, startDate, endDate);
    }

    public double getTotalExpenseByDate(String email, long startDate, long endDate) {
        return transactionDb.getTotalExpenseByDate(email, startDate, endDate);
    }

    public List<CategorySum> getCategoryGroupedSums(String email, String type, long startDate, long endDate) {
        return transactionDb.getCategoryGroupedSums(email, type, startDate, endDate);
    }

    // ---------------- Transactions (write async) ----------------
    public void insertTransaction(Transaction transaction) {
        executor.execute(() -> transactionDb.insert(transaction));
    }

    public void deleteTransaction(Transaction transaction) {
        executor.execute(() -> transactionDb.deleteById(transaction.getId()));
    }

    public void updateTransaction(Transaction transaction) {
        executor.execute(() -> transactionDb.update(transaction));
    }

    // get all transactions as List
    public List<Transaction> getAllTransactionsList(String email) {
        return transactionDb.getAllTransactions(email);
    }

    // ---------------- Categories ----------------
    public List<Category> getAllCategories(String email) {
        return categoryDb.getAllCategories(email);
    }

    public List<Category> getCategoriesByType(String email, String type) {
        return categoryDb.getCategoriesByType(email, type);
    }

    public void insertCategory(Category category) {
        executor.execute(() -> categoryDb.insert(category));
    }

    public void deleteCategory(Category category) {
        executor.execute(() -> categoryDb.delete(category));
    }

    public void updateCategory(Category oldCategory, String newName) {
        executor.execute(() -> categoryDb.update(oldCategory, newName));
    }

    // ---------------- Budgets ----------------
    public List<Budget> getAllBudgets(String email) {
        return budgetDb.getAllBudgets(email);
    }

    public void insertBudget(Budget budget) {
        executor.execute(() -> budgetDb.insert(budget));
    }

    public void deleteBudget(Budget budget) {
        executor.execute(() -> budgetDb.deleteById(budget.getId()));
    }

    public void updateBudget(Budget budget) {
        executor.execute(() -> budgetDb.update(budget));
    }

    // ---------------- Pre-populate categories ----------------
    public void prePopulateCategories(String email) {
        executor.execute(() -> {
            if (categoryDb.getCategoryCount(email) == 0) {
                // Income
                categoryDb.insert(new Category("Salary", "INCOME", email));
                categoryDb.insert(new Category("Scholarship", "INCOME", email));
                categoryDb.insert(new Category("Gift", "INCOME", email));
                categoryDb.insert(new Category("Interest", "INCOME", email));

                // Expense
                categoryDb.insert(new Category("Groceries", "EXPENSE", email));
                categoryDb.insert(new Category("Rent", "EXPENSE", email));
                categoryDb.insert(new Category("Food", "EXPENSE", email));
                categoryDb.insert(new Category("Bills", "EXPENSE", email));
                categoryDb.insert(new Category("Entertainment", "EXPENSE", email));
                categoryDb.insert(new Category("Transport", "EXPENSE", email));
                categoryDb.insert(new Category("Health", "EXPENSE", email));
                categoryDb.insert(new Category("Shopping", "EXPENSE", email));
            }
        });
    }

    // ---------------- User ----------------
    public User getUser(String email) {
        return userDb.getUser(email);
    }

    public void updateUser(User user) {
        executor.execute(() -> userDb.updateUser(user));
    }
}