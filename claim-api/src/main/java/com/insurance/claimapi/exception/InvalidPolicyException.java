package com.insurance.claimapi.exception;

public class InvalidPolicyException extends RuntimeException {
    public InvalidPolicyException(String message) {
        super(message);
    }
}
