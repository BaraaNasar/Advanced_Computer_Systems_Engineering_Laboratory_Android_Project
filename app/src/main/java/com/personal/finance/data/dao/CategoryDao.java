package com.personal.finance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.personal.finance.data.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories WHERE userEmail = :email")
    LiveData<List<Category>> getAllCategories(String email);

    @Query("SELECT * FROM categories WHERE userEmail = :email AND type = :type")
    LiveData<List<Category>> getCategoriesByType(String email, String type);

    // For sync checks if needed
    @Query("SELECT COUNT(*) FROM categories WHERE userEmail = :email")
    int getCategoryCount(String email);
}
