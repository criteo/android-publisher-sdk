package com.criteo.publisher.model;

class BidResponse {

    private double price;
    private BidToken token;
    private boolean valid;

    BidResponse(double price, BidToken token, boolean valid) {
        this.price = price;
        this.token = token;
        this.valid = valid;
    }

    public double getPrice() {
        return price;
    }

    public BidToken getToken() {
        return token;
    }

    public boolean isValid() {
        return valid;
    }
}
