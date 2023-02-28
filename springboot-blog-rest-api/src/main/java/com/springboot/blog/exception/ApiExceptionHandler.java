package com.springboot.blog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e, WebRequest request){
        return handleException(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ClientRequestException.class})
    public ResponseEntity<Object> handleClientRequestException(ClientRequestException e, WebRequest request){
        return handleException(e, request, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {ServerException.class})
    public ResponseEntity<Object> handleServerException(ServerException e, WebRequest request){
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = "Method not supported";
        return handleException(new Exception(message, e), request, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = "invalid arguments";
        if(error != null)
            message = String.format("Field: %s. Message: %s", error.getField(), error.getDefaultMessage());

        return handleException(new Exception(message, e), request, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> handleException(Exception e, WebRequest request, HttpStatus httpStatus){
        log.error("API error: ", e);

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                new Date(),
                e.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }
}
