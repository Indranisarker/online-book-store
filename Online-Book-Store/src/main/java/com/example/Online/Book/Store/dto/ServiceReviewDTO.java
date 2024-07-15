package com.example.Online.Book.Store.dto;

import com.example.Online.Book.Store.entity.ServiceReview;
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
public class ServiceReviewDTO {
    private Integer ratings;
    private String comments;
    private User user;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
}
