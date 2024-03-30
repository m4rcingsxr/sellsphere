package com.sellsphere.admin;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
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
    void shouldUploadAndDownloadObject() throws Exception {
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
    void shouldListFolderContents() throws Exception {
        // given
        String folderName = "test-folder";
        File uploadFile1 = new File(UPLOAD_FILE_NAME);
        File uploadFile2 = new File(UPLOAD_FILE_NAME);
        S3Utility.uploadFile(folderName, "file1.txt", new FileInputStream(uploadFile1));
        S3Utility.uploadFile(folderName, "file2.txt", new FileInputStream(uploadFile2));

        // when
        List<String> files = S3Utility.listFolder(folderName);

        // then
        assertTrue(files.contains(folderName + "/file1.txt"));
        assertTrue(files.contains(folderName + "/file2.txt"));
    }

    @Test
    void shouldDeleteFile() throws Exception {
        // given
        File uploadFile = new File(UPLOAD_FILE_NAME);
        String fileName = "delete-test.txt";
        String folderName = "folder";
        S3Utility.uploadFile(folderName, fileName, new FileInputStream(uploadFile));

        // when
        S3Utility.deleteS3Object(folderName + "/" + fileName);

        // then
        assertFalse(S3Utility.checkFileExistsInS3(folderName, fileName));
    }

    @Test
    void shouldCheckFileExists() throws Exception {
        // given
        File uploadFile = new File(UPLOAD_FILE_NAME);
        String fileName = "exist-test.txt";
        S3Utility.uploadFile("", fileName, new FileInputStream(uploadFile));

        // then
        assertTrue(S3Utility.checkFileExistsInS3("", fileName));
        assertFalse(S3Utility.checkFileExistsInS3("", "non-existent-file.txt"));
    }

    @Test
    void shouldUploadMultipleFiles() throws Exception {
        // given
        String folderName = "multipart-test-folder";
        String fileName1 = "multipart-test-file1.txt";
        String fileName2 = "multipart-test-file2.txt";
        InputStream inputStream1 = new ByteArrayInputStream("Content of file 1".getBytes());
        InputStream inputStream2 = new ByteArrayInputStream("Content of file 2".getBytes());

        MockMultipartFile multipartFile1 = new MockMultipartFile("file", fileName1, "text/plain", inputStream1);
        MockMultipartFile multipartFile2 = new MockMultipartFile("file", fileName2, "text/plain", inputStream2);

        // when
        S3Utility.uploadFiles(folderName, List.of(multipartFile1, multipartFile2));

        // then
        List<String> files = S3Utility.listFolder(folderName);
        assertTrue(files.contains(folderName + "/" + fileName1));
        assertTrue(files.contains(folderName + "/" + fileName2));
    }

    @Test
    void shouldRemoveFolder() throws Exception {
        // given
        String folderName = "remove-test-folder";
        File uploadFile1 = new File(UPLOAD_FILE_NAME);
        File uploadFile2 = new File(UPLOAD_FILE_NAME);
        S3Utility.uploadFile(folderName, "file1.txt", new FileInputStream(uploadFile1));
        S3Utility.uploadFile(folderName, "file2.txt", new FileInputStream(uploadFile2));

        // when
        S3Utility.removeFolder(folderName);

        // then
        List<String> files = S3Utility.listFolder(folderName);
        assertTrue(files.isEmpty());
    }

    @Test
    void shouldDeleteFiles() throws Exception {
        // given
        String folderName = "delete-files-test-folder";
        String fileName1 = "file1.txt";
        String fileName2 = "file2.txt";
        File uploadFile1 = new File(UPLOAD_FILE_NAME);
        File uploadFile2 = new File(UPLOAD_FILE_NAME);
        S3Utility.uploadFile(folderName, fileName1, new FileInputStream(uploadFile1));
        S3Utility.uploadFile(folderName, fileName2, new FileInputStream(uploadFile2));

        // when
        S3Utility.deleteFiles(List.of(folderName + "/" + fileName1, folderName + "/" + fileName2));

        // then
        List<String> files = S3Utility.listFolder(folderName);
        assertFalse(files.contains(folderName + "/" + fileName1));
        assertFalse(files.contains(folderName + "/" + fileName2));
    }

}