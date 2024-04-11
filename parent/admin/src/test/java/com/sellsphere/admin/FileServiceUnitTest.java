package com.sellsphere.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceUnitTest {

    @Test
    void removeNotMatchingFiles_ShouldDeleteNotMatchingFiles() {
        // Given
        String extrasFolderName = "test-folder";
        List<String> newFiles = List.of("test-folder/file1.txt", "test-folder/file2.txt");
        List<String> existingFiles = List.of("test-folder/file1.txt", "test-folder/file3.txt");

        try (MockedStatic<S3Utility> s3UtilityMockedStatic = mockStatic(S3Utility.class)) {
            when(S3Utility.listFolder(extrasFolderName)).thenReturn(existingFiles);

            // When
            FileService.removeNotMatchingFiles(extrasFolderName, newFiles);

            // Then
            s3UtilityMockedStatic.verify(() -> S3Utility.deleteFiles(List.of("test-folder/file3.txt")));
        }
    }
}