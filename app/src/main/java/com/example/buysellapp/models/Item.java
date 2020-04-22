package com.example.buysellapp.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Item {


    @ServerTimestamp
    private Date date;
    private String owner_email,owner_name,title,description,category,item_id;
    private int price;

    public Item(){

    }

    public Item(String owner_email, String owner_name, String title, String description, String category, String item_id, int price) {
        this.owner_email = owner_email;
        this.owner_name = owner_name;
        this.title = title;
        this.description = description;
        this.category = category;
        this.item_id = item_id;
        this.price = price;
    }

    public String getOwner_email() {
        return owner_email;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getItem_id() {
        return item_id;
    }

    public int getPrice() {
        return price;
    }

    @ServerTimestamp
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
