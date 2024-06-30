package com.example.Online.Book.Store.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ShippingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipping_id;
    private String full_name;
    private String phone_no;
    private String city;
    private String address;

    @OneToOne(mappedBy = "shippingInfo")
    private OrderDetails orderDetails;
}
