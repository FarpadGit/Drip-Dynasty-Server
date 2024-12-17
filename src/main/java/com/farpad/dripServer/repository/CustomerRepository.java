package com.farpad.dripServer.repository;

import com.farpad.dripServer.models.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    public long count();

    @Query("{email: ?0}")
    public Customer findCustomerByEmail(String email);

    @Query("{orders: {$all: [?0]}}")
    public Customer findByOrderId(String orderId);
}
