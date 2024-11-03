package com.sellsphere.admin;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.tasks.UnsupportedFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * REST controller for handling image upload and compression.
 * This controller allows clients to upload an image, compress it, and receive the compressed image as a response.
 */
@RestController
@Slf4j
public class ImageCompressorRestController {

    /**
     * Handles file upload, compresses the image, and returns the compressed image as a response.
     * The compression involves adjusting the image quality and optionally resizing it.
     *
     * @param file    the image file to be uploaded and compressed.
     * @param width   the target width of the compressed image (optional).
     * @param height  the target height of the compressed image (optional).
     * @param quality the quality of the compressed image (between 0.0 and 1.0).
     * @return a ResponseEntity containing the compressed image in byte array format.
     * @throws IOException if an I/O error occurs during image processing.
     */
    @PostMapping("/upload")
    public ResponseEntity<byte[]> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam("quality") float quality) throws IOException {
        log.info("Received file upload request for file: {}", file.getOriginalFilename());

        // Validate the file
        if (file.isEmpty()) {
            log.error("File upload failed: File is empty");
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file size (limit 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            log.error("File upload failed: File size exceeds 5MB limit");
            throw new IllegalArgumentException("File size exceeds the maximum limit of 5MB");
        }

        // Validate image quality parameter
        if (quality < 0.0f || quality > 1.0f) {
            log.error("Invalid quality value: {}", quality);
            throw new IllegalArgumentException("Quality must be between 0.0 and 1.0");
        }

        // Validate file type (image)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("Unsupported file type: {}", contentType);
            throw new IllegalArgumentException("Unsupported file type. Please upload an image.");
        }

        log.info("Compressing image with quality: {} and optional dimensions: {}x{}", quality, width, height);

        // Compress and resize the image
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Thumbnails.Builder<?> thumbnailBuilder = Thumbnails.of(file.getInputStream())
                    .outputFormat("jpg")  // Ensure output as JPEG
                    .outputQuality(quality);

            // Resize if width and height are specified, and force the size
            if (width != null && height != null) {
                thumbnailBuilder.forceSize(width, height);
            } else {
                thumbnailBuilder.scale(1.0);  // Keep original size
            }

            thumbnailBuilder.toOutputStream(byteArrayOutputStream);
        } catch (UnsupportedFormatException e) {
            log.error("Unsupported format", e);
            throw new IllegalArgumentException("Unsupported image format.");
        } catch (IOException e) {
            log.error("Error processing image", e);
            throw new IOException("Failed to process the image file.", e);
        }

        byte[] compressedImage = byteArrayOutputStream.toByteArray();

        // Extract the original filename and replace the extension with .jpg
        String originalFilename = file.getOriginalFilename();
        String newFilename = originalFilename != null ? originalFilename.replaceAll("\\.[^.]+$", ".jpg") : "compressed_image.jpg";

        log.info("Image compression successful, returning compressed file: {}", newFilename);

        // Set the response headers for the file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + newFilename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");

        return new ResponseEntity<>(compressedImage, headers, HttpStatus.OK);
    }
}
