package com.example.Online.Book.Store.controller;

import com.example.Online.Book.Store.dto.UserDTO;
import com.example.Online.Book.Store.entity.User;
import com.example.Online.Book.Store.service.LoginService;
import com.example.Online.Book.Store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model){
        return "home";
    }
    //admin login
    @GetMapping("/user-login")
    public String showAdminLogin(@RequestParam(value = "error", required = false) String error, User user, Model model, RedirectAttributes redirectAttributes){
        if(error != null){
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        model.addAttribute("user", user);
        return "userLogin";
    }
    @GetMapping("/register")
    public String showRegisterForm(User user, Model model){
        model.addAttribute("user", user);
        return "register";
    }
    @PostMapping("/register-user")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){
       if(bindingResult.hasErrors()){
           System.out.println("Validation errors occurred");
           return "register";
       }
        model.addAttribute("user", userDTO);
        loginService.createUser(userDTO);
        redirectAttributes.addFlashAttribute("message", "Registration Successfully Complete!");
        return "redirect:/register";
    }
}
