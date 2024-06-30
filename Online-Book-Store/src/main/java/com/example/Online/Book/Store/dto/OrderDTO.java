package com.example.Online.Book.Store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Date orderDate;
    private Long userId;
    private ShippingInfoDTO shippingInfo;
    private List<Long> cartItemsId;
}
