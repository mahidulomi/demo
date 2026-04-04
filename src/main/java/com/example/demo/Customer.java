package com.example.demo;

import java.io.Serializable;
import java.util.UUID;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String type; // "Retail" or "Wholesale"
    private double dueBalance;
    private long createdAt; // Timestamp for sorting

    public Customer() {
        this.id = UUID.randomUUID().toString();
        this.dueBalance = 0.0;
        this.type = "Retail";
        this.createdAt = System.currentTimeMillis();
    }

    public Customer(String name, String phone, String email, String address, String type, double dueBalance) {
        this();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.type = type;
        this.dueBalance = dueBalance;
    }

    public Customer(String id, String name, String phone, String email, String address, String type, double dueBalance) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.type = type;
        this.dueBalance = dueBalance;
        this.createdAt = System.currentTimeMillis();
    }

    public Customer(String id, String name, String phone, String email, String address, String type, double dueBalance, long createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.type = type;
        this.dueBalance = dueBalance;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDueBalance() { return dueBalance; }
    public void setDueBalance(double dueBalance) { this.dueBalance = dueBalance; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}

