package com.sellsphere.admin;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.s3.S3Client;
import util.S3TestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(S3MockExtension.class)
class S3UtilityIntegrationTest {

    private static final String BUCKET_NAME = "my-demo-test-bucket";
    private static final String UPLOAD_FILE_NAME = "src/test/resources/sampleFile.txt";
    private static S3Client s3Client;

    @BeforeAll
    static void setUpClient(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void givenValidFile_whenUpload_thenFileIsUploadedSuccessfully() throws Exception {
        // Given
        String folderName = "folder";
        String fileName = "abc.txt";
        Path uploadFilePath = Path.of(UPLOAD_FILE_NAME);

        // When
        S3Utility.uploadFile(folderName, fileName, S3TestUtils.getFileInputStream(uploadFilePath));

        // Then
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME, folderName + "/" + fileName, S3TestUtils.getFileInputStream(uploadFilePath));
    }

    @Test
    void givenNonExistentFile_whenCheckFileExistence_thenReturnsFalse() {
        // Given
        String folderName = "non-existent-folder";
        String fileName = "non-existent-file.txt";

        // When & Then
        assertFalse(S3Utility.checkFileExistsInS3(folderName, fileName));
    }

    @Test
    void givenValidFilesInFolder_whenListFolder_thenReturnsFileList() throws Exception {
        // Given
        String folderName = "test-folder";
        File uploadFile1 = new File(UPLOAD_FILE_NAME);
        File uploadFile2 = new File(UPLOAD_FILE_NAME);

        S3Utility.uploadFile(folderName, "file1.txt", new FileInputStream(uploadFile1));
        S3Utility.uploadFile(folderName, "file2.txt", new FileInputStream(uploadFile2));

        // When
        List<String> files = S3Utility.listFolder(folderName);

        // Then
        assertTrue(files.contains(folderName + "/file1.txt"));
        assertTrue(files.contains(folderName + "/file2.txt"));
    }

    @Test
    void givenFileExists_whenDeleteFile_thenFileIsDeleted() throws Exception {
        // Given
        File uploadFile = new File(UPLOAD_FILE_NAME);
        String fileName = "delete-test.txt";
        String folderName = "folder";

        S3Utility.uploadFile(folderName, fileName, new FileInputStream(uploadFile));

        // When
        S3Utility.deleteS3Object(folderName + "/" + fileName);

        // Then
        assertFalse(S3Utility.checkFileExistsInS3(folderName, fileName));
    }

    @Test
    void givenValidFiles_whenBatchDeleteFiles_thenAllFilesDeleted() throws Exception {
        // Given
        String folderName = "delete-files-test-folder";
        String fileName1 = "file1.txt";
        String fileName2 = "file2.txt";
        File uploadFile1 = new File(UPLOAD_FILE_NAME);
        File uploadFile2 = new File(UPLOAD_FILE_NAME);

        S3Utility.uploadFile(folderName, fileName1, new FileInputStream(uploadFile1));
        S3Utility.uploadFile(folderName, fileName2, new FileInputStream(uploadFile2));

        // When
        S3Utility.deleteFiles(List.of(folderName + "/" + fileName1, folderName + "/" + fileName2));

        // Then
        List<String> files = S3Utility.listFolder(folderName);
        assertFalse(files.contains(folderName + "/" + fileName1));
        assertFalse(files.contains(folderName + "/" + fileName2));
    }

    @Test
    void givenEmptyFolder_whenRemoveFolder_thenFolderIsRemovedSuccessfully() throws Exception {
        // Given
        String folderName = "remove-test-folder";
        File uploadFile1 = new File(UPLOAD_FILE_NAME);
        File uploadFile2 = new File(UPLOAD_FILE_NAME);

        S3Utility.uploadFile(folderName, "file1.txt", new FileInputStream(uploadFile1));
        S3Utility.uploadFile(folderName, "file2.txt", new FileInputStream(uploadFile2));

        // When
        S3Utility.removeFilesInFolder(folderName);

        // Then
        List<String> files = S3Utility.listFolder(folderName);
        assertTrue(files.isEmpty());
    }

    @Test
    void givenValidFolder_whenRemoveFolder_thenAllContentsAreRemoved() throws Exception {
        // Given
        String folderName = "folder-to-remove";
        S3Utility.uploadFile(folderName, "file1.txt", new FileInputStream(new File(UPLOAD_FILE_NAME)));

        // When
        S3Utility.removeFolder(folderName);

        // Then
        assertTrue(S3Utility.listFolder(folderName).isEmpty());
    }

    @Test
    void givenEmptyFile_whenUploadFile_thenFileIsNotUploaded() throws Exception {
        // Given
        String folderName = "empty-folder";
        String fileName = "empty.txt";
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        // When
        S3Utility.uploadFile(folderName, fileName, emptyStream);

        // Then
        assertFalse(S3Utility.checkFileExistsInS3(folderName, fileName));
    }
}
