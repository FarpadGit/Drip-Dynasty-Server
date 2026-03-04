package com.farpad.dripServer.models.clientSideData;

import com.farpad.dripServer.models.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFormData {
    private String id;
    private String name;
    private String slug;
    private String description;
    private List<String> categories;
    private String price;
    private String discount;
    private String emailMessage;
    private String isActive;
    private List<Product.Variant> variants;
    private List<Product.SearchTag> searchTags;
    @JsonIgnore
    private List<String> variantsJson = new ArrayList<>();
    @JsonIgnore
    private List<String> searchTagsJson = new ArrayList<>();
    private String defaultStock;
    private List<String> imagesToDelete;
    private List<String> imagePaths;
    private MultipartFile[] imageFiles;
    private List<String> orders;
    private String createdSince;

    @Transient
    public List<Product.Variant> _getVariants() {
	    if(this.variants != null) return this.getVariants();
        Gson gson = new Gson();
        return this.getVariantsJson().stream().map(variant -> gson.fromJson(variant, Product.Variant.class)).toList();
    }

    @Transient
    public List<Product.SearchTag> _getSearchTags() {
	    if(this.searchTags != null) return this.getSearchTags();
        Gson gson = new Gson();
        return this.getSearchTagsJson().stream().map(tag -> gson.fromJson(tag, Product.SearchTag.class)).toList();
    }
}
