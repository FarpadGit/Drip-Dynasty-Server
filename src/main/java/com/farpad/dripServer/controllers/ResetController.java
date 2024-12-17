package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.Product;
import com.farpad.dripServer.models.clientSideData.ProductFormData;
import com.farpad.dripServer.services.CustomerService;
import com.farpad.dripServer.services.ImageService;
import com.farpad.dripServer.services.OrderService;
import com.farpad.dripServer.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@AllArgsConstructor
@RestController
public class ResetController {

    private final ProductService productService;
    private final ImageService imageService;
    private final CustomerService customerService;
    private final OrderService orderService;

    @PostMapping("/resetDB")
    public ResponseEntity<Void> resetDatabase(){
        try {
                ObjectMapper mapper = new ObjectMapper();
                List<ProductFormData> products = mapper.readValue(new File("src/main/resources/static/dbseed/dbseed.json"), mapper.getTypeFactory().constructCollectionType(List.class, ProductFormData.class));

                productService.deleteAllProducts();
                customerService.deleteAllCustomers();
                orderService.deleteAllOrders();

                products.forEach(product -> {
                    product.setIsActive("true");
                    product.setImagePaths(product.getImagePaths().stream().map(imgPath -> Product.imageUploadDirectoryAccessPath + product.getId() + '/' + imgPath).toList());
                    Product newProduct = productService.createProduct(product);
                });

                imageService.loadStarterImages();

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception ignored) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}