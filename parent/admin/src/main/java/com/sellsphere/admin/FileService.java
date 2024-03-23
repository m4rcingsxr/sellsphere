package com.sellsphere.admin;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileService {

    /**
     * Saves a single file to a specified directory on S3.
     *
     * @param file     the file to save
     * @param dirName  the directory name on S3
     * @param fileName the name of the file on S3
     * @throws IOException if an I/O error occurs
     */
    public void saveSingleFile(MultipartFile file, String dirName, String fileName)
            throws IOException {
        S3Utility.removeFolder(dirName);
        S3Utility.uploadFile(dirName, fileName, file.getInputStream());
    }

    public void saveFile(MultipartFile file, String dirName, String fileName)
            throws IOException {
        S3Utility.uploadFile(dirName, fileName, file.getInputStream());
    }


    /**
     * Removes files from an S3 directory that do not match the given list of new files.
     * Performs deletion in batch to improve performance.
     *
     * @param extrasFolderName the directory name on S3
     * @param newFiles         a list of new files that should remain on S3
     */
    public void removeNotMatchingFiles(String extrasFolderName, List<String> newFiles) {
        List<String> existingFiles = S3Utility.listFolder(extrasFolderName);

        // Identify files that are not in the newFiles list
        List<String> filesToDelete = existingFiles.stream()
                .filter(existingFile -> !newFiles.contains(existingFile))
                .toList();

        // Delete files in batch
        S3Utility.deleteFiles(filesToDelete);
    }

    /**
     * Uploads multiple files to a specified directory on S3 in batch for improved performance.
     *
     * @param files            the files to upload
     * @param extrasFolderName the directory name on S3
     * @throws IOException if an I/O error occurs
     */
    public void uploadFiles(MultipartFile[] files, String extrasFolderName) throws IOException {
        if (files == null || files.length == 0) {
            return;
        }
        List<MultipartFile> fileList = Arrays.asList(files);
        S3Utility.uploadFiles(extrasFolderName, fileList);
    }

    public void removeFile(String object) {
        S3Utility.deleteFile(object);
    }

}
