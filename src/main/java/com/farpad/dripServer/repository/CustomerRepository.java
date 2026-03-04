package com.farpad.dripServer.repository;

import com.farpad.dripServer.models.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    long count();

    @Query("{email: ?0}")
    Customer findCustomerByEmail(String email);

    @Query("{orders: {$all: [?0]}}")
    Customer findByOrderId(String orderId);
}
