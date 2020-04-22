package com.example.buysellapp.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comment {
    @ServerTimestamp
    private Date date;
    private String email;
    private String comment;
    private String item_id;
    private String name;

    public Comment() {
    }

    public Comment(String email, String comment, String item_id, String name) {
        this.email = email;
        this.comment = comment;
        this.item_id = item_id;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getComment() {
        return comment;
    }

    public String getItem_id() {
        return item_id;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
