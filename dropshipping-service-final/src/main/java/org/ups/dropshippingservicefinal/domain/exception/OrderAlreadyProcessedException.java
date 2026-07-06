package org.ups.dropshippingservicefinal.domain.exception;

public class OrderAlreadyProcessedException extends RuntimeException {

    public OrderAlreadyProcessedException(String message) {
        super(message);
    }
}
