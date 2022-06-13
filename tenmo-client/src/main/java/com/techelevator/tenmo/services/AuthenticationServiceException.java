package com.techelevator.tenmo.services;

public class AuthenticationServiceException extends Exception {
    private static final long serialVersionUID = 1L; //serialversionuid = The serialVersionUID attribute
                                                    // is an identifier that is used to serialize/deserialize
                                                    //an object of a Serializable class.

    public AuthenticationServiceException(String message) {
        super(message);
    }
}
