package com.sellsphere.admin;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
public class ImageCompressorRestController {

    @PostMapping("/upload")
    public ResponseEntity<byte[]> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("width") int width,
            @RequestParam("height") int height,
            @RequestParam("quality") float quality) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        if (file.getSize() > 5 * 1024 * 1024) { // Check if file is larger than 5MB
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(null);
        }

        if (quality < 0.0f || quality > 1.0f) { // Check if quality is within the valid range
            return ResponseEntity.badRequest().body(null);
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Resize and compress the image to the provided width, height, and quality
            Thumbnails.of(file.getInputStream())
                    .size(width, height)
                    .outputFormat("jpg")
                    .outputQuality(quality) // Adjust the output quality as provided
                    .toOutputStream(byteArrayOutputStream);

            byte[] compressedImage = byteArrayOutputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed_image.jpg");
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");

            return new ResponseEntity<>(compressedImage, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
