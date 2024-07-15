package com.example.Online.Book.Store.service;

import com.example.Online.Book.Store.dto.BookDTO;
import com.example.Online.Book.Store.dto.BookReviewDTO;
import com.example.Online.Book.Store.dto.ServiceReviewDTO;
import com.example.Online.Book.Store.dto.ShippingInfoDTO;
import com.example.Online.Book.Store.entity.*;
import com.example.Online.Book.Store.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceReviewRepository serviceReviewRepository;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private ShippingInfoRepository shippingInfoRepository;

    @Autowired
    private OrderDetailsRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    public void infoToDTO(ShippingInfo shippingInfo){
         modelMapper.map(shippingInfo, ShippingInfoDTO.class);
    }
    public BookDTO bookEntityToDTO(Book book){
        BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
        bookDTO.setImageBase64(Base64.getEncoder().encodeToString(book.getImage()));
        return bookDTO;
    }

    public void reviewEntityToDTO(ServiceReview serviceReview){
         modelMapper.map(serviceReview, ServiceReviewDTO.class);
    }
    public void bookReviewEntityToDTO(BookReview bookReview){
         modelMapper.map(bookReview,BookReviewDTO.class);
    }

    public Page<Book> getBook(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable paging = PageRequest.of(pageNo - 1, pageSize, sort);
        return booksRepository.findAll(paging);
    }

    public BookDTO getBookDetails(Long id) {
        Book book = booksRepository.findById(id).get();
        return this.bookEntityToDTO(book);
    }

    public void createReviews(Long userId, ServiceReviewDTO serviceReviewDTO) {
        userRepository.findById(userId).get();
        ServiceReview serviceReview = modelMapper.map(serviceReviewDTO, ServiceReview.class);
        serviceReview.setCreateTime(new Date());
        serviceReviewRepository.save(serviceReview);
        this.reviewEntityToDTO(serviceReview);
    }

    @Transactional
    public List<BookDTO> searchBooks(String keyword) {
            List<Book> books = booksRepository.findBookByNameIgnoreCaseOrCategoryIgnoreCase(keyword);
           List<BookDTO> bookDTOS = new ArrayList<>();
            if(books != null){
                for(Book book : books){
                    bookDTOS.add(this.bookEntityToDTO(book));
                }
            }
            return bookDTOS;
    }

    @Transactional
    public void processCheckout() {
        List<CartItem> cartItems = cartRepository.findAll();
        // Assume payment is processed here
        for (CartItem cartItem : cartItems) {
            Book book = booksRepository.findById(cartItem.getBook().getId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            if (book.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for book: " + book.getName());
            }
            book.setQuantity(book.getQuantity() - cartItem.getQuantity());
            booksRepository.save(book);
        }
    }

    public List<CartItem> getCartItems() {
        return cartRepository.findAll();
    }

    @Transactional
    public void addToCart(Long bookId, Long userId, Long orderId) {
        Book book = booksRepository.findById(bookId).get();
        User user = userRepository.findById(userId).get();
        OrderDetails order = orderRepository.findById(orderId).get();
        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setUser(user);
        cartItem.setOrder(order);
        CartItem savedItem = cartRepository.save(cartItem);
        this.bookEntityToDTO(savedItem.getBook());

    }
    @Transactional
    public void updateCartItem(Long bookId, int quantity) {
        CartItem cartItem = cartRepository.findByBookId(bookId);

        if (cartItem != null) {
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
        } else {
            Optional<Book> book = booksRepository.findById(bookId);
            if (book.isPresent()) {
                Book book1 = book.get();
                cartItem = new CartItem();
                cartItem.setBook(book1);
                cartItem.setQuantity(quantity);
                cartRepository.save(cartItem);
            } else {
                throw new RuntimeException("Book not found");
            }
        }
    }

    public void deleteCartItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }


    public void createBookReview(Long userId, Long bookId, BookReviewDTO bookReviewDTO) {
        userRepository.findById(userId).get();
        booksRepository.findById(bookId).get();
        BookReview bookReview = modelMapper.map(bookReviewDTO,BookReview.class);
        bookReview.setCreateDate(new Date());
        bookReviewRepository.save(bookReview);
        this.bookReviewEntityToDTO(bookReview);
    }

    public int getTotalReviewCount(Long bookId) {
        Optional<Book> bookOptional = booksRepository.findById(bookId);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            List<BookReview> reviews = book.getReviews();
            return reviews.size();
        }
        return 0;
    }
public Double getTotalRatingsCount(Long bookId) {
    return bookReviewRepository.findAverageRatingByBookId(bookId);
}

    public List<BookReview> getBookReviews(Long bookId) {
        return bookReviewRepository.findByBookId(bookId);
    }

    @Transactional
    public void saveCheckoutDetails(ShippingInfoDTO shippingInfoDTO) {
        ShippingInfo shippingInfo = this.modelMapper.map(shippingInfoDTO, ShippingInfo.class);
        shippingInfoRepository.save(shippingInfo);
        this.infoToDTO(shippingInfo);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsForUser(User user ) {
        return cartRepository.findByUser(user);

    }

    @Transactional
    public void deleteCartItemsByUser(User user) {
        cartRepository.deleteCartItemsByUser(user);
    }

    public int getCartItemsCount(User user) {
        return cartRepository.countByUser(user);
    }

    @Transactional
    public OrderDetails createOrder(ShippingInfoDTO shippingInfoDTO, Long userId, List<Long> cartItemIds) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<CartItem> cartItems = cartRepository.findAllById(cartItemIds);

        ShippingInfo info = this.modelMapper.map(shippingInfoDTO, ShippingInfo.class);

        OrderDetails order = new OrderDetails();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setShippingInfo(info);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(cartItem.getBook());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getBook().getPrice());
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }

    public OrderDetails getOrCreateOrderForUser(User user) {
        OrderDetails order = new OrderDetails();
        order.setUser(user);
        order = orderRepository.save(order);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Long> getCartItemIdsForUser(User user) {
        return cartRepository.findByUser(user).stream()
                .map(cartItem -> cartItem.getCardId())
                .collect(Collectors.toList());
    }

    public OrderDetails createOrderForUser(User user) {
        OrderDetails order = new OrderDetails();
        order.setUser(user);
        order.setOrderDate(new Date());
        return orderRepository.save(order);
    }

    public OrderDetails getOrderById(Long orderId) {
        return orderRepository.findById(orderId).get();
    }
}
