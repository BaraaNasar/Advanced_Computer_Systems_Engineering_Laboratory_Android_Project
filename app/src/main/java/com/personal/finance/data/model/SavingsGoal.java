package com.personal.finance.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavingsGoal {
    private long id;
    private double goalAmount;
    private int month;
    private int year;
    private String userEmail;

    public SavingsGoal(long id, double goalAmount, int month, int year, String userEmail) {
        this.id = id;
        this.goalAmount = goalAmount;
        this.month = month;
        this.year = year;
        this.userEmail = userEmail;
    }

    public SavingsGoal(double goalAmount, int month, int year, String userEmail) {
        this(0, goalAmount, month, year, userEmail);
    }
}
