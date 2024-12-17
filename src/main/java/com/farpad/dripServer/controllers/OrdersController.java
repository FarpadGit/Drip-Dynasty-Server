package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.Order;
import com.farpad.dripServer.models.clientSideData.OrderFormData;
import com.farpad.dripServer.services.AggregationService;
import com.farpad.dripServer.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrdersController {

  private final OrderService orderService;
  private final AggregationService aggregationService;

  @GetMapping("")
  public ResponseEntity<List<OrderFormData>> getOrders() {
    List<Order> orders = orderService.getOrders();
    List<OrderFormData> customers = orders.stream().map(aggregationService::populateOrder).toList();
    return new ResponseEntity<>(customers, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
    aggregationService.deleteOrderFromCustomer(id);
    aggregationService.deleteOrderFromProduct(id);

    boolean deleted = orderService.deleteOrder(id);
    if (!deleted) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}