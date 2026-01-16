package com.personal.finance.data.model;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
