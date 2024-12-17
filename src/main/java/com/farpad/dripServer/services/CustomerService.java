package com.farpad.dripServer.services;

import com.farpad.dripServer.models.Customer;
import com.farpad.dripServer.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomer(String id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer getCustomerByOrder(String orderId) {
        return customerRepository.findByOrderId(orderId);
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findCustomerByEmail(email);
    }

    public Customer createCustomer(String email) {
        try {
            String uuid = UUID.randomUUID().toString();
            List<String> orders = new ArrayList<>();
            Date now = new Date();

            Customer newCustomer = new Customer(uuid, email, orders, now, now);
            customerRepository.save(newCustomer);
            return newCustomer;
        } catch (Exception e) {
            return null;
        }
    }

    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public boolean deleteCustomer(String id) {
        Customer customerToDelete = getCustomer(id);
        if (customerToDelete != null) {
            customerRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public void deleteAllCustomers() {
        customerRepository.deleteAll();
    }
}
