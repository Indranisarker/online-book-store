package com.example.Online.Book.Store.service;


import com.example.Online.Book.Store.dto.BookDTO;
import com.example.Online.Book.Store.entity.Book;
import com.example.Online.Book.Store.entity.OrderDetails;
import com.example.Online.Book.Store.entity.OrderItem;
import com.example.Online.Book.Store.entity.ServiceReview;
import com.example.Online.Book.Store.repository.BooksRepository;
import com.example.Online.Book.Store.repository.OrderDetailsRepository;
import com.example.Online.Book.Store.repository.OrderItemsRepository;
import com.example.Online.Book.Store.repository.ServiceReviewRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
@Service
public class AdminService {
    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private ServiceReviewRepository serviceReviewRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OrderDetailsRepository detailsRepository;

    public void bookEntityToDTO(Book book){
         BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
         bookDTO.setImageBase64(Base64.getEncoder().encodeToString(book.getImage()));
    }

    public Page<Book> getBooks(int pageNo, int pageSize, String sortBy, String sortDirection) {
       Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable paging = PageRequest.of(pageNo - 1,pageSize,sort);
        return booksRepository.findAll(paging);

    }

    @Transactional
    public void createBook(BookDTO bookDTO) {
        try {
            // Save the file locally
            MultipartFile file = bookDTO.getImage();
            if (file == null || file.isEmpty()) {
                throw new IOException("File is empty or not provided.");
            }

            String uploadDir = "uploads/"; // Ensure the directory path has a trailing slash
            Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new IOException("File name is not provided.");
            }

            Path filePath = uploadPath.resolve(fileName);
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath);
            }

            // Convert MultipartFile to BufferedImage
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(200, 200)
                    .outputFormat("jpg")
                    .toOutputStream(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            Book book = modelMapper.map(bookDTO, Book.class);
            book.setImage(imageBytes);
            Book savedBook = booksRepository.save(book);
            this.bookEntityToDTO(savedBook);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Book updateBook(Long id) {
        Optional<Book> book = booksRepository.findById(id);
        Book newBook = null;
        if(book.isPresent()){
            newBook = book.get();
            booksRepository.save(newBook);
        }
        return newBook;
    }

    public BookDTO updateBookById(Long id, BookDTO bookDTO) {
        Book existingBook = booksRepository.findById(id).get();
        Book presentBook = null;
        try {
            MultipartFile file = bookDTO.getImage();
            if (file != null && !file.isEmpty()) {
                // Define the upload directory relative to the current working directory
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);

                // Ensure the upload directory exists
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.isEmpty()) {
                    throw new IOException("File name is not provided.");
                }

                Path filePath = uploadPath.resolve(fileName);

                // Copy the file to the target location
                try (var inputStream = file.getInputStream()) {
                    Files.copy(inputStream, filePath);
                }

                // Convert file to byte array
                byte[] imageBytes = file.getBytes();
                    presentBook = modelMapper.map(bookDTO, Book.class);
                    presentBook.setImage(imageBytes);
                    presentBook.setImagePath(filePath.toString());
                    booksRepository.save(presentBook);
                }
            else{
                presentBook = modelMapper.map(bookDTO, Book.class);
                presentBook.setImage(existingBook.getImage());
                presentBook.setImagePath(existingBook.getImagePath());
                booksRepository.save(presentBook);
            }
            presentBook.setId(existingBook.getId());
             this.bookEntityToDTO(presentBook);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bookDTO;
    }
        public void deleteBookById(Long id) {
        booksRepository.deleteById(id);
    }
    public List<ServiceReview> getAllReviews() {
        return serviceReviewRepository.findAll();
    }
    public List<OrderDetails> getAllOrderDetails() {
        return detailsRepository.findAll();
    }
}
