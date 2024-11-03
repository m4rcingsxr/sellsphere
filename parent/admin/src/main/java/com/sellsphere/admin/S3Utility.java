package com.sellsphere.admin;

import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Utility class for Amazon S3 operations such as listing folders, uploading files,
 * and deleting files or folders. This class uses the AWS SDK for Java to interact
 * with S3 and provides methods to simplify common tasks.
 */
@UtilityClass
@Slf4j
public class S3Utility {

    @Setter
    private static String bucketName;

    @Setter
    private static S3Client s3Client;

    private static final String REGION;

    static {
        bucketName = System.getenv("AWS_BUCKET_NAME");
        REGION = System.getenv("AWS_REGION");
        s3Client = createS3Client();
        log.info("S3 Client initialized with bucket: {} in region: {}", bucketName, REGION);
    }

    /**
     * Lists all objects in a specified folder within the S3 bucket.
     *
     * @param folderName the name of the folder
     * @return a list of keys (file paths) within the folder
     * @throws S3Exception if an error occurs while communicating with S3
     */
    public static List<String> listFolder(String folderName) throws S3Exception {
        log.info("Listing objects in folder: {} in bucket: {}", folderName, bucketName);

        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(folderName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(listRequest);
        List<String> objectKeys = response.contents()
                .stream()
                .map(S3Object::key)
                .toList();

        log.info("Listed {} objects in folder: {}", objectKeys.size(), folderName);
        return objectKeys;
    }

    /**
     * Uploads a file to the specified folder in the S3 bucket.
     *
     * @param folderName  the folder within the bucket
     * @param fileName    the name of the file to upload
     * @param inputStream the content of the file
     * @throws IOException  if an I/O error occurs
     * @throws S3Exception  if an error occurs while communicating with S3
     */
    public static void uploadFile(String folderName, String fileName, InputStream inputStream) throws IOException, S3Exception {
        log.info("Uploading file: {} to folder: {} in bucket: {}", fileName, folderName, bucketName);

        if(inputStream.available() == 0) {
            log.info("Cannot upload file because it is empty");
            return;
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folderName + "/" + fileName)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));

        log.info("File: {} successfully uploaded to folder: {}", fileName, folderName);
    }

    /**
     * Removes files from a folder in the S3 bucket.
     *
     * @param folderName the folder from which to remove files
     * @throws S3Exception if an error occurs while communicating with S3
     */
    public static void removeFilesInFolder(String folderName) throws S3Exception {
        log.info("Removing files from folder: {} in bucket: {}", folderName, bucketName);

        String dirName = folderName.endsWith("/") ? folderName : folderName + "/";

        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(dirName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(request);
        response.contents().forEach(s3Object -> {
            if (isFileInFolder(dirName, s3Object.key())) {
                deleteS3Object(s3Object.key());
            }
        });

        log.info("Files in folder: {} have been removed", folderName);
    }

    /**
     * Checks if the given key belongs to a file in the specified folder.
     *
     * @param folderName the folder name
     * @param key        the S3 object key
     * @return true if the key is a file in the folder, false otherwise
     */
    private static boolean isFileInFolder(String folderName, String key) {
        String relativePath = key.substring(folderName.length());
        return !relativePath.contains("/");
    }

    /**
     * Removes an object from the S3 bucket.
     *
     * @param object the object to remove
     * @throws S3Exception if an error occurs while communicating with S3
     */
    public static void deleteS3Object(String object) throws S3Exception {
        log.info("Deleting object: {} from bucket: {}", object, bucketName);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(object)
                .build();

        s3Client.deleteObject(request);

        log.info("Object: {} successfully deleted from bucket: {}", object, bucketName);
    }

    /**
     * Deletes a list of files from the specified S3 bucket in a batch operation using a consumer builder.
     *
     * @param filesToDelete The list of keys (S3 object paths) to be deleted.
     * @throws S3Exception if an error occurs while communicating with S3
     */
    public static void deleteFiles(List<String> filesToDelete) throws S3Exception {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            log.warn("No files specified for deletion.");
            return;
        }

        log.info("Deleting {} files from bucket: {}", filesToDelete.size(), bucketName);

        List<ObjectIdentifier> identifiers = filesToDelete.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        s3Client.deleteObjects(builder -> builder.bucket(bucketName)
                .delete(delBuilder -> delBuilder.objects(identifiers)));

        log.info("Successfully deleted {} files from bucket: {}", filesToDelete.size(), bucketName);
    }

    /**
     * Checks if a file exists in the specified folder within the S3 bucket.
     *
     * @param folderName the name of the folder
     * @param fileName   the name of the file
     * @return true if the file exists, false otherwise
     * @throws S3Exception if an error occurs while checking the file existence
     */
    public static boolean checkFileExistsInS3(String folderName, String fileName) throws S3Exception {
        log.info("Checking if file: {} exists in folder: {} of bucket: {}", fileName, folderName, bucketName);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderName + "/" + fileName)
                    .build();

            s3Client.headObject(headObjectRequest);
            log.info("File: {} exists in folder: {}", fileName, folderName);
            return true;
        } catch (NoSuchKeyException e) {
            log.warn("File: {} does not exist in folder: {}", fileName, folderName);
            return false;
        }
    }

    /**
     * Creates and configures an S3 client with a region and credentials.
     *
     * @return configured S3Client
     */
    public static S3Client createS3Client() {
        log.info("Creating S3 client for region: {}", REGION);
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(REGION))
                .build();
    }

    /**
     * Removes a folder and its contents from the S3 bucket.
     *
     * @param dirName the directory name to remove
     * @throws S3Exception if an error occurs while communicating with S3
     */
    public static void removeFolder(String dirName) throws S3Exception {
        log.info("Removing folder: {} and its contents from bucket: {}", dirName, bucketName);

        String folderPrefix = dirName.endsWith("/") ? dirName : dirName + "/";

        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(folderPrefix)
                .build();

        ListObjectsResponse listResponse = s3Client.listObjects(listRequest);

        List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                .toList();

        if (!objectsToDelete.isEmpty()) {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            s3Client.deleteObjects(deleteRequest);
        }

        log.info("Successfully deleted folder: {} and its contents from bucket: {}", folderPrefix, bucketName);
    }
}
