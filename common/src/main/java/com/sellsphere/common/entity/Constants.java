package com.sellsphere.common.entity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SORT_ASCENDING = "asc";
    public static final String SORT_DESCENDING = "desc";

    public static final String S3_BASE_URI;

    static {
        String bucketName = System.getenv("AWS_BUCKET_NAME");
        String region = System.getenv("AWS_REGION");
        String pattern = "https://%s.s3.%s.amazonaws.com";

        if (bucketName == null || region == null) throw new IllegalStateException(
                "AWS_BUCKET_NAME and AWS_REGION environment variables must be set");

        S3_BASE_URI = String.format(pattern, bucketName, region);
    }

}
