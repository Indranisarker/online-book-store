package com.example.Online.Book.Store.exception;

public class BookReviewNotFoundException extends RuntimeException{
    public BookReviewNotFoundException(String message){
        super(message);
    }
}
