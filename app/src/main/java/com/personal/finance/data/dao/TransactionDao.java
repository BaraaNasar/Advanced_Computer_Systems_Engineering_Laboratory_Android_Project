package com.personal.finance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.personal.finance.data.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions(String email);

    @Query("SELECT * FROM transactions WHERE userEmail = :email AND type = 'INCOME' ORDER BY date DESC")
    LiveData<List<Transaction>> getIncomes(String email);

    @Query("SELECT * FROM transactions WHERE userEmail = :email AND type = 'EXPENSE' ORDER BY date DESC")
    LiveData<List<Transaction>> getExpenses(String email);

    @Query("SELECT SUM(amount) FROM transactions WHERE userEmail = :email AND type = 'INCOME'")
    LiveData<Double> getTotalIncome(String email);

    @Query("SELECT SUM(amount) FROM transactions WHERE userEmail = :email AND type = 'EXPENSE'")
    LiveData<Double> getTotalExpense(String email);

    // Date Range Queries
    @Query("SELECT SUM(amount) FROM transactions WHERE userEmail = :email AND type = 'INCOME' AND date >= :startDate AND date <= :endDate")
    LiveData<Double> getTotalIncomeByDate(String email, long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM transactions WHERE userEmail = :email AND type = 'EXPENSE' AND date >= :startDate AND date <= :endDate")
    LiveData<Double> getTotalExpenseByDate(String email, long startDate, long endDate);

    // Category Distribution Queries
    @Query("SELECT category, SUM(amount) as totalAmount FROM transactions WHERE userEmail = :email AND type = :type AND date >= :startDate AND date <= :endDate GROUP BY category")
    LiveData<List<com.personal.finance.data.model.CategorySum>> getCategoryGroupedSums(String email, String type,
            long startDate, long endDate);
}
