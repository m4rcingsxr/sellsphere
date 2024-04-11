package util;

import com.adobe.testing.s3mock.util.DigestUtil;
import lombok.experimental.UtilityClass;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UtilityClass
public class S3TestUtils {

    public static void createBucket(S3Client s3Client, String bucketName) {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    }

    public static void verifyFileContent(S3Client s3Client, String bucketName, String key, InputStream originalFileStream) throws
            IOException {

        var response = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(key).build()
        );

        String originalDigest = DigestUtil.hexDigest(originalFileStream);
        String downloadedDigest = DigestUtil.hexDigest(response);

        originalFileStream.close();
        response.close();

        assertEquals(originalDigest, downloadedDigest);
    }

    public static InputStream getFileInputStream(Path filePath) throws IOException {
        return Files.newInputStream(filePath);
    }

    private static void listObjectsInBucket(S3Client s3Client, String bucketName) {
        // List objects in the specified bucket
        List<S3Object> objects = s3Client.listObjects(ListObjectsRequest.builder().bucket(bucketName).build()).contents();

        // Print keys of the objects
        System.out.println("Objects in bucket " + bucketName + ":");
        for (S3Object object : objects) {
            System.out.println(" - " + object.key());
        }
    }
}