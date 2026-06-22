package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.Product;
import lombok.AllArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/images")
public class ImageController {
    @GetMapping("/products/{id}/{filename}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String id, @PathVariable String filename){
        try {
            Path filePath = Paths.get(Product.imageUploadDirectory, id, filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<String> fileNameParts = Arrays.asList(filename.split("\\."));
            String extension = fileNameParts.get(fileNameParts.size() - 1);
            MediaType mediaType = extension.equalsIgnoreCase("PNG") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (MalformedURLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/thumbnail/{id}/{filename}")
    public ResponseEntity<BufferedImage> getProductThumbnail(
            @PathVariable String id,
            @PathVariable String filename,
            @RequestParam(defaultValue = "200") String w,
            @RequestParam(defaultValue = "200") String h
    ) {
        try {
            int width = Integer.parseInt(w);
            int height = Integer.parseInt(h);
            Path filePath = Paths.get(Product.imageUploadDirectory, id, filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            BufferedImage thumbnail = Thumbnails.of(resource.getInputStream())
                    .size(width, height)
                    .asBufferedImage();

            List<String> fileNameParts = Arrays.asList(filename.split("\\."));
            String extension = fileNameParts.get(fileNameParts.size() - 1);
            MediaType mediaType = extension.equalsIgnoreCase("PNG") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(thumbnail);

        } catch (IOException | NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/collections/{filename}")
    public ResponseEntity<Resource> getCollectionImage(@PathVariable String filename) {
        try {
            Path folderPath = Paths.get("src/main/resources/static/images/collections");
            Path filePath = folderPath.resolve(filename);
            Resource image = new UrlResource(filePath.toUri());

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } catch (MalformedURLException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}