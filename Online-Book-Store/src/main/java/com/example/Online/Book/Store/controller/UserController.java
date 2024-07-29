package com.example.Online.Book.Store.controller;

import com.example.Online.Book.Store.dto.*;
import com.example.Online.Book.Store.entity.*;
import com.example.Online.Book.Store.exception.*;
import com.example.Online.Book.Store.repository.BooksRepository;
import com.example.Online.Book.Store.repository.UserRepository;
import com.example.Online.Book.Store.service.CustomUserDetailsService;
import com.example.Online.Book.Store.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


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

    @Autowired
    private ModelMapper modelMapper;
    public BookDTO bookEntityToDTO(Book book){
        BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
        bookDTO.setImageBase64(Base64.getEncoder().encodeToString(book.getImage()));
        return bookDTO;
    }
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
    public String getAllBook(@RequestParam(defaultValue = "name") String sortBy,
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
        }
        else {
            model.addAttribute("cartItemCount", 0); // Default to 0 if user not found or cart is empty
        }
        return getBooks(1, sortBy, sortDirection, model);
    }
    private int calculateStartSerialForPage(int pageNo) {
        int pageSize = 3;
        return (pageNo - 1) * pageSize;
    }


    @GetMapping("/view-books/page/{pageNo}")
    public String getBooks(@PathVariable("pageNo") int pageNo,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDirection,
                           Model model){
        User user = getCartValue(model);
        int pageSize = 3;
        int startSerial = calculateStartSerialForPage(pageNo);
        model.addAttribute("startSerial", startSerial);
        Page<Book> bookPage = userService.getBook(pageNo,pageSize, sortBy, sortDirection);
        List<Book> books = bookPage.getContent();
        List<BookDTO> bookModels = new ArrayList<>();
        for(Book book:books){
            bookModels.add(this.bookEntityToDTO(book));
        }
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", bookPage.getTotalPages());
        model.addAttribute("books",bookModels);

        return "user/userHome";
    }
    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword,
                         Model model) {
        List<BookDTO> books = List.of();
        User user = getCartValue(model);
        if(keyword != null && !keyword.isEmpty()){
            books = userService.searchBooks(keyword);
            if(books.isEmpty()){
                throw new BookNotFoundException("Book with this name " + keyword + " is not found!");
            }
            else if(books.size() == 1) {
                BookDTO book = books.get(0);
                model.addAttribute("book", book);
                model.addAttribute("user", user);
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
        model.addAttribute("startSerial", 0);

        return "user/search-list";
    }
    @GetMapping("/book/details/{id}")
    public String bookDetails(@PathVariable("id") Long id,
                              Model model){
        User user = getCartValue(model);
        model.addAttribute("user", user);
        model.addAttribute("book",userService.getBookDetails(id));
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
                                        @RequestParam("amount") Float amount, HttpSession session,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            System.out.println("Validation errors found");
            // Re-populate the model with the necessary attributes for the view
            model.addAttribute("quantity", quantity);
            model.addAttribute("amount", amount);
            getCartValue(model);
            return "user/checkout";
        }
        ShippingInfo shippingInfo = userService.saveCheckoutDetails(shippingInfoDTO);
        Long shippingId = shippingInfo.getShipping_id();
        session.setAttribute("shippingId", shippingId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("amount", amount);
        model.addAttribute("shippingId", shippingId);
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
    public String showPaymentPage(Model model, HttpSession session) {
        User user = getCartValue(model);
        Long shippingInfoId = (Long) session.getAttribute("shippingId");
        model.addAttribute("user", user);
        model.addAttribute("shippingId", shippingInfoId);
        return "user/payment";
    }
    @PostMapping("/processPayment")
    public String processPayment(@RequestParam("userId") Long userId, HttpSession session, Model model){
        User user = getCartValue(model);
        List<Long> cartItemIds = userService.getCartItemIdsForUser(user);
        Long shippingInfoId = (Long) session.getAttribute("shippingId");
        ShippingInfo shippingInfo = userService.getShippingId(shippingInfoId);
        OrderDetails orderDetails = userService.createOrder(shippingInfo, userId, cartItemIds );
        model.addAttribute("order", orderDetails);
        userService.processCheckout();
        userService.deleteCartItemsByUser(user);
        session.removeAttribute("shippingInfoId");
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
        getCartValue(model);
        List<BookReview> reviews = userService.getBookReviews(bookId);
        if(reviews.isEmpty()){
            throw new BookReviewNotFoundException("No review is found for this  book!");
        }
        model.addAttribute("bookReviews",reviews);
        return "user/book-review-details";
    }
    @GetMapping("/cart")
    public String viewCart(Model model){
        User user = getCartValue(model);
        List<CartItem> cartItems = userService.getCartItemsForUser(user);
        List<BookDTO> bookDTOs = new ArrayList<>();
        for (CartItem i : cartItems) {
            bookDTOs.add(this.bookEntityToDTO(i.getBook()));
        }
        model.addAttribute("books", bookDTOs);
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("quantity", 1);
        return "user/cart";
    }
    @PostMapping("/cart/add/{userId}")
    public String addToCart(@PathVariable("userId") Long userId, @RequestParam Long bookId,
                            @ModelAttribute("cartItems") CartItem cartItem, Model model, Principal principal) {
        String email = principal.getName();
        User user1 = userRepository.findByEmail(email).get();
        User user = userRepository.findById(userId).get();
        model.addAttribute("user", user);
        cartItem.setUser(user1);
        BookDTO book = userService.getBookDetails(bookId);
        if(book.getQuantity() == 0){
            throw new OutOfStockException("The book is out of stock!");
        }
        try{
            userService.addToCart(bookId, userId);
            return "redirect:/user/view-modal/" + bookId;
        }
        catch (CartServiceUnavailableException ex){
            throw ex;
        }

    }
    @PostMapping("/cart/update/{bookId}")
    public String updateCart(@PathVariable Long bookId, @RequestParam int quantity) {
        BookDTO book = userService.getBookDetails(bookId);
        if(book.getQuantity() < quantity){
            throw new InsufficientStockException("The requested quantity is not available in stock!");
        }
         userService.updateCartItem(bookId, quantity);
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
