package com.example.Online.Book.Store.controller;

import com.example.Online.Book.Store.dto.*;
import com.example.Online.Book.Store.entity.*;
import com.example.Online.Book.Store.repository.BooksRepository;
import com.example.Online.Book.Store.repository.OrderDetailsRepository;
import com.example.Online.Book.Store.repository.UserRepository;
import com.example.Online.Book.Store.service.CustomUserDetailsService;
import com.example.Online.Book.Store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BooksRepository booksRepository;

    public User getCartValue(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).get();
        if (user != null) {
            int cartItemCount = userService.getCartItemsCount(user);
            model.addAttribute("cartItemCount", cartItemCount);
        } else {
            model.addAttribute("cartItemCount", 0); // Default to 0 if user not found or cart is empty
        }
        return user;
    }

    @GetMapping("/view-books")
    public String getAllBook(@RequestParam(required = false) Long orderId,
                             @RequestParam(defaultValue = "name") String sortBy,
                             @RequestParam(defaultValue = "asc") String sortDirection,
                             Model model, Principal principal) {
        String userEmail = principal.getName(); // Get the email from Principal
        // Load user details using email (assuming email is used as username)
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
        model.addAttribute("user", userDetails);
        User user = userRepository.findByEmail(userEmail).get();
        if (user != null) {
            int cartItemCount = userService.getCartItemsCount(user);
            model.addAttribute("cartItemCount", cartItemCount);

            if (orderId == null) {
                OrderDetails order = userService.getOrCreateOrderForUser(user);
                orderId = order.getOrder_id();
            }
            model.addAttribute("orderId", orderId);
        }

        else {
            model.addAttribute("cartItemCount", 0); // Default to 0 if user not found or cart is empty
        }
        return getBooks(1,orderId, sortBy, sortDirection, model);
    }
    private int calculateStartSerialForPage(int pageNo) {
        int pageSize = 3;
        return (pageNo - 1) * pageSize;
    }


    @GetMapping("/view-books/page/{pageNo}")
    public String getBooks(@PathVariable("pageNo") int pageNo,
                           @RequestParam(required = false) Long orderId,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDirection,
                           Model model){
        User user = getCartValue(model);
        if (orderId == null) {
            // Generate or retrieve an orderId for the user
            OrderDetails order = userService.getOrCreateOrderForUser(user);
            orderId = order.getOrder_id();
        }
        model.addAttribute("orderId", orderId);
        int pageSize = 3;
        int startSerial = calculateStartSerialForPage(pageNo);
        model.addAttribute("startSerial", startSerial);
        Page<Book> bookPage = userService.getBook(pageNo,pageSize, sortBy, sortDirection);
        List<Book> books = bookPage.getContent();
        List<BookDTO> bookModels = books.stream().map(BookDTO::bookEntityToDTO).toList();
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", bookPage.getTotalPages());
        model.addAttribute("books",bookModels);

        return "user/userHome";
    }
    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword,
                         @RequestParam(required = false) Long orderId,Model model){
        List<BookDTO> books = List.of();
        User user = getCartValue(model);
        if(keyword != null && !keyword.isEmpty()){
            books = userService.searchBooks(keyword);
            if (books.size() == 1) {
                BookDTO book = books.get(0);
                model.addAttribute("book", book);
                model.addAttribute("user", user);
                model.addAttribute("orderId", orderId);
                model.addAttribute("ratingCount", userService.getTotalRatingsCount(book.getId()));
                model.addAttribute("reviewCount", userService.getTotalReviewCount(book.getId()));
                return "user/books-details";
            }
        }
        for(BookDTO book : books){
            model.addAttribute("book", book);
        }
        model.addAttribute("user", user);
        model.addAttribute("books",books);
        model.addAttribute("orderId", orderId);
        model.addAttribute("startSerial", 0);

        return "user/search-list";
    }
    @GetMapping("/book/details/{id}")
    public String bookDetails(@PathVariable("id") Long id, @RequestParam(required = false) Long orderId, Model model){
        User user = getCartValue(model);
        model.addAttribute("user", user);
        model.addAttribute("book",userService.getBookDetails(id));
        model.addAttribute("orderId",orderId);
        model.addAttribute("ratingCount", userService.getTotalRatingsCount(id));
        model.addAttribute("reviewCount", userService.getTotalReviewCount(id));
        return "user/books-details";
    }
    @GetMapping("/checkout")
    public String checkOut(@RequestParam("quantity") int quantity, @RequestParam("amount") Float amount, Model model){
        getCartValue(model);
        model.addAttribute("info", new ShippingInfoDTO());
        model.addAttribute("quantity", quantity);
        model.addAttribute("amount", amount);
        return "user/checkout";
    }
    @PostMapping("/add-checkout-details")
    public String createCheckoutDetails(@Valid @ModelAttribute("info") ShippingInfoDTO shippingInfoDTO,
                                        BindingResult bindingResult, @RequestParam("quantity") int quantity,
                                        @RequestParam("amount") Float amount,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors found");

            // Re-populate the model with the necessary attributes for the view
            model.addAttribute("quantity", quantity);
            model.addAttribute("amount", amount);
            getCartValue(model);
            return "user/checkout";
        }
        userService.saveCheckoutDetails(shippingInfoDTO);
        User user = getCartValue(model);
        List<Long> cartItemIds = userService.getCartItemIdsForUser(user);
        OrderDetails orderDetails = userService.createOrder(shippingInfoDTO, user.getUser_id(), cartItemIds);
        model.addAttribute("quantity", quantity);
        model.addAttribute("amount", amount);
        model.addAttribute("order", orderDetails);
        return "user/checkout-details";
    }

    @GetMapping("/checkout-details")
    public String checkoutDetails(@RequestParam("quantity") int quantity, @RequestParam("amount") Float amount, Model model){
        User user = getCartValue(model);
        model.addAttribute("quantity", quantity);
        model.addAttribute("amount", amount);
        return "user/checkout-details";
    }
    @GetMapping("/payment")
    public String showPaymentPage(Model model) {
        getCartValue(model);
        return "user/payment";
    }
    @PostMapping("/processPayment")
    public String processPayment(Model model, Principal principal){
        User user = getCartValue(model);
        userService.processCheckout();
        userService.deleteCartItemsByUser(user);
        return "redirect:/user/order-success";
}
    @GetMapping("/order-success")
    public String showSuccessOrder(Model model){
        getCartValue(model);
        List<CartItem> cartItems = userService.getCartItems();
        model.addAttribute("cartItems", cartItems);
        return "user/order-success";
    }
