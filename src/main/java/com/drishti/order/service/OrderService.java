package com.drishti.order.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.drishti.order.dto.InventoryResponse;
import com.drishti.order.dto.OrderLineItemsDto;
import com.drishti.order.dto.OrderRequest;
import com.drishti.order.model.Order;
import com.drishti.order.model.OrderLineItems;
import com.drishti.order.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;
	private final WebClient.Builder webClientBuilder;
	
	private final KafkaProducer kafkaProducer;

	public String placeOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());

		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto)
				.toList();

		order.setOrderLineItemsList(orderLineItems);

		List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

		// Call Inventory Service, and place order if product is in
		// stock in synchronous manner that's why adding block at end in this method
		// If u want to run aysnchronously then run without block
		InventoryResponse[] inventoryResponsArray = webClientBuilder.build().get()
				.uri("http://inventory-service/api/inventory",
						uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
				.retrieve().bodyToMono(InventoryResponse[].class).block();

		boolean allProductsInStock = Arrays.stream(inventoryResponsArray).allMatch(InventoryResponse::isInStock);

		if (allProductsInStock) {
			orderRepository.save(order);
			//kafkaProducer.sendMessage("Order is created successfully");
			return "Order Placed Successfully";
		} else {
			//kafkaProducer.sendMessage("Opps!! Order creation failed");
			throw new IllegalArgumentException("Product is not in stock, please try again later");
		}
		
		
	}

	public List<Order> getOrders() {
		return orderRepository.findAll();
	}

	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = OrderLineItems.builder().price(orderLineItemsDto.getPrice())
				.skuCode(orderLineItemsDto.getSkuCode()).quantity(orderLineItemsDto.getQuantity()).build();
		return orderLineItems;
	}
}
