package com.gridinsight.backend.RGMM_3.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String from, String to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
