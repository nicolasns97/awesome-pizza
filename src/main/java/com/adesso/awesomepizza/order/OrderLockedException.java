package com.adesso.awesomepizza.order;

public class OrderLockedException extends RuntimeException {
    public OrderLockedException(String trackingCode) {
        super("Order " + trackingCode + " is being updated by another request. Please retry.");
    }
}
