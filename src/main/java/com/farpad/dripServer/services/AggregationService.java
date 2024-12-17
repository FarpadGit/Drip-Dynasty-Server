package com.farpad.dripServer.services;

import com.farpad.dripServer.models.Customer;
import com.farpad.dripServer.models.Order;
import com.farpad.dripServer.models.Product;
import com.farpad.dripServer.models.clientSideData.CustomerFormData;
import com.farpad.dripServer.models.clientSideData.OrderFormData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AggregationService {

    private final ProductService productService;
    private  final CustomerService customerService;
    private final OrderService orderService;

    public OrderFormData populateOrder(Order order) {
        Product product = productService.getProductByOrder(order.getId());
        Customer customer = customerService.getCustomerByOrder(order.getId());

        OrderFormData result = order.asClientSide();
        result.setCustomerEmail(customer.getEmail());
        result.setProductName(product.getName());

        return  result;
    }

    public CustomerFormData populateCustomer(Customer customer) {
        List<Order> orders = customer.getOrders().stream().map(orderService::getOrder).toList();
        List<Integer> orderPrices = orders.stream().map(Order::getPricePaid).toList();
        Integer totalOrderValue = orderPrices.stream().reduce(0, Integer::sum);

        CustomerFormData result = customer.asClientSide();
        result.setTotalOrderValue(totalOrderValue.toString());

        return  result;
    }

    public void deleteOrderFromCustomer(String orderId) {
        Customer customer = customerService.getCustomerByOrder(orderId);
        if(customer == null) return;
        customer.getOrders().remove(orderId);
        customerService.saveCustomer(customer);
    }

    public void deleteOrdersByCustomer(String customerId) {
        Customer customer = customerService.getCustomer(customerId);
        if(customer == null) return;
        List<String> orders = customer.getOrders();

        orders.forEach(this::deleteOrderFromProduct);
        orders.forEach(orderService::deleteOrder);
    }

    public void deleteOrderFromProduct(String orderId) {
        Order order = orderService.getOrder(orderId);
        Product product = productService.getProduct(order.getProductId());
        if(product == null) return;
        product.getOrders().remove(orderId);
        productService.saveProduct(product);
    }
}
