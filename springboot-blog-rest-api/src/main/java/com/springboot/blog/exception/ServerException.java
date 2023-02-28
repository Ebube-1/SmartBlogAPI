package com.springboot.blog.exception;

public class ServerException extends RuntimeException{
    public ServerException(String message) {
        super(message);
    }
}
