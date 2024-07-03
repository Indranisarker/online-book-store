package com.example.Online.Book.Store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ShippingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipping_id;

    @NotBlank(message = "Full name is mandatory!")
    private String full_name;

    @NotBlank(message = "Phone no is mandatory!")
    private String phone_no;

    @NotBlank(message = "City name is mandatory!")
    private String city;

    @NotBlank(message = "Address is mandatory!")
    private String address;


    @OneToOne(mappedBy = "shippingInfo")
    private OrderDetails orderDetails;
}
