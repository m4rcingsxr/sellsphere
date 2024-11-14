package com.sellsphere.admin.product;

import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingHelper;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class responsible for managing products within the system.
 * This class provides functionality for CRUD operations
 * on products, including handling associated images and managing pagination.
 *
 * <p>Main functionalities include:</p>
 * <ul>
 *     <li>Listing products with pagination and sorting options</li>
 *     <li>Retrieving, creating, updating, and deleting products</li>
 *     <li>Managing product images, including primary and extra images, through AWS S3</li>
 *     <li>Checking the uniqueness of product names</li>
 *     <li>Updating product statuses (enabled/disabled)</li>
 * </ul>
 *
 * This class also interacts with the {@link ProductRepository} to perform database operations
 * and uses AWS S3 for file storage via {@link S3Utility}.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class ProductService {

    private static final int PRODUCTS_PER_PAGE = 12;
    private final ProductRepository productRepository;


    /**
     * Retrieves a list of all products, sorted by the specified field and direction.
     *
     * @param sortField The field by which to sort the products.
     * @param sortDir   The direction of sorting (asc or desc).
     * @return A list of Product entities.
     */
    public List<Product> listAllProducts(String sortField, Sort.Direction sortDir) {
        return productRepository.findAll(PagingHelper.getSort(sortField, sortDir));
    }

    /**
     * Facilitates pagination for listing products.
     *
     * @param pageNum The current page number.
     * @param helper  A helper class for pagination and sorting.
     */
    public void paginateProducts(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, PRODUCTS_PER_PAGE, productRepository);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return The found Product.
     * @throws ProductNotFoundException If no product is found with the provided ID.
     */
    public Product getProductById(Integer id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    /**
     * Checks if a product name is unique or already exists in the database.
     *
     * @param id   The ID of the product (can be null for new products).
     * @param name The name to check for uniqueness.
     * @return true if the name is unique; false otherwise.
     */
    public boolean isProductNameUnique(Integer id, String name) {
        return productRepository.findByName(name)
                .map(existingProduct -> existingProduct.getId().equals(id))
                .orElse(true);
    }

    /**
     * Saves a product entity, handling both the primary image and any additional images.
     *
     * @param product         The product to save.
     * @param newPrimaryImage The new primary image (can be null).
     * @param extraImages     Additional images for the product (optional).
     * @return The saved product.
     * @throws IOException If there's an error uploading images.
     */
    public Product saveProduct(Product product, MultipartFile newPrimaryImage, MultipartFile[] extraImages)
            throws IOException {
        updateProductTimestamps(product);
        generateAliasForProduct(product);

        ProductHelper.addProductImages(product, extraImages);

        Product savedProduct = productRepository.save(product);

        ProductHelper.saveExtraImages(savedProduct, extraImages);
        ProductHelper.savePrimaryImage(savedProduct, newPrimaryImage);

        return savedProduct;
    }

    /**
     * Updates the creation and last modification timestamps for a product.
     *
     * @param product The product to update timestamps for.
     */
    private void updateProductTimestamps(Product product) {
        if (product.getId() == null) {
            product.setCreatedTime(LocalDateTime.now());
        }
        product.updateProductTimestamp();
    }

    /**
     * Generates an alias for the product based on its name or alias.
     *
     * @param product The product for which to generate an alias.
     */
    private void generateAliasForProduct(Product product) {
        String alias = (product.getAlias() == null || product.getAlias().isEmpty())
                ? product.getName().replace(" ", "_")
                : product.getAlias().replace(" ", "_");
        product.setAlias(alias);
    }

    /**
     * Deletes a product by its ID and removes its associated images from S3.
     *
     * @param id The ID of the product to delete.
     * @throws ProductNotFoundException If the product with the given ID is not found.
     */
    public void deleteProductById(Integer id) throws ProductNotFoundException {
        Product product = getProductById(id);
        S3Utility.removeFolder("product-photos/" + product.getId());

        productRepository.delete(product);
    }

    /**
     * Updates the enabled/disabled status of a product.
     *
     * @param id     The ID of the product.
     * @param status The new enabled status.
     * @throws ProductNotFoundException If the product is not found.
     */
    public void updateProductEnabledStatus(Integer id, boolean status)
            throws ProductNotFoundException {
        Product product = productRepository.findById(id).orElseThrow(
                ProductNotFoundException::new);
        product.setEnabled(status);

        productRepository.save(product);
    }

    /**
     * Retrieves all products that belong to a specified brand.
     *
     * @param brandId The ID of the brand.
     * @return A list of products belonging to the brand.
     */
    public List<Product> listAllProductsByBrand(Integer brandId) {
        return productRepository.findAllByBrandId(brandId);
    }

    /**
     * Retrieves all products that belong to a specified category.
     *
     * @param categoryId The ID of the category.
     * @return A list of products in the category.
     */
    public List<Product> listAllProductsByCategory(Integer categoryId) {
        return productRepository.findAllByCategoryId(categoryId);
    }

    /**
     * Searches for products that match a given keyword.
     *
     * @param keyword The keyword to search for.
     * @return A list of products matching the keyword.
     */
    public List<Product> searchProductsByKeyword(String keyword) {
        return productRepository.findAll(keyword, Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<Product> findByIds(List<String> ids) {
        return productRepository.findAllById(ids.stream().map(Integer::parseInt).toList());
    }

    public long countByBrandId(Integer id) {
        return productRepository.countAllByBrandId(id);
    }
}

