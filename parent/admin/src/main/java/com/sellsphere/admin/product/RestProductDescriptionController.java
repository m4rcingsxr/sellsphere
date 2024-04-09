package com.sellsphere.admin.product;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class RestProductDescriptionController {

    private static final String IMAGE_PATH = "description/images";

    private final FileService fileService;

    @PostMapping("/descriptions/upload")
    public ResponseEntity<Map<String, String>> uploadProductDescriptionImage(
            @RequestParam("file") MultipartFile image)
            throws IOException, IllegalArgumentException {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String fileName = image.getOriginalFilename();

        fileService.saveFile(image, IMAGE_PATH, fileName);

        Map<String, String> map = new HashMap<>();
        map.put("location", Constants.S3_BASE_URI + "/" + IMAGE_PATH + "/" + fileName);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/descriptions/delete")
    public ResponseEntity<Void> deleteProductDescriptionImage(@RequestBody Map<String, String> request)
            throws IllegalArgumentException {

        String fileName = request.get("fileName");
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is empty");
        }

        String s3Object = IMAGE_PATH + "/" + fileName;
        fileService.removeFile(s3Object);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                                                        HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Internal server error: " + ex.getMessage(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
