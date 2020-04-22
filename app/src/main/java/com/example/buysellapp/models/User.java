package com.example.buysellapp.models;

public class User {
    private String username,email;

    public User(){

    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }


}
