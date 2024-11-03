package com.sellsphere.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ImageCompressorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockMultipartFile validImageFile;
    private byte[] originalImageData;

    @BeforeEach
    void setup() throws IOException {
        BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        originalImageData = baos.toByteArray();

        validImageFile = new MockMultipartFile(
                "file", "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE, originalImageData
        );
    }

    @Test
    void givenValidImage_whenCompress_thenReturnsCompressedImage() throws Exception {
        byte[] compressedImageData = mockMvc.perform(multipart("/upload")
                                                             .file(validImageFile)
                                                             .param("quality", "0.8"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                           "attachment; filename=\"test-image.jpg\""
                ))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedImageData)) {
            BufferedImage compressedImage = ImageIO.read(bais);
            assertNotNull(compressedImage, "Compressed image should not be null");
        }
    }

    @Test
    void givenEmptyFile_whenCompress_thenReturnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", MediaType.IMAGE_JPEG_VALUE,
                                                            new byte[0]
        );

        mockMvc.perform(multipart("/upload")
                                .file(emptyFile)
                                .param("quality", "0.8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenLargeFile_whenCompress_thenReturnsBadRequest() throws Exception {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB file
        MockMultipartFile largeFile = new MockMultipartFile("file", "large-file.jpg",
                                                            MediaType.IMAGE_JPEG_VALUE, largeContent
        );

        mockMvc.perform(multipart("/upload")
                                .file(largeFile)
                                .param("quality", "0.8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidQuality_whenCompress_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/upload")
                                .file(validImageFile)
                                .param("quality", "1.2"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(multipart("/upload")
                                .file(validImageFile)
                                .param("quality", "-0.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidFileType_whenCompress_thenReturnsBadRequest() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "file.txt",
                                                              MediaType.TEXT_PLAIN_VALUE,
                                                              "some text".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                                .file(invalidFile)
                                .param("quality", "0.8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidImageWithDimensions_whenCompress_thenResizesImage() throws Exception {
        byte[] compressedImageData = mockMvc.perform(multipart("/upload")
                                                             .file(validImageFile)
                                                             .param("quality", "0.8")
                                                             .param("width", "300")
                                                             .param("height", "200"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                           "attachment; filename=\"test-image.jpg\""
                ))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();


        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedImageData)) {
            BufferedImage compressedImage = ImageIO.read(bais);
            assertNotNull(compressedImage, "Compressed image should not be null");
            assertEquals(300, compressedImage.getWidth(), "Width should be 300");
            assertEquals(200, compressedImage.getHeight(), "Height should be 200");
        }

        System.out.println("Original image size: " + originalImageData.length);
        System.out.println("Compressed image size: " + compressedImageData.length);

        assertTrue(compressedImageData.length < originalImageData.length,
                   "Compressed image should be smaller in size than the original"
        );
    }
}
