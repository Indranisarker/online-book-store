package com.example.Online.Book.Store.service;

import com.example.Online.Book.Store.dto.UserDTO;
import com.example.Online.Book.Store.entity.User;
import com.example.Online.Book.Store.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;
    public void userEntityToUserDTO(User user){
        modelMapper.map(user, UserDTO.class);
    }
    @Transactional
    public void createUser(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//        User user = new User();
//        user.setFirst_name(userDTO.getFirst_name());
//        user.setLast_name(userDTO.getLast_name());
//        user.setEmail(userDTO.getEmail());
//        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//        user.setRole(userDTO.getRole());
        this.userEntityToUserDTO(user);
        userRepository.save(user);
    }
}
