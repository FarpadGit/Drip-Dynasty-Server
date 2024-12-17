package com.farpad.dripServer.services;

import com.farpad.dripServer.models.clientSideData.ProductFormData;
import com.farpad.dripServer.models.Product;
import com.farpad.dripServer.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;

    public Page<Product> getProducts(Integer page) {
        if(page == null) return new PageImpl<>(productRepository.findAll());

        Pageable pageable = PageRequest.of(page,20);
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProducts(String category, Integer page) {
        if(category == null) return getProducts(page);

        Pageable pageable = PageRequest.of(page,20);
        return productRepository.findAllByCategory(category, pageable);
    }

    public List<Product> getNewestProducts() {
        return productRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<Product> getMostPopularProducts() {
        return productRepository.findTop10ByOrderByOrdersDesc();
    }

    public Product getProduct(String id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product getProductByOrder(String orderId) {
        return productRepository.findByOrderId(orderId);
    }

    public Product createProduct(ProductFormData newProductData, Boolean noImages) {
        try {
            String uuid = UUID.randomUUID().toString();
            String name = newProductData.getName();
            String description = newProductData.getDescription();
            List<String> categories = newProductData.getCategories();
            int price = Integer.parseInt(newProductData.getPrice());
            if(price < 1) price = 1;
            int discount = newProductData.getDiscount() == null ? 0 : Integer.parseInt(newProductData.getDiscount());
            if(discount < 0) discount = 0;
            if(discount > price) discount = price;
            String extra = newProductData.getExtra();
            List<String> fileNames = noImages ? new ArrayList<>() : newProductData.getImagePaths();
            List<String> orders = new ArrayList<>();
            Boolean isAvailableForPurchase = Boolean.parseBoolean(newProductData.getIsActive());
            Date now = new Date();
            Product newProduct = new Product(uuid, name, description, categories, price, discount, extra, fileNames, isAvailableForPurchase, orders, now, now);
            productRepository.save(newProduct);
            return newProduct;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Product createProduct(ProductFormData newProductData) {
        return createProduct(newProductData, false);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public boolean deleteProduct(String id) {
        Product productToDelete = getProduct(id);
        List<String> productOrders = productToDelete.getOrders();
        if(productToDelete == null) return false;
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
}
