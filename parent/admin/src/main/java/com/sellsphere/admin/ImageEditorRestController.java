package com.sellsphere.admin;

import com.sellsphere.common.entity.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling product description images in the editor.
 * Provides endpoints for uploading and deleting images from the system.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class ImageEditorRestController {

    private static final String IMAGE_PATH = "editor/images";

    /**
     * Handles the upload of a product description image.
     * The image is saved to a specified location, and the URL for accessing the image is returned in the response.
     *
     * @param image The image file to be uploaded.
     * @return A ResponseEntity containing the location of the uploaded image.
     * @throws IOException             If an I/O error occurs during file upload.
     * @throws IllegalArgumentException If the provided file is null or empty.
     */
    @PostMapping("/editor/upload")
    public ResponseEntity<Map<String, String>> uploadProductDescriptionImage(
            @RequestParam("file") MultipartFile image)
            throws IOException, IllegalArgumentException {

        log.info("Received request to upload image.");

        // Validate the image file
        if (image == null || image.isEmpty()) {
            log.error("Image upload failed: file is empty or null.");
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = image.getOriginalFilename();
        log.info("Uploading image with filename: {}", fileName);

        // Save the file using FileService
        FileService.saveFile(image, IMAGE_PATH, fileName);
        log.info("Image '{}' successfully uploaded to '{}'.", fileName, IMAGE_PATH);

        // Construct the response with the image location
        Map<String, String> response = new HashMap<>();
        response.put("location", Constants.S3_BASE_URI + "/" + IMAGE_PATH + "/" + fileName);

        log.info("Image upload completed. File URL: {}", response.get("location"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Handles the deletion of a product description image.
     * The image is deleted from the storage system based on the provided file name.
     *
     * @param request A map containing the file name of the image to be deleted.
     * @return A ResponseEntity indicating the status of the deletion.
     * @throws IllegalArgumentException If the file name is null or empty.
     */
    @PostMapping("/editor/delete")
    public ResponseEntity<Void> deleteProductDescriptionImage(@RequestBody Map<String, String> request)
            throws IllegalArgumentException {

        log.info("Received request to delete image.");

        // Extract the file name from the request body
        String fileName = request.get("fileName");
        if (fileName == null || fileName.isEmpty()) {
            log.error("Image deletion failed: file name is empty or null.");
            throw new IllegalArgumentException("File name is empty");
        }

        log.info("Deleting image with filename: {}", fileName);

        // Remove the file using FileService
        String s3Object = IMAGE_PATH + "/" + fileName;
        FileService.removeFile(s3Object);
        log.info("Image '{}' successfully deleted from '{}'.", fileName, IMAGE_PATH);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
