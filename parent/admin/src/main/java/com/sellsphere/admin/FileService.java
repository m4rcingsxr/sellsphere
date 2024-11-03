package com.sellsphere.admin;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * A utility class that provides services to handle file operations with AWS S3.
 * This class handles uploading, deleting, and listing files on S3, ensuring efficient
 * operations like batch deletions and file removals that match specific criteria.
 *
 * <p>This class makes use of the {@code S3Utility} class for S3-related operations.</p>
 *
 * <p>Operations provided:</p>
 * <ul>
 *   <li>Uploading a single file to S3, with an option to remove existing files in a folder before upload.</li>
 *   <li>Batch removal of files in an S3 directory based on a provided list of files that should be kept.</li>
 *   <li>Deletion of individual files from S3.</li>
 * </ul>
 */
@UtilityClass
public class FileService {

    /**
     * Saves a single file to a specified directory on S3. Before uploading the new file,
     * it removes any existing files in the folder to ensure the directory only contains
     * the most recent file.
     *
     * @param file     the {@code MultipartFile} to be saved on S3
     * @param dirName  the name of the directory (folder) on S3 where the file will be stored
     * @param fileName the name under which the file will be saved on S3
     * @throws IOException if an I/O error occurs during file upload or file reading
     */
    public static void saveSingleFile(MultipartFile file, String dirName, String fileName)
            throws IOException {
        S3Utility.removeFilesInFolder(dirName);
        S3Utility.uploadFile(dirName, fileName, file.getInputStream());
    }

    /**
     * Saves a file to a specified directory on S3 without removing any existing files.
     * This is useful for adding additional files to a folder without disrupting
     * its current contents.
     *
     * @param file     the {@code MultipartFile} to be uploaded to S3
     * @param dirName  the name of the directory (folder) on S3 where the file will be stored
     * @param fileName the name under which the file will be saved on S3
     * @throws IOException if an I/O error occurs during file upload or file reading
     */
    public static void saveFile(MultipartFile file, String dirName, String fileName)
            throws IOException {
        S3Utility.uploadFile(dirName, fileName, file.getInputStream());
    }

    /**
     * Removes all files from the specified S3 folder that do not match the provided list of new files.
     * This operation optimizes performance by performing deletions in batches.
     *
     * @param extrasFolderName the name of the S3 directory (folder) where files will be checked
     * @param newFiles         a list of file names that should be kept in the directory; any other files will be deleted
     */
    public static void removeNotMatchingFiles(String extrasFolderName, List<String> newFiles) {
        List<String> existingFiles = S3Utility.listFolder(extrasFolderName);

        // Identify files that are not in the newFiles list
        List<String> filesToDelete = existingFiles.stream()
                .filter(existingFile -> !newFiles.contains(existingFile))
                .toList();

        // Delete files in batch
        S3Utility.deleteFiles(filesToDelete);
    }

    /**
     * Removes a specific file from S3 by its object key (full path or identifier).
     *
     * @param object the key (file path or identifier) of the file to be deleted from S3
     */
    public static void removeFile(String object) {
        S3Utility.deleteS3Object(object);
    }

}
