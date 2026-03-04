package com.farpad.dripServer.repository;

import com.farpad.dripServer.models.Product;
import org.bson.BSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    long count();

    @Query("{slug: ?0}")
    Product findBySlug(String slug);

    @Query("{orders: {$all: [?0]}}")
    Product findByOrderId(String orderId);

    @Query("?0")
    Page<Product> findAllByFilters(BSONObject query, Pageable pageable);

    Product findTop1ByIsAvailableForPurchaseOrderByPriceDesc(Boolean isAvailableForPurchase);

    List<Product> findTop10ByIsAvailableForPurchaseOrderByCreatedAtDesc(Boolean isAvailableForPurchase);

    @Aggregation(pipeline = {
            "{ $addFields: { orders_count: {$size: { \"$ifNull\": [ \"$orders\", [] ] } } }}",
            "{ $match: { isAvailableForPurchase: true } }",
            "{ $sort: { orders_count: -1 } }",
            "{ $limit: 10 }"
    })
    List<Product> findTop10ByOrderByOrdersDesc();

    @Aggregation(pipeline = {
            "{ $match: { isAvailableForPurchase: true, searchTags: { $ne: null } } }",
            "{ $unwind: \"$searchTags\" }",
            "{ $group: { _id: { name: \"$searchTags.name\", value: \"$searchTags.value\" } } }",
            "{ $project: { name: \"$_id.name\", value: \"$_id.value\" } }"
    })
    List<Product.SearchTag> findAllSearchTags();
}
