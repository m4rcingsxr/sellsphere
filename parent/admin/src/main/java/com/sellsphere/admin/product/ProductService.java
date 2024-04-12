package com.sellsphere.admin.product;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ProductService {

    private static final int PRODUCTS_PER_PAGE = 12;

    private final ProductRepository productRepository;

    /**
     * Facilitates pagination for listing products
     *
     * @param pageNum the current page number
     * @param helper  a helper class for pagination and sorting
     */
    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, PRODUCTS_PER_PAGE, productRepository);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to retrieve
     * @return the found Product
     * @throws ProductNotFoundException if no product is found with the
     *                                  provided ID
     */
    public Product get(Integer id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    public boolean isNameUnique(Integer id, String name) {
        return productRepository.findByName(name)
                .map(existingProduct -> existingProduct.getId().equals(id))
                .orElse(true);
    }

    @Transactional
    public Product save(Product product, MultipartFile newPrimaryImage, MultipartFile[] extraImages)
            throws IOException {
        updateProductDates(product);
        generateProductAlias(product);

        ProductHelper.addProductImages(product, extraImages);

        Product savedProduct = productRepository.save(product);

        ProductHelper.saveExtraImages(savedProduct, extraImages);
        ProductHelper.savePrimaryImage(savedProduct, newPrimaryImage);

        return savedProduct;
    }

    private void updateProductDates(Product product) {
        if (product.getId() == null) {
            product.setCreatedTime(LocalDateTime.now());
        }
        product.updateProductTimestamp();
    }

    private void generateProductAlias(Product product) {
        String alias;
        if (product.getAlias() == null || product.getAlias().isEmpty()) {
            alias = product.getName().replace(" ", "_");
        } else {
            alias = product.getAlias().replace(" ", "_");
        }
        product.setAlias(alias);

    }

    public void deleteProduct(Integer id) throws ProductNotFoundException {
        Product product = get(id);
        S3Utility.removeFolder("product-photos/" + product.getId());

        productRepository.delete(product);
    }
}
