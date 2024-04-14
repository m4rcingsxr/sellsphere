package com.sellsphere.admin.product;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Constants;
import lombok.RequiredArgsConstructor;
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
 * REST controller for handling product description images.
 */
@RequiredArgsConstructor
@RestController
public class RestProductDescriptionController {

    private static final String IMAGE_PATH = "description/images";

    /**
     * Uploads a product description image.
     *
     * @param image The image file to be uploaded.
     * @return A ResponseEntity containing the location of the uploaded image.
     * @throws IOException             If an I/O error occurs during file upload.
     * @throws IllegalArgumentException If the provided file is empty.
     */
    @PostMapping("/descriptions/upload")
    public ResponseEntity<Map<String, String>> uploadProductDescriptionImage(
            @RequestParam("file") MultipartFile image)
            throws IOException, IllegalArgumentException {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String fileName = image.getOriginalFilename();

        FileService.saveFile(image, IMAGE_PATH, fileName);

        Map<String, String> map = new HashMap<>();
        map.put("location", Constants.S3_BASE_URI + "/" + IMAGE_PATH + "/" + fileName);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    /**
     * Deletes a product description image.
     *
     * @param request A map containing the file name of the image to be deleted.
     * @return A ResponseEntity indicating the status of the deletion.
     * @throws IllegalArgumentException If the file name is empty.
     */
    @PostMapping("/descriptions/delete")
    public ResponseEntity<Void> deleteProductDescriptionImage(@RequestBody Map<String, String> request)
            throws IllegalArgumentException {

        String fileName = request.get("fileName");
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is empty");
        }

        String s3Object = IMAGE_PATH + "/" + fileName;
        FileService.removeFile(s3Object);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
