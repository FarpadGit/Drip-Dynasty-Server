package com.farpad.dripServer.models.clientSideData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFormData {
    private String id;
    private String name;
    private String description;
    private List<String> categories;
    private String price;
    private String discount;
    private String extra;
    private String isActive;
    private List<String> imagesToDelete;
    private List<String> imagePaths;
    private MultipartFile[] imageFiles;
    private List<String> orders;
    private String createdSince;
}

