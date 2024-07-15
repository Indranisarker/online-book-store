package com.example.Online.Book.Store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleBookNotFoundException(BookNotFoundException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "user/error";
    }

    @ExceptionHandler(BookReviewNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleBookReviewNotFoundException(BookReviewNotFoundException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "user/error";
    }

    @ExceptionHandler(OutOfStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleOutOfStockException(OutOfStockException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "user/error";
    }
    @ExceptionHandler(CartServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleCartServiceUnavailableException(CartServiceUnavailableException ex, Model model) {
        model.addAttribute("errorMessage", "The cart service is currently unavailable. Please try again later!");
        return "user/error";
    }
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientStockException(InsufficientStockException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "user/error";
    }
}
