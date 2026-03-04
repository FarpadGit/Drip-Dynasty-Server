package com.farpad.dripServer.services;

import com.farpad.dripServer.models.clientSideData.ProductFormData;
import com.farpad.dripServer.models.Product;
import com.farpad.dripServer.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@AllArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final Integer pageSize = 20;

    public Page<Product> getProducts(Product.Filters filters) {
        if(filters == null) return new PageImpl<>(productRepository.findAll());

        Pageable pageable;
        Sort sort;

        if(filters.hasSorting()) {
            sort = Sort.by(filters.getSortBy());
            if(filters.getSortOrder().equals("ASC")) sort = sort.ascending();
            if(filters.getSortOrder().equals("DESC")) sort = sort.descending();
            pageable = PageRequest.of(filters.getPage(), pageSize, sort);
        }
        else pageable = PageRequest.of(filters.getPage(), pageSize);

//      dynamic query construction using MongoDB query syntax:
//      {price: {$gte: priceMin, $lte: priceMax}, categories: {$all: [category]}, $and: [{searchTags: {$in: [{tagGroup1}, *]}, {searchTags: {$in: [{tagGroup2}, *]}}], isAvailableForPurchase: true}
        BSONObject query = new BasicBSONObject("isAvailableForPurchase", true);
        if(filters.hasPriceFilters()) {
            BSONObject priceQuery = new BasicBSONObject();
            if(filters.getPriceMin() != null) priceQuery.put("$gte", filters.getPriceMin());
            if(filters.getPriceMax() != null) priceQuery.put("$lte", filters.getPriceMax());
            query.put("price", priceQuery);
        }
        if(filters.getCategory() != null) query.put("categories", new BasicBSONObject("$all", List.of(filters.getCategory())));
        if(filters.hasTagFilters()) {
            List<BSONObject> andQuery = new ArrayList<>();
            filters.getTags().forEach((tagGroup, searchTags) -> {
                List<BasicBSONObject> tagList = searchTags.stream().map(tag -> new BasicBSONObject("name", tag.getName()).append("value", tag.getValue())).toList();
                BasicBSONObject inQuery = new BasicBSONObject("$in", tagList);
                andQuery.add(new BasicBSONObject("searchTags", inQuery));
            });
            query.put("$and", andQuery);
        }

        return productRepository.findAllByFilters(query, pageable);
    }

    public List<Product> getNewestProducts() {
        return productRepository.findTop10ByIsAvailableForPurchaseOrderByCreatedAtDesc(true);
    }

    public List<Product> getMostPopularProducts() {
        return productRepository.findTop10ByOrderByOrdersDesc();
    }

    public Product getProduct(String id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public Product getProductByOrder(String orderId) {
        return productRepository.findByOrderId(orderId);
    }

    public boolean setProduct(Product product, ProductFormData formData) {
        try {
            product.setName(formData.getName());
            product.setSlug(formData.getSlug());
            product.setDescription(formData.getDescription());
            product.setCategories(formData.getCategories());
            int price = Integer.parseInt(formData.getPrice());
            if(price < 1) price = 1;
            product.setPrice(price);
            int discount = formData.getDiscount() == null ? 0 : Integer.parseInt(formData.getDiscount());
            if(discount < 0) discount = 0;
            if(discount > price) discount = price;
            product.setDiscount(discount);
            product.setEmailMessage(formData.getEmailMessage());
            product.setVariants(formData._getVariants());
            product.setSearchTags(formData._getSearchTags());
            Integer defaultStock = null;
            if(formData.getVariantsJson().isEmpty() && formData.getDefaultStock() != null) defaultStock = Integer.parseInt(formData.getDefaultStock());
            if(defaultStock != null && defaultStock < 0) defaultStock = 0;
            product.setDefaultStock(defaultStock);
            product.setIsAvailableForPurchase(Boolean.parseBoolean(formData.getIsActive()));

            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public Product createProduct(ProductFormData newProductData, Boolean noImages) {
            Product newProduct = new Product();
            newProduct.setId(UUID.randomUUID().toString());
            boolean success = setProduct(newProduct, newProductData);
            if(!success) return null;
            newProduct.setFileNames(noImages ? new ArrayList<>() : newProductData.getImagePaths());
            newProduct.setOrders(new ArrayList<>());
            Date now = new Date();
            newProduct.setCreatedAt(now);
            newProduct.setUpdatedAt(now);

            productRepository.save(newProduct);
            return newProduct;
    }

    public Product createProduct(ProductFormData newProductData) {
        return createProduct(newProductData, false);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public boolean deleteProduct(String id) {
        Product productToDelete = getProduct(id);
        if(productToDelete == null) return false;
        List<String> productOrders = productToDelete.getOrders();
        if(productOrders != null && !productOrders.isEmpty()) return false;

        productToDelete.getFileNames().forEach(fileName-> {
            String imageFileName = Path.of(fileName).getFileName().toString();
            imageService.deleteImageFromStorage(Product.imageUploadDirectory + id, imageFileName);
        });
        imageService.deleteFolderFromStorage(Product.imageUploadDirectory + id);
        productRepository.deleteById(id);
        return true;
    }

    public void deleteAllProducts() {
        imageService.deleteAllImages();
        productRepository.deleteAll();
    }

    public List<Product.SearchTag> getSearchTags() {
        return productRepository.findAllSearchTags();
    }

    public Integer getHighestPrice() {
        return productRepository.findTop1ByIsAvailableForPurchaseOrderByPriceDesc(true).getPrice();
    }
}
