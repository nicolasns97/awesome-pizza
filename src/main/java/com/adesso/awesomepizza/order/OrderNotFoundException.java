package com.adesso.awesomepizza.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String trackingCode) {
        super("Order not found: " + trackingCode);
    }
}
