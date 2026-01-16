package com.personal.finance.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {
    private long id; // AUTOINCREMENT
    private String name;
    private String type;
    private String userEmail;

    public Category(long id, String name, String type, String userEmail) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }

    public Category(String name, String type, String userEmail) {
        this(0, name, type, userEmail);
    }
}