package com.caphecode.myapplication.model;

import java.util.Objects;

/**
 * Created by Nhatran241 on 12/5/2021.
 * trannhat2411999@gmail.com
 */
public class Transaction {
    private String txn;
    private String method;
    private String age;
    private String from;
    private String to;
    private String tag = "BTC";
    private double quantity;
    private boolean isNoty = false;

    public Transaction(String txn, String method, String age, String from, String to, double quantity) {
        this.txn = txn;
        this.method = method;
        this.age = age;
        this.from = from;
        this.to = to;
        this.quantity = quantity;
    }

    public Transaction(){

    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isNoty() {
        return isNoty;
    }

    public void setNoty(boolean noty) {
        isNoty = noty;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(that.quantity, quantity) == 0 && Objects.equals(txn, that.txn) && Objects.equals(method, that.method) && Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txn, method, from, to, quantity);
    }
}
