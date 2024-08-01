package com.example.Online.Book.Store.dto;

import com.example.Online.Book.Store.entity.ShippingInfo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingInfoDTO {
    @NotBlank(message = "Full name is required!")
    private String full_name;

    @NotBlank(message = "Phone no is required!")
    @Size(min = 11, max = 11, message = "Phone number should have exactly 11 characters")
    @Pattern(regexp = "\\d+", message = "Phone number must be numeric")
    private String phone_no;

    @NotBlank(message = "City name is required!")
    private String city;

    @NotBlank(message = "Address is required!")
    private String address;

}
