package com.adesso.awesomepizza.order;

public class InvalidOrderTransitionException extends RuntimeException {
    public InvalidOrderTransitionException(String trackingCode, OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Order " + trackingCode + " cannot move from " + currentStatus + " to " + targetStatus + ".");
    }
}
