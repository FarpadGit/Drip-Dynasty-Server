package com.farpad.dripServer.services;

import com.farpad.dripServer.models.Order;
import com.farpad.dripServer.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(String productId, String customerId, Integer pricePaid) {
        try {
            String uuid = UUID.randomUUID().toString();

            Date now = new Date();
            Order newOrder = new Order(uuid, productId, customerId, pricePaid, now, now);
            orderRepository.save(newOrder);
            return newOrder;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteOrder(String id) {
        Order orderToDelete = getOrder(id);
        if (orderToDelete != null) {
            orderRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public void deleteAllOrders() {
        orderRepository.deleteAll();
    }
}
