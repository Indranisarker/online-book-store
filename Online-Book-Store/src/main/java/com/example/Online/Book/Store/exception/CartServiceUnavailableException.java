package com.example.Online.Book.Store.exception;

public class CartServiceUnavailableException extends RuntimeException{
    public CartServiceUnavailableException(String message){
        super(message);
    }
}
