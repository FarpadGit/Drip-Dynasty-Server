package com.farpad.dripServer.services;

import com.farpad.dripServer.models.Product;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    public void loadStarterImages() {
        try {
            Path srcDirectoryPath = Path.of("src/main/resources/static/dbseed/images");
            Path destDirectoryPath = Path.of("src/main/resources/static/images/products");
            FileUtils.copyDirectory(srcDirectoryPath.toFile(), destDirectoryPath.toFile());
        } catch (IOException ignored) {
        }
    }

    // Save image to local directory
    public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile) {
        try {
            String safeFileName = imageFile.getOriginalFilename().replace(' ', '_');
            String uniqueFileName = UUID.randomUUID() + "_" + safeFileName;

            Path directoryPath = Path.of(uploadDirectory);
            Path filePath = directoryPath.resolve(uniqueFileName);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return Product.imageUploadDirectoryAccessPath + directoryPath.getFileName().toString() + "/" + uniqueFileName;
        } catch (IOException | NullPointerException e) {
            return "#Invalid";
        }
    }

    // Delete image
    public void deleteImageFromStorage(String imageDirectory, String imageName) {
        try {
            Path imagePath = Paths.get(imageDirectory, imageName);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                Files.delete(imagePath);
            }
        } catch (IOException ignored) {
        }
    }

    // Delete folder
    public void deleteFolderFromStorage(String imageDirectory) {
        try {
            Path directoryPath = Path.of(imageDirectory);
            Resource resource = new UrlResource(directoryPath.toUri());

            if (resource.exists()) {
                Files.delete(directoryPath);
            }
        } catch (IOException ignored) {
        }
    }

    public void deleteAllImages() {
        try {
            Path directoryPath = Path.of("src/main/resources/static/images/products");
            java.io.File imagesDirectory = new UrlResource(directoryPath.toUri()).getFile();
            FileUtils.cleanDirectory(imagesDirectory);
        } catch (IOException ignored) {
        }
    }
}
