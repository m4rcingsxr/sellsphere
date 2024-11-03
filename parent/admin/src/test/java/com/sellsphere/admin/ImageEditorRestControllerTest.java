package com.sellsphere.admin;

import com.sellsphere.common.entity.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class ImageEditorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockMultipartFile validImage;

    @BeforeEach
    void setUp() {
        validImage = new MockMultipartFile("file", "test-image.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    void givenValidImage_whenUploadProductDescriptionImage_thenReturnSuccess() throws Exception {
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // Given
            mockedFileService.when(() -> FileService.saveFile(any(MultipartFile.class), anyString(), anyString()))
                    .thenAnswer(invocation -> null);

            // When & Then
            mockMvc.perform(multipart("/editor/upload")
                                    .file(validImage)
                                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.location", is(Constants.S3_BASE_URI + "/editor/images/test-image.jpg")));

            mockedFileService.verify(() -> FileService.saveFile(validImage, "editor/images", "test-image.jpg"));
        }
    }

    @Test
    void givenEmptyFile_whenUploadProductDescriptionImage_thenThrowIllegalArgumentException() throws Exception {
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[]{});

            // When & Then
            mockMvc.perform(multipart("/editor/upload")
                                    .file(emptyFile)
                                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());

            mockedFileService.verifyNoInteractions();
        }
    }

    @Test
    void givenValidRequest_whenDeleteProductDescriptionImage_thenReturnSuccess() throws Exception {
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // Given
            Map<String, String> request = new HashMap<>();
            request.put("fileName", "test-image.jpg");
            mockedFileService.when(() -> FileService.removeFile("editor/images/test-image.jpg")).thenAnswer(invocation -> null);

            // When & Then
            mockMvc.perform(post("/editor/delete")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"fileName\": \"test-image.jpg\"}"))
                    .andExpect(status().isOk());

            mockedFileService.verify(() -> FileService.removeFile("editor/images/test-image.jpg"));
        }
    }

    @Test
    void givenNullFileName_whenDeleteProductDescriptionImage_thenThrowIllegalArgumentException() throws Exception {
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // Given
            Map<String, String> request = new HashMap<>();
            request.put("fileName", null);

            // When & Then
            mockMvc.perform(post("/editor/delete")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"fileName\": null}"))
                    .andExpect(status().isBadRequest());

            mockedFileService.verifyNoInteractions();
        }
    }

    @Test
    void givenEmptyFileName_whenDeleteProductDescriptionImage_thenThrowIllegalArgumentException() throws Exception {
        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // Given
            Map<String, String> request = new HashMap<>();
            request.put("fileName", "");

            // When & Then
            mockMvc.perform(post("/editor/delete")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"fileName\": \"\"}"))
                    .andExpect(status().isBadRequest());

            mockedFileService.verifyNoInteractions();
        }
    }
}
