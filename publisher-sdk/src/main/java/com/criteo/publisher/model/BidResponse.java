package com.criteo.publisher.model;

public class BidResponse {

    private double price;
    private String token;
    private boolean success;

    public BidResponse(double price, String token, boolean success) {
        this.price = price;
        this.token = token;
        this.success = success;
    }

    public double getPrice() {
        return price;
    }

    public String getToken() {
        return token;
    }

    public boolean isSuccess() {
        return success;
    }
}