@GetMapping("/review")
public String showReviewForm(Model model) {
    User user = getCartValue(model);
    System.out.println("User ID (Security): " + user.getUser_id());
    ServiceReviewDTO serviceReviewDTO = new ServiceReviewDTO();
    serviceReviewDTO.setUser(user);
    model.addAttribute("review", serviceReviewDTO);
    model.addAttribute("user", user);
    return "user/add-review";
}
    @PostMapping("/add-review/{userId}")
    public String addReview(@PathVariable("userId") Long userId, @ModelAttribute("review") ServiceReviewDTO serviceReviewDTO, Model model){
        User user = userRepository.findById(userId).get();
        serviceReviewDTO.setUser(user);
        System.out.println("Selected Rating: " + serviceReviewDTO.getRatings()); // Debug statement to verify rating value
        userService.createReviews(userId, serviceReviewDTO);
        model.addAttribute("user", user);
        model.addAttribute("review", serviceReviewDTO);
        return "redirect:/user/view-books";
    }
    @GetMapping("/book-review/{id}")
    public String showBookReviewForm(@PathVariable("id") Long id, Model model){
        User user = getCartValue(model);
        System.out.println("User ID (Security): " + user.getUser_id());
        BookReviewDTO bookReviewDTO = new BookReviewDTO();
        bookReviewDTO.setUser(user);
        model.addAttribute("book", userService.getBookDetails(id));
        model.addAttribute("bookReview", bookReviewDTO);
        model.addAttribute("user", user);
        return "user/book-review";
    }

    @PostMapping("/add-book-review/{userId}/{bookId}")
    public String createBookReview(@PathVariable("userId") Long userId, @PathVariable("bookId") Long bookId, @ModelAttribute("bookReview")BookReviewDTO bookReviewDTO, Model model){
        User user = userRepository.findById(userId).get();
        Book book = booksRepository.findById(bookId).get();
        bookReviewDTO.setUser(user);
        bookReviewDTO.setBook(book);
        userService.createBookReview(userId,bookId, bookReviewDTO);
        model.addAttribute("user", user);
        model.addAttribute("book", book);
        model.addAttribute("bookReview", bookReviewDTO);
        return "redirect:/user/book/details/{bookId}";
    }

    @GetMapping("/review-details/{bookId}")
    public String viewReviews(@PathVariable("bookId") Long bookId, Model model){
        model.addAttribute("bookReviews", userService.getBookReviews(bookId));
        return "user/book-review-details";
    }
    @GetMapping("/cart")
    public String viewCart(Model model){
        User user = getCartValue(model);
        List<CartItem> cartItems = userService.getCartItemsForUser(user);
        List<BookDTO> bookDTOs = new ArrayList<>();
        for (CartItem i : cartItems) {
            bookDTOs.add(BookDTO.bookEntityToDTO(i.getBook()));
        }
        model.addAttribute("books", bookDTOs);
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("quantity", 1);
        return "user/cart";
    }
    @PostMapping("/cart/add/{userId}")
    public String addToCart(@PathVariable("userId") Long userId, @RequestParam Long bookId, @RequestParam Long orderId,
                            @ModelAttribute("cartItems") CartItem cartItem, Model model, Principal principal) {
        String email = principal.getName();
        User user1 = userRepository.findByEmail(email).get();
        User user = userRepository.findById(userId).get();
        model.addAttribute("user", user);
        cartItem.setUser(user1);
        userService.addToCart(bookId, userId, orderId);
        return "redirect:/user/view-modal/" + bookId;
    }
    @PostMapping("/cart/update/{bookId}")
    public String updateCart(@PathVariable Long bookId, @RequestParam int quantity, RedirectAttributes redirectAttributes) {
         userService.updateCartItem(bookId, quantity);
        redirectAttributes.addFlashAttribute("message", "Quantity updated successfully!");

        return "redirect:/user/cart";
    }
    @GetMapping("/view-modal/{id}")
    public String viewModal(@PathVariable("id") Long id, Model model){
        model.addAttribute("book", userService.getBookDetails(id));
        return "user/cartModalView";
    }

    @GetMapping("/delete-cart-item/{id}")
    public String deleteCartItem(@PathVariable("id") Long cartId){
        userService.deleteCartItem(cartId);
        return "redirect:/user/cart";
    }
}
