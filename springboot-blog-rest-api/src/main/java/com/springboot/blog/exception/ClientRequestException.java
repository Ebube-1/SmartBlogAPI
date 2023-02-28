package com.springboot.blog.exception;

public class ClientRequestException extends RuntimeException{
    public ClientRequestException(String message) {
        super(message);
    }
}
