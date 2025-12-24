package com.personal.finance.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.personal.finance.data.dao.BudgetDao;
import com.personal.finance.data.dao.TransactionDao;
import com.personal.finance.data.dao.UserDao;
import com.personal.finance.data.model.Budget;
import com.personal.finance.data.model.Transaction;
import com.personal.finance.data.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { User.class, Transaction.class, Budget.class,
        com.personal.finance.data.model.Category.class }, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();

    public abstract com.personal.finance.data.dao.CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "finance_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(
                                        @androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Could pre-populate here but we need an email.
                                    // Better to do it on user sign-up or first launch logic.
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
