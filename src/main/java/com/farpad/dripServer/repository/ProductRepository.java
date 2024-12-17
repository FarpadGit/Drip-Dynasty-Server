package com.farpad.dripServer.repository;

import com.farpad.dripServer.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    public long count();

    @Query("{orders: {$all: [?0]}}")
    public Product findByOrderId(String orderId);

    @Query("{categories: {$all: [?0]}}")
    public Page<Product> findAllByCategory(String category, Pageable pageable);

    public List<Product> findTop10ByOrderByCreatedAtDesc();

    @Aggregation(pipeline = {
            "{ $addFields: { orders_count: {$size: { \"$ifNull\": [ \"$orders\", [] ] } } }}",
            "{ $sort: { orders_count: -1 } }",
            "{ $limit: 10 }"
    })
    public List<Product> findTop10ByOrderByOrdersDesc();
}
