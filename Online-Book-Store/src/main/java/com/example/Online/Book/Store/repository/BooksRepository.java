package com.example.Online.Book.Store.repository;

import com.example.Online.Book.Store.entity.Book;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BooksRepository extends JpaRepository<Book, Long> {
    Book findBookByNameIgnoreCase(String name);
    @Query("SELECT b FROM Book b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> findBookByNameIgnoreCaseOrCategoryIgnoreCase(@Param("keyword") String searchText);
    // Define a method to find a book by its ID along with its reviews
    @EntityGraph(attributePaths = "reviews")
    Optional<Book> findById(Long id);

}
