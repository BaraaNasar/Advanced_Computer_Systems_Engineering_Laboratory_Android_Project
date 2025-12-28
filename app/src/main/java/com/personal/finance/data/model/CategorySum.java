package com.personal.finance.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySum {
    private String category;
    private double totalAmount;

    public CategorySum(String category, double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }
}