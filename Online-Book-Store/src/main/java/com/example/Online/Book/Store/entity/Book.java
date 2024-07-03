package com.example.Online.Book.Store.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "allBooks")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Book name is mandatory")
    private String name;
    @NotBlank(message = "Author name is mandatory")
    private String author;
    @NotBlank(message = "Category is mandatory")
    private String category;
    @Lob
    private byte[] image;
    private String imagePath;
    @Positive(message = "Price must be positive")
    private double price;
    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;
    @Min(value = 1, message = "Pages must be at least 1")
    private int pages;
    @Min(value = 1, message = "Edition must be at least 1")
    private int edition;
    @Min(value = 1, message = "Edition must be at least 1")
    private String publication;
    @Size(min = 5, max = 13, message = "ISBN must be between 5 and 13 characters")
    @Pattern(regexp = "\\d+", message = "ISBN must be numeric")
    private String ISBN;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookReview> reviews;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookReview> rating;
}
