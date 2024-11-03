package com.sellsphere.admin;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class FileServiceUnitTest {

    @Test
    void givenValidFile_whenSaveSingleFile_thenRemoveOldFilesAndSaveNew() throws IOException {
        try (MockedStatic<S3Utility> s3UtilityMock = Mockito.mockStatic(S3Utility.class)) {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            Mockito.when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));

            // When
            FileService.saveSingleFile(mockFile, "test-dir", "test-file");

            // Then
            s3UtilityMock.verify(() -> S3Utility.removeFilesInFolder("test-dir"), times(1));
            s3UtilityMock.verify(() -> S3Utility.uploadFile("test-dir", "test-file", mockFile.getInputStream()), times(1));
        }
    }

    @Test
    void givenValidFile_whenSaveFile_thenSaveToS3() throws IOException {
        try (MockedStatic<S3Utility> s3UtilityMock = Mockito.mockStatic(S3Utility.class)) {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            Mockito.when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));

            // When
            FileService.saveFile(mockFile, "test-dir", "test-file");

            // Then
            s3UtilityMock.verify(() -> S3Utility.uploadFile("test-dir", "test-file", mockFile.getInputStream()), times(1));
        }
    }

    @Test
    void givenNewFilesList_whenRemoveNotMatchingFiles_thenRemoveOldFilesFromS3() {
        try (MockedStatic<S3Utility> s3UtilityMock = Mockito.mockStatic(S3Utility.class)) {
            // Given
            List<String> existingFiles = List.of("file1", "file2", "file3");
            Mockito.when(S3Utility.listFolder("test-dir")).thenReturn(existingFiles);

            // When
            FileService.removeNotMatchingFiles("test-dir", List.of("file1", "file3"));

            // Then
            s3UtilityMock.verify(() -> S3Utility.deleteFiles(List.of("file2")), times(1));
        }
    }

    @Test
    void givenS3Object_whenRemoveFile_thenDeleteObjectFromS3() {
        try (MockedStatic<S3Utility> s3UtilityMock = Mockito.mockStatic(S3Utility.class)) {
            // When
            FileService.removeFile("test-object");

            // Then
            s3UtilityMock.verify(() -> S3Utility.deleteS3Object("test-object"), times(1));
        }
    }
}
