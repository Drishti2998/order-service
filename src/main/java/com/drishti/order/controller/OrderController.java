package com.drishti.order.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.drishti.order.dto.OrderRequest;
import com.drishti.order.model.Order;
import com.drishti.order.service.OrderService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	@CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
	@TimeLimiter(name = "inventory")
	@Retry(name = "inventory")
	public CompletableFuture<String> createOrder(@RequestBody OrderRequest orderRequest) {
		return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));
		// return "Order Placed Successfully";
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Order> getOrders() {
		return orderService.getOrders();
	}

	public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
		return CompletableFuture.supplyAsync(() -> "Opps!! Inventory is down please try after sometime");
	}
}
