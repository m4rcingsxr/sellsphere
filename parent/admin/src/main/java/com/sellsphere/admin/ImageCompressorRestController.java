package com.sellsphere.admin;

import com.sellsphere.common.entity.ErrorResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * REST controller for handling image upload and compression.
 */
@RestController
public class ImageCompressorRestController {

    /**
     * Handles file upload, compresses the image, and returns the compressed image as a response.
     *
     * @param file the image file to be uploaded
     * @param width the target width of the compressed image
     * @param height the target height of the compressed image
     * @param quality the quality of the compressed image (between 0.0 and 1.0)
     * @return ResponseEntity containing the compressed image
     * @throws IOException if an I/O error occurs during image processing
     */
    @PostMapping("/upload")
    public ResponseEntity<byte[]> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam("quality") float quality) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) { // Check if file is larger than 5MB
            throw new IllegalArgumentException("File size exceeds the maximum limit of 5MB");
        }

        if (quality < 0.0f || quality > 1.0f) { // Check if quality is within the valid range
            throw new IllegalArgumentException("Quality must be between 0.0 and 1.0");
        }

        // Check if the file is an image
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Unsupported file type. Please upload an image.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            Thumbnails.Builder<?> thumbnailBuilder = Thumbnails.of(file.getInputStream())
                    .outputFormat("jpg") // Output format will be JPG for consistency
                    .outputQuality(quality); // Adjust the output quality as provided

            // Apply resizing only if width and height are provided
            if (width != null && height != null) {
                thumbnailBuilder.size(width, height);
            } else {
                thumbnailBuilder.scale(1.0);
            }

            thumbnailBuilder.toOutputStream(byteArrayOutputStream);
        } catch (IOException e) {
            throw new IOException("Failed to process the image file.", e);
        }

        byte[] compressedImage = byteArrayOutputStream.toByteArray();

        // Extract the original filename and change its extension to .jpg
        String originalFilename = file.getOriginalFilename();
        String newFilename = originalFilename != null ? originalFilename.replaceAll("\\.[^.]+$", ".jpg") : "compressed_image.jpg";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + newFilename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");

        return new ResponseEntity<>(compressedImage, headers, HttpStatus.OK);
    }


}
