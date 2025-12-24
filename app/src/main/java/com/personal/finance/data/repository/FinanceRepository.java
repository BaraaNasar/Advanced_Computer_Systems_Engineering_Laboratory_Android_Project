package com.personal.finance.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.personal.finance.data.AppDatabase;
import com.personal.finance.data.dao.BudgetDao;
import com.personal.finance.data.dao.CategoryDao;
import com.personal.finance.data.dao.TransactionDao;
import com.personal.finance.data.dao.UserDao;
import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Category;
import com.personal.finance.data.model.Transaction;
import com.personal.finance.data.model.User;

import java.util.List;

public class FinanceRepository {
    private UserDao userDao;
    private TransactionDao transactionDao;
    private BudgetDao budgetDao;
    private CategoryDao categoryDao;

    public FinanceRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        categoryDao = db.categoryDao();
    }

    // User Operations
    public void insertUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
    }

    public LiveData<User> login(String email, String password) {
        return userDao.login(email, password);
    }

    public User getUserByEmail(String email) {
        // This should probably be LiveData or done asynchronously in a real app
        // For simplicity in login check we might need it, but Dao returns LiveData
        // usually.
        // We will execute this on background thread if needed or use the LiveData
        // login.
        return userDao.getUserByEmail(email);
    }

    public void updateUser(User user) {
        AppDatabase.databaseWriteExecutor
                .execute(() -> userDao.updateUser(user.email, user.firstName, user.lastName, user.password));
    }

    // Transaction Operations
    public LiveData<List<Transaction>> getAllTransactions(String email) {
        return transactionDao.getAllTransactions(email);
    }

    public LiveData<Double> getTotalIncome(String email) {
        return transactionDao.getTotalIncome(email);
    }

    public LiveData<Double> getTotalExpense(String email) {
        return transactionDao.getTotalExpense(email);
    }

    // Filtered Statistics
    public LiveData<Double> getTotalIncomeByDate(String email, long startDate, long endDate) {
        return transactionDao.getTotalIncomeByDate(email, startDate, endDate);
    }

    public LiveData<Double> getTotalExpenseByDate(String email, long startDate, long endDate) {
        return transactionDao.getTotalExpenseByDate(email, startDate, endDate);
    }

    public LiveData<List<com.personal.finance.data.model.CategorySum>> getCategoryGroupedSums(String email, String type,
            long startDate, long endDate) {
        return transactionDao.getCategoryGroupedSums(email, type, startDate, endDate);
    }

    // Category Operations
    public void insertCategory(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.insert(category));
    }

    public void deleteCategory(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.delete(category));
    }

    public LiveData<List<Category>> getAllCategories(String email) {
        return categoryDao.getAllCategories(email);
    }

    public LiveData<List<Category>> getCategoriesByType(String email, String type) {
        return categoryDao.getCategoriesByType(email, type);
    }

    public void insertTransaction(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.insert(transaction));
    }

    public void deleteTransaction(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.delete(transaction));
    }

    // Budget Operations
    public LiveData<List<Budget>> getAllBudgets(String email) {
        return budgetDao.getAllBudgets(email);
    }

    public void insertBudget(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.insert(budget));
    }

    public void deleteBudget(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.delete(budget));
    }
}
