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
    @Min(value = 11, message = "Phone number should have 11 characters")
    @Pattern(regexp = "\\d+", message = "Phone number must be numeric")
    private String phone_no;

    @NotBlank(message = "City name is required!")
    private String city;

    @NotBlank(message = "Address is required!")
    private String address;


    public static ShippingInfoDTO infoToDTO(ShippingInfo shippingInfo){
        ShippingInfoDTO shippingInfoDTO = new ShippingInfoDTO();
        shippingInfoDTO.setFull_name(shippingInfo.getFull_name());
        shippingInfoDTO.setPhone_no(shippingInfo.getPhone_no());
        shippingInfoDTO.setCity(shippingInfo.getCity());
        shippingInfoDTO.setAddress(shippingInfo.getAddress());
        return shippingInfoDTO;
    }
}
