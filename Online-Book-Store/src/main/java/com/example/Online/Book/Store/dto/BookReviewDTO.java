package com.example.Online.Book.Store.dto;

import com.example.Online.Book.Store.entity.Book;
import com.example.Online.Book.Store.entity.BookReview;
import com.example.Online.Book.Store.entity.User;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookReviewDTO {
    private Integer rating;
    private String review;
    @Temporal(TemporalType.DATE)
    private Date createDate;
    private User user;
    private Book book;
}
