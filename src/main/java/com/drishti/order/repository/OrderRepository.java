package com.drishti.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drishti.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}
