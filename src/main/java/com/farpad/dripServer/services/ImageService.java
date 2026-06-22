package com.farpad.dripServer.services;

import com.farpad.dripServer.models.Product;
import net.coobird.thumbnailator.Thumbnails;
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
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    public void loadStarterImages() {
        try {
            Path srcDirectoryPath = Path.of("src/main/resources/static/dbseed/images");
            Path destDirectoryPath = Path.of("src/main/resources/static/images/products");
            java.io.File imagesDirectory = new UrlResource(destDirectoryPath.toUri()).getFile();
            FileUtils.cleanDirectory(imagesDirectory);
            FileUtils.copyDirectory(srcDirectoryPath.toFile(), destDirectoryPath.toFile());

            Path srcDirectoryPath2 = Path.of("src/main/resources/static/dbseed/thumbnails");
            Path destDirectoryPath2 = Path.of("src/main/resources/static/images/thumbnail");
            java.io.File thumbnailDirectory = new UrlResource(destDirectoryPath2.toUri()).getFile();
            FileUtils.cleanDirectory(thumbnailDirectory);
            FileUtils.copyDirectory(srcDirectoryPath2.toFile(), destDirectoryPath2.toFile());
        } catch (IOException ignored) {
        }
    }

    // Save image to local directory
    public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile) {
        try {
            String safeFileName = imageFile.getOriginalFilename();
            if(safeFileName == null) safeFileName = "";
            else safeFileName = safeFileName.replace(' ', '_');
            String uniqueFileName = UUID.randomUUID() + "_" + safeFileName;

            Path directoryPath = Path.of(uploadDirectory);
            Path filePath = directoryPath.resolve(uniqueFileName);

            if (!Files.exists(directoryPath)) Files.createDirectories(directoryPath);

            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            saveThumbnailToStorage(
                    Product.thumbnailUploadDirectory + directoryPath.getFileName().toString() + "/",
                    uniqueFileName,
                    uploadDirectory  + "/" + uniqueFileName);

            return Product.imageRestEndpointPath + directoryPath.getFileName().toString() + "/" + uniqueFileName;
        } catch (IOException | NullPointerException e) {
            return "#Invalid";
        }
    }

    // Create thumbnail and save to local directory
    public void saveThumbnailToStorage(String filePath, String filename, String baseImagePath) {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) Files.createDirectories(path);
            Thumbnails.of(baseImagePath)
                    .size(280, 280)
                    .toFile(filePath + filename);

        } catch (IOException | NullPointerException ignored) {
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
            List.of("src/main/resources/static/images/products", "src/main/resources/static/images/thumbnail")
                .forEach(dir -> {
                    try {
                        Path directoryPath = Path.of(dir);
                        java.io.File imagesDirectory = new UrlResource(directoryPath.toUri()).getFile();
                        FileUtils.cleanDirectory(imagesDirectory);
                    } catch (IOException ignored) {
                    }
                });
    }
}
