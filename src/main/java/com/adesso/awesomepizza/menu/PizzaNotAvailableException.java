package com.adesso.awesomepizza.menu;

public class PizzaNotAvailableException extends RuntimeException {
    public PizzaNotAvailableException(Long pizzaId) {
        super("Pizza not available: " + pizzaId);
    }
}
