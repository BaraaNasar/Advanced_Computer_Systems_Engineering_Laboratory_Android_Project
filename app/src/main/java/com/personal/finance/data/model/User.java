package com.personal.finance.data.model;

import androidx.annotation.NonNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    @NonNull
    private String email;
    private String firstName;
    private String lastName;
    private String password;

    public User(@NonNull String email, String firstName, String lastName, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }
}
