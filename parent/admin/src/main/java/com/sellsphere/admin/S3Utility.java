package com.sellsphere.admin;

import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for Amazon S3 operations such as listing folders,
 * uploading files, and deleting files or folders.
 */
@UtilityClass
@Log4j2
public class S3Utility {

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());

    @Setter
    private static String bucketName;

    @Setter
    private static S3Client s3Client;

    private static final String REGION;


    static {
        bucketName = System.getenv("AWS_BUCKET_NAME");
        REGION = System.getenv("AWS_REGION");
        s3Client = createS3Client();
    }

    /**
     * Lists all objects in a specified folder within the S3 bucket.
     *
     * @param folderName the name of the folder
     * @return a list of keys (file paths) within the folder
     */
    public static List<String> listFolder(String folderName) throws S3Exception {
        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(folderName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(listRequest);
        return response.contents()
                .stream()
                .map(S3Object::key)
                .toList();
    }

    /**
     * Uploads a file to the specified folder in the S3 bucket.
     *
     * @param folderName  the folder within the bucket
     * @param fileName    the name of the file to upload
     * @param inputStream the content of the file
     */
    public static void uploadFile(String folderName, String fileName,
                                  InputStream inputStream) throws IOException, S3Exception {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folderName + "/" + fileName)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream,
                                                                inputStream.available()
        ));
    }

    /**
     * Uploads files in parallel to a specified directory on S3 to improve
     * performance.
     *
     * @param extrasFolderName the directory name on S3
     * @param files            list of files to upload
     */
    public static void uploadFiles(String extrasFolderName,
                                   List<MultipartFile> files) throws IOException, S3Exception {
        List<CompletableFuture<Void>> futures = files.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> CompletableFuture.runAsync(() -> {
                    try {
                        String fileName = file.getOriginalFilename();
                        InputStream inputStream = file.getInputStream();
                        uploadFile(extrasFolderName, fileName, inputStream);
                    } catch (IOException | S3Exception e) {
                        log.error("Failed to upload file: {}",
                                  file.getOriginalFilename(), e
                        );
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toList();

        // Wait for all to complete
        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])).join();
    }


    /**
     * Removes files from a folder in the S3 bucket.
     *
     * @param folderName the folder from which to remove files
     * @throws S3Exception if an error occurs while communicating with S3
     */
    public static void removeFilesInFolder(String folderName) throws S3Exception {
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
     * Removes object from S3 bucket.
     *
     * @param object the object to remove
     */
    public static void deleteS3Object(String object) throws S3Exception {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(object)
                .build();

        s3Client.deleteObject(request);
    }

    /**
     * Deletes a list of files from the specified S3 bucket in a batch operation
     * using a consumer builder for enhanced readability and fluency in creating
     * request objects.
     *
     * @param filesToDelete The list of keys (S3 object paths) to be deleted.
     */
    public static void deleteFiles(List<String> filesToDelete) throws S3Exception {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            log.warn("No files specified for deletion.");
            return;
        }

        // Transform the list of file keys into a list of ObjectIdentifier
        List<ObjectIdentifier> identifiers = filesToDelete.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        // Execute the batch delete operation using consumer builder
        s3Client.deleteObjects(builder -> builder.bucket(bucketName)
                .delete(delBuilder -> delBuilder.objects(identifiers)));

        log.info("Successfully deleted {} files from bucket '{}'.",
                 filesToDelete.size(), bucketName
        );
    }

    /**
     * Checks if a file exists in the specified folder within the S3 bucket.
     *
     * @param folderName the name of the folder
     * @param fileName   the name of the file
     * @return true if the file exists, false otherwise
     * @throws S3Exception if an error occurs while checking the file existence
     */
    public static boolean checkFileExistsInS3(String folderName,
                                              String fileName) throws S3Exception {
        try {
            // Build the request to head the object
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderName + "/" + fileName)
                    .build();

            // Issue the head request. If the object exists, this will succeed.
            s3Client.headObject(headObjectRequest);
            return true; // Object exists
        } catch (NoSuchKeyException e) {
            // Object does not exist
            return false;
        }
    }

    /**
     * Creates and configures an S3 client with a region and credentials.
     *
     * @return configured S3Client
     */
    public static S3Client createS3Client() {
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
        String folderPrefix = dirName.endsWith("/") ? dirName : dirName + "/";

        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(folderPrefix)
                .build();

        ListObjectsResponse listResponse = s3Client.listObjects(listRequest);

        // Collect all keys to delete
        List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                .toList();

        if (!objectsToDelete.isEmpty()) {
            // Delete all objects in a single batch request
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            s3Client.deleteObjects(deleteRequest);
        }

        log.info("Successfully deleted folder '{}' and its contents from bucket '{}'.", folderPrefix, bucketName);
    }
}