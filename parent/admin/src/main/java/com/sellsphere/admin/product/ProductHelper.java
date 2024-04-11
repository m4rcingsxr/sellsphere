package com.sellsphere.admin.product;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import com.sellsphere.common.entity.ProductImage;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ProductHelper {

    private static final String PRODUCT_PHOTOS_DIR = "product-photos/";


    public static void addProductDetails(Product product, String[] detailNames,
                                         String[] detailValues) {
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

    public static void addProductImages(Product product, MultipartFile[] extraImages) {
        if (extraImages != null) {
            for (MultipartFile extraImage : extraImages) {
                ProductImage productImage = new ProductImage();
                productImage.setName(extraImage.getOriginalFilename());

                product.addProductImage(productImage);
            }
        }
    }

    public static void saveExtraImages(Product savedProduct, MultipartFile[] extraImages)
            throws IOException {
        if (savedProduct.getId() == null) {
            throw new IllegalStateException("Saved product must have assigned id.");
        }

        List<String> existingExtraImageS3Objects = new ArrayList<>();
        String extrasFolderName = PRODUCT_PHOTOS_DIR + savedProduct.getId() +
                "/extras";

        for (ProductImage image : savedProduct.getImages()) {
            existingExtraImageS3Objects.add(extrasFolderName + "/" + image.getName());
        }

        FileService.removeNotMatchingFiles(extrasFolderName, existingExtraImageS3Objects);
        FileService.uploadFiles(extraImages, extrasFolderName);
    }

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
