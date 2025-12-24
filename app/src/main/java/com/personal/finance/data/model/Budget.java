package com.personal.finance.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets", foreignKeys = @ForeignKey(entity = User.class, parentColumns = "email", childColumns = "userEmail", onDelete = ForeignKey.CASCADE), indices = {
        @Index("userEmail") })
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String category;
    public double limitAmount;
    public String userEmail;

    public Budget(String category, double limitAmount, String userEmail) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.userEmail = userEmail;
    }
}
