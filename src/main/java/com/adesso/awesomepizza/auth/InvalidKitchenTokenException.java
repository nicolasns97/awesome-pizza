package com.adesso.awesomepizza.auth;

public class InvalidKitchenTokenException extends RuntimeException {
    public InvalidKitchenTokenException() {
        super("Access denied: invalid or missing kitchen token.");
    }
}
