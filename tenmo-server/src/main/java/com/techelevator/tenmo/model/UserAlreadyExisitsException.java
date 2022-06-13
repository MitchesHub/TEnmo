package com.techelevator.tenmo.model;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "User name already Exists")
public class UserAlreadyExisitsException  extends RuntimeException{
}