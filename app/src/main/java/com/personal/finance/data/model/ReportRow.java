package com.personal.finance.data.model;

public class ReportRow {
    private final String label;       // day/week/month label
    private final double income;
    private final double expense;

    public ReportRow(String label, double income, double expense) {
        this.label = label;
        this.income = income;
        this.expense = expense;
    }

    public String getLabel() { return label; }
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
    public double getBalance() { return income - expense; }
}