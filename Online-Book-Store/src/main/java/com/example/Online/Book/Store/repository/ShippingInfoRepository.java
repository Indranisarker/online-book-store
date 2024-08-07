package com.example.Online.Book.Store.repository;

import com.example.Online.Book.Store.entity.ShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, Long> {
}
