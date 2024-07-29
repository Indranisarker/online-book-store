package com.example.Online.Book.Store.controller;

import com.example.Online.Book.Store.dto.BookDTO;
import com.example.Online.Book.Store.dto.ServiceReviewDTO;
import com.example.Online.Book.Store.entity.Book;
import com.example.Online.Book.Store.entity.CartItem;
import com.example.Online.Book.Store.entity.OrderDetails;
import com.example.Online.Book.Store.entity.OrderItem;
import com.example.Online.Book.Store.repository.CartRepository;
import com.example.Online.Book.Store.service.AdminService;
import com.example.Online.Book.Store.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @GetMapping("/customer-review")
    public String review(Model model){
        model.addAttribute("reviews", adminService.getAllReviews());
        return "admin/view-customer-review";
    }

    @GetMapping("/order-details")
    public String ShowAllOrders(Model model){
        List<OrderDetails> orderList = adminService.getAllOrderDetails();
        Map<Long, Double> orderTotalAmounts = new HashMap<>();
        for (OrderDetails orderDetails : orderList) {
            double totalAmount = 0;
            for (OrderItem orderItem : orderDetails.getOrderItems()) {
                totalAmount += orderItem.getBook().getPrice() * orderItem.getQuantity();
            }
            orderTotalAmounts.put(orderDetails.getOrder_id(), totalAmount + 48);
        }

        model.addAttribute("orderList", orderList);
        model.addAttribute("orderTotalAmounts", orderTotalAmounts);
        return "admin/order-details";
    }


    @GetMapping("/books")
    public String getAllBooks(Model model, Principal principal) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("admin", userDetails);
        return getBooks(1, "name", "asc", model);
    }

    @GetMapping("/books/page/{pageNo}")
    public String getBooks(@PathVariable("pageNo") int pageNo,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDirection, Model model) {
        int pageSize = 6;
        int startSerial = calculateStartSerialForPage(pageNo);
        model.addAttribute("startSerial", startSerial);
        Page<Book> bookPage = adminService.getBooks(pageNo, pageSize, sortBy, sortDirection);
        List<Book> bookList = bookPage.getContent();
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", bookPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("books", bookList);

        return "admin/adminHome";
    }

    private int calculateStartSerialForPage(int pageNo) {
        int pageSize = 6;
        return (pageNo - 1) * pageSize;
    }

    @GetMapping("/book/add-form")
    public String viewForm(Model model) {
        model.addAttribute("book", new BookDTO());
        return "admin/add-form";
    }

    @PostMapping("/book/create")
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO, BindingResult bindingResult, Model model,
                          RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()){
            System.out.println("Validation errors occurred!");
            return "admin/add-form";
        }
        model.addAttribute("book", bookDTO);
        adminService.createBook(bookDTO);
        redirectAttributes.addFlashAttribute("message", "New Book Added Successfully!");
        return "redirect:/admin/books";
    }

    @GetMapping("/book-update-id/{id}")
    public String viewUpdateBook(@PathVariable("id") Long id, Model model) {
        Book existingBook = adminService.updateBook(id);
        model.addAttribute("book", existingBook);
        if (existingBook.getImagePath() != null) {
            model.addAttribute("existingImageUrl", Paths.get(existingBook.getImagePath()).getFileName().toString());
        } else {
            model.addAttribute("existingImageUrl", null);
        }
        return "admin/update-form";
    }

    @PostMapping("/book/updateBook/{id}")
    public String updateBook(@PathVariable("id") Long id, @ModelAttribute("book") BookDTO bookDTO,
                             Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("book", adminService.updateBookById(id, bookDTO));
        redirectAttributes.addFlashAttribute("message", "Book Update Successfully!");
        return "redirect:/admin/books";
    }

    @GetMapping("/book/deleteById/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        adminService.deleteBookById(id);
        redirectAttributes.addFlashAttribute("message", "Book Deleted Successfully!");
        return "redirect:/admin/books";
    }
}