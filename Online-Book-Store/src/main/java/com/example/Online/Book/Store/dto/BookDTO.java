package com.example.Online.Book.Store.dto;


import com.example.Online.Book.Store.entity.Book;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {
    private Long id;

    @NotBlank(message = "Book name is mandatory")
    private String name;
    @NotBlank(message = "Author name is mandatory")
    private String author;
    @NotBlank(message = "Category is mandatory")
    private String category;
    private MultipartFile image;
    private String imageBase64;
    @Positive(message = "Price must be positive")
    private double price;
    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;
    @Min(value = 1, message = "Pages must be at least 1")
    private int pages;
    @Min(value = 1, message = "Edition must be at least 1")
    private int edition;
    @NotBlank(message = "Publication is mandatory")
    private String publication;
    @Size(min = 5, max = 13, message = "ISBN must be between 5 and 13 characters")
    @Pattern(regexp = "\\d+", message = "ISBN must be numeric")
    private String ISBN;
    public static BookDTO bookEntityToDTO(Book book){
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(book.getId());
        bookDTO.setName(book.getName());
        bookDTO.setAuthor(book.getAuthor());
        bookDTO.setCategory(book.getCategory());
        bookDTO.setImageBase64(Base64.getEncoder().encodeToString(book.getImage()));
        bookDTO.setPrice(book.getPrice());
        bookDTO.setQuantity(book.getQuantity());
        bookDTO.setPages(book.getPages());
        bookDTO.setEdition(book.getEdition());
        bookDTO.setPublication(book.getPublication());
        bookDTO.setISBN(book.getISBN());
        return bookDTO;
    }
}
