package com.criteo.publisher;

public class BidResponse {

    private double price;
    private BidToken token;
    private boolean valid;

    protected BidResponse(double price, BidToken token, boolean valid) {
        this.price = price;
        this.token = token;
        this.valid = valid;
    }

    protected  BidResponse() {
        this.price = 0;
        this.token = null;
        this.valid = false;
    }

    public double getPrice() {
        return price;
    }

    public BidToken getBidToken() {
        return token;
    }

    public boolean isBidSuccess() {
        return valid;
    }
}
