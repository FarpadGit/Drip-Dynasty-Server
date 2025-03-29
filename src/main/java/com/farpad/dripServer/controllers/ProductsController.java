package com.farpad.dripServer.controllers;

import java.nio.file.Path;
import java.util.*;

import com.farpad.dripServer.models.clientSideData.ProductFormData;
import com.farpad.dripServer.models.clientSideData.OrderFormData;
import com.farpad.dripServer.models.Product;
import com.farpad.dripServer.models.Customer;
import com.farpad.dripServer.models.Order;
import com.farpad.dripServer.models.clientSideData.ProductPaginatedResponse;
import com.farpad.dripServer.services.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductsController {

  @Value("${drip-app.server-origin}")
  private String serverRootPath;

  private final ProductService productService;
  private final CustomerService customerService;
  private final OrderService orderService;
  private final ImageService imageService;
  private final EmailService emailService;
  private final PaypalService paypalService;

  @GetMapping("")
  public ResponseEntity<ProductPaginatedResponse> getProducts(@RequestParam(required = false) String category, @RequestParam(required = false) String page) {
    Integer _page = page == null ? null : Integer.parseInt(page);
    Page<Product> productPage = productService.getProducts(category, _page);
    List<ProductFormData> products = productPage.stream().map(p -> p.asClientSide(serverRootPath)).toList();
    ProductPaginatedResponse response = new ProductPaginatedResponse(products, productPage.hasNext(), productPage.hasPrevious(), productPage.getTotalPages());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("newest")
  public ResponseEntity<List<ProductFormData>> getNewestProducts() {
    List<ProductFormData> products = productService.getNewestProducts().stream().map(p -> p.asClientSide(serverRootPath)).toList();
    return new ResponseEntity<>(products, HttpStatus.OK);
  }

  @GetMapping("mostPopular")
  public ResponseEntity<List<ProductFormData>> getMostPopularProducts() {
    List<ProductFormData> products = productService.getMostPopularProducts().stream().map(p -> p.asClientSide(serverRootPath)).toList();
    return new ResponseEntity<>(products, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductFormData> getProduct(@PathVariable String id) {
    Product product = productService.getProduct(id);
    if(product == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    return new ResponseEntity<>(product.asClientSide(serverRootPath), HttpStatus.OK);
  }

  @RequestMapping(path = "", method = RequestMethod.POST, consumes = {"multipart/form-data"})
  public ResponseEntity<Void> createProduct(@ModelAttribute ProductFormData bodyParams) {
    Product newProduct = productService.createProduct(bodyParams, true);
    if(newProduct == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

    MultipartFile[] newImageFiles = bodyParams.getImageFiles();
    List<String> imageNames = new ArrayList<>();

    for(MultipartFile imageFile : newImageFiles) {
      String savedImageName = imageService.saveImageToStorage(Product.imageUploadDirectory + newProduct.getId(), imageFile);
      imageNames.add(savedImageName);
    }

    newProduct.setFileNames(imageNames);
    productService.saveProduct(newProduct);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.PUT, consumes = {"multipart/form-data"})
  public ResponseEntity<Void> updateProduct(@PathVariable String id, @ModelAttribute ProductFormData bodyParams) {
    Product product = productService.getProduct(id);
    if (product == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    List<String> imagesToDelete = bodyParams.getImagesToDelete();
    MultipartFile[] newImageFiles = bodyParams.getImageFiles();

    if(imagesToDelete != null && !imagesToDelete.isEmpty()) {
      List<String> deletedImages = new ArrayList<>();

      imagesToDelete.replaceAll(img -> {
        int i = img.indexOf("images/");
        return img.substring(i);
      });

      imagesToDelete.forEach(imagePath-> {
        imageService.deleteImageFromStorage(Product.imageUploadDirectory + id, Path.of(imagePath).getFileName().toString());
        deletedImages.add(imagePath);
      });

      List<String> imagePaths = product.getFileNames();
      imagePaths.removeAll(deletedImages);
      product.setFileNames(imagePaths);
    }

    if(newImageFiles != null) {
      List<String> addedImages = new ArrayList<>();

      for(MultipartFile newImage : newImageFiles) {
        String savedImageName = imageService.saveImageToStorage(Product.imageUploadDirectory + id, newImage);
        addedImages.add(savedImageName);
      }

      List<String> imagePaths = product.getFileNames();
      imagePaths.addAll(addedImages);
      product.setFileNames(imagePaths);
    }

    product.setName(bodyParams.getName());
    product.setDescription(bodyParams.getDescription());
    product.setCategories(bodyParams.getCategories());
    product.setPrice(Integer.parseInt(bodyParams.getPrice()));
    product.setDiscount(Integer.parseInt(bodyParams.getDiscount()));
    product.setExtra(bodyParams.getExtra());
    product.setUpdatedAt(new Date());

    if(product.getFileNames().isEmpty()) product.setIsAvailableForPurchase(false);
    else product.setIsAvailableForPurchase(Boolean.parseBoolean(bodyParams.getIsActive()));

    productService.saveProduct(product);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
    boolean deleted = productService.deleteProduct(id);
    if (!deleted) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}