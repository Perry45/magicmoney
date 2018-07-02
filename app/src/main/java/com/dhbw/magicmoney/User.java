package com.dhbw.magicmoney;

import android.app.Application;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

@DatabaseTable(tableName = "db_user")
public class User extends Application implements Serializable {

    @DatabaseField(columnName = "ID", generatedId = true)
    private int ID;

    @DatabaseField(columnName = "username", canBeNull = false)
    private String username;

    @DatabaseField(columnName = "email", canBeNull = false)
    private String email;

    @DatabaseField(columnName = "password", canBeNull = false)
    private String password;

    @DatabaseField(columnName = "Nachname", canBeNull = false)
    private String name;

    @DatabaseField(columnName = "Vorname", canBeNull = false)
    private String forename;

    @DatabaseField(columnName = "Kontostand", canBeNull = false)
    private double balance;


    public User(){



    }

    public User(String username, String email, String password, String name, String forename, double balance) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.forename = forename;
        this.balance = balance;
    }

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void riseBalance(double amount) {
        this.balance = this.balance+amount;
    }

    @Override
    public String toString() {
        return "User{" +
                "ID=" + ID +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", forename='" + forename + '\'' +
                ", balance=" + balance +
                '}';
    }

    public String getEURBalance(){
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format((this.getBalance()));
    }


}
