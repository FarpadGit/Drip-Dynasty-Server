package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.Customer;
import com.farpad.dripServer.models.Order;
import com.farpad.dripServer.models.Product;
import com.farpad.dripServer.models.clientSideData.OrderFormData;
import com.farpad.dripServer.services.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@AllArgsConstructor
@RestController
@RequestMapping("/paypal")
public class PaypalController {

    private ProductService productService;
    private CustomerService customerService;
    private OrderService orderService;
    private EmailService emailService;
    private PaypalService paypalService;

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(@RequestBody String body){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonBody = objectMapper.readTree(body);
            String productId = jsonBody.get("productId").asText();
            Product product = productService.getProduct(productId);
            if(product == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            String res = paypalService.createOrder(productId, product.getPrice().toString());
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (JsonProcessingException ignored) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/purchase/{transactionId}")
    public ResponseEntity<String> buyProduct(@PathVariable String transactionId, @ModelAttribute OrderFormData bodyParams) {
        boolean isValidTransaction = paypalService.validateOrder(transactionId);
        if(!isValidTransaction) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Customer customer = customerService.getCustomerByEmail(bodyParams.getCustomerEmail());
        if(customer == null) {
            customer = customerService.createCustomer(bodyParams.getCustomerEmail());
        }
        String productId = bodyParams.getProductId();
        String customerId = customer.getId();
        Integer pricePaid = Integer.parseInt(bodyParams.getPricePaid());
        Order newOrder = orderService.createOrder(productId, customerId, pricePaid);

        customer.getOrders().add(newOrder.getId());
        customerService.saveCustomer(customer);

        Product product = productService.getProduct(productId);
        if(product.getOrders() == null) product.setOrders(new ArrayList<>());
        product.getOrders().add(newOrder.getId());

        productService.saveProduct(product);

        String email = customer.getEmail();
        new Thread(() -> {
            emailService.sendPurchaseSuccessEmail(email, newOrder.getId());
        }).start();

        return new ResponseEntity<>(newOrder.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/get-bearer")
    /* testing only */
    public ResponseEntity<String> getBearer(){
        return new ResponseEntity<>(paypalService.getBearer(), HttpStatus.OK);
    }
}