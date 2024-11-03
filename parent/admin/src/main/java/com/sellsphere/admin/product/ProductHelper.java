package com.sellsphere.admin.product;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import com.sellsphere.common.entity.ProductImage;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class providing helper methods for managing product details and images.
 */
@UtilityClass
public class ProductHelper {

    private static final String PRODUCT_PHOTOS_DIR = "product-photos/";

    /**
     * Adds product details to a given product.
     *
     * @param product      The product to which details are to be added.
     * @param detailNames  An array of detail names.
     * @param detailValues An array of detail values.
     * @throws IllegalStateException If the lengths of detailNames and detailValues arrays do not match.
     */
    public static void addProductDetails(Product product, @Nullable String[] detailNames,
                                         @Nullable String[] detailValues) {
        if(detailNames == null || detailValues == null) {
            return;
        }

        if (detailNames.length != detailValues.length) {
            throw new IllegalStateException(
                    "detail names and values arrays must have same length.");
        }

        int length = detailNames.length;
        for (int i = 0; i < length; i++) {
            String detailName = detailNames[i];
            String detailValue = detailValues[i];

            ProductDetail productDetail = new ProductDetail();
            productDetail.setName(detailName);
            productDetail.setValue(detailValue);

            product.addProductDetail(productDetail);
        }
    }

    /**
     * Adds product images to a given product.
     *
     * @param product     The product to which images are to be added.
     * @param extraImages An array of multipart files representing the extra images.
     */
    public static void addProductImages(Product product, MultipartFile[] extraImages) {
        if (extraImages != null) {
            for (MultipartFile extraImage : extraImages) {
                ProductImage productImage = new ProductImage();
                productImage.setName(extraImage.getOriginalFilename());

                product.addProductImage(productImage);
            }
        }
    }

    /**
     * Saves extra images of a product to the file system and updates S3 storage.
     *
     * @param savedProduct The product entity that has been saved.
     * @param extraImages  An array of multipart files representing the extra images.
     * @throws IOException           If an I/O error occurs while saving the images.
     * @throws IllegalStateException If the saved product does not have an assigned ID.
     */
    public static void saveExtraImages(Product savedProduct, MultipartFile[] extraImages)
            throws IOException {
        if (savedProduct.getId() == null) {
            throw new IllegalStateException("Saved product must have assigned id.");
        }

        if (extraImages == null || extraImages.length == 0) {
            return;
        }

        List<String> existingExtraImageS3Objects = new ArrayList<>();
        String extrasFolderName = PRODUCT_PHOTOS_DIR + savedProduct.getId() +
                "/extras";

        for (ProductImage image : savedProduct.getImages()) {
            existingExtraImageS3Objects.add(extrasFolderName + "/" + image.getName());
        }

        FileService.removeNotMatchingFiles(extrasFolderName, existingExtraImageS3Objects);
        for (MultipartFile extraImage : extraImages) {
            FileService.saveFile(extraImage, extrasFolderName, extraImage.getOriginalFilename());
        }
    }

    /**
     * Saves the primary image of a product to the file system.
     *
     * @param product      The product entity to which the primary image belongs.
     * @param primaryImage A multipart file representing the primary image.
     * @throws IOException If an I/O error occurs while saving the image.
     */
    public static void savePrimaryImage(Product product, MultipartFile primaryImage)
            throws IOException {
        if (primaryImage == null || primaryImage.isEmpty()) return;

        String fileName = StringUtils.cleanPath(
                Objects.requireNonNull(primaryImage.getOriginalFilename()));
        String folderName = PRODUCT_PHOTOS_DIR + product.getId();
        FileService.saveSingleFile(primaryImage, folderName, fileName);
        product.setMainImage(fileName);
    }

}

