package com.personal.finance.data.model;

public class Category {
    private long id;          // AUTOINCREMENT
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

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}