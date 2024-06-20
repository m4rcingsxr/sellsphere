package com.sellsphere.admin.product;

import com.sellsphere.admin.PagingHelper;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.setting.SettingService;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.easyship.payload.shipment.SaveProductResponse;
import com.stripe.exception.StripeException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private static final int PRODUCTS_PER_PAGE = 12;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductRepository productRepository;
    private final SettingService settingService;
    private final EasyshipService easyshipService;


    /**
     * Retrieves a list of all products, sorted by the specified field and direction.
     *
     * @param sortField The field by which to sort the products.
     * @param sortDir   The direction of sorting (asc or desc).
     * @return A list of Product entities.
     */
    public List<Product> listAll(String sortField, String sortDir) {
        return productRepository.findAll(PagingHelper.getSort(sortField, sortDir));
    }

    /**
     * Facilitates pagination for listing products.
     *
     * @param pageNum The current page number.
     * @param helper  A helper class for pagination and sorting.
     */
    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, PRODUCTS_PER_PAGE, productRepository);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return The found Product.
     * @throws ProductNotFoundException If no product is found with the provided ID.
     */
    public Product get(Integer id) throws ProductNotFoundException {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    /**
     * Checks if a product name is unique.
     *
     * @param id   The ID of the product being checked (null for new products).
     * @param name The name of the product to check for uniqueness.
     * @return true if the name is unique or belongs to the product with the given ID; false
     * otherwise.
     */
    public boolean isNameUnique(Integer id, String name) {
        return productRepository.findByName(name)
                .map(existingProduct -> existingProduct.getId().equals(id))
                .orElse(true);
    }

    /**
     * Saves a product entity, including handling its primary and extra images.
     *
     * @param product         The Product entity to save.
     * @param newPrimaryImage The new primary image for the product.
     * @param extraImages     Additional images for the product.
     * @return The saved Product entity.
     * @throws IOException If there is an error handling the images.
     */
    @Transactional
    public Product save(Product product, MultipartFile newPrimaryImage, MultipartFile[] extraImages)
            throws IOException, SettingNotFoundException,
            CurrencyNotFoundException {
        Currency defaultCurrency = settingService.getCurrentCurrency();

        updateProductDates(product);
        generateProductAlias(product);

        ProductHelper.addProductImages(product, extraImages);

        Product savedProduct = productRepository.save(product);

        var productBuilder = com.sellsphere.easyship.payload.shipment.Product.builder()
                .id(product.getEasyshipId())
                .identifier(String.valueOf(savedProduct.getId()))
                .containsBatteryPi966(savedProduct.isContainsBatteryPi966())
                .containsBatteryPi967(savedProduct.isContainsBatteryPi967()) // Corrected method call
                .containsLiquids(savedProduct.isContainsLiquids())
                .costPrice(savedProduct.getCost())
                .costPriceCurrency(defaultCurrency.getCode().toUpperCase())
                .createdAt(savedProduct.getCreatedTime().format(formatter))
                .updatedAt(savedProduct.getProductUpdate().getUpdatedTime().format(formatter))
                .hsCode(product.getHsCode())
                .imageUrl(product.getMainImagePath())
                .name(product.getName())
                .sellingPrice(product.getDiscountPrice())
                .height(product.getHeight())
                .length(product.getLength())
                .weight(product.getWeight())
                .width(product.getWidth());

        SaveProductResponse saveProductResponse = easyshipService.saveProduct(productBuilder.build());
        savedProduct.setEasyshipId(saveProductResponse.getProduct().getId());

        ProductHelper.saveExtraImages(savedProduct, extraImages);
        ProductHelper.savePrimaryImage(savedProduct, newPrimaryImage);

        return savedProduct;
    }

    /**
     * Updates the creation and modification timestamps of a product.
     *
     * @param product The Product entity to update.
     */
    private void updateProductDates(Product product) {
        if (product.getId() == null) {
            product.setCreatedTime(LocalDateTime.now());
        }
        product.updateProductTimestamp();
    }

    /**
     * Generates a product alias based on its name or existing alias.
     *
     * @param product The Product entity for which to generate an alias.
     */
    private void generateProductAlias(Product product) {
        String alias;
        if (product.getAlias() == null || product.getAlias().isEmpty()) {
            alias = product.getName().replace(" ", "_");
        } else {
            alias = product.getAlias().replace(" ", "_");
        }
        product.setAlias(alias);
    }

    /**
     * Deletes a product by its ID and removes associated images from S3.
     *
     * @param id The ID of the product to delete.
     * @throws ProductNotFoundException If no product is found with the provided ID.
     */
    public void deleteProduct(Integer id) throws ProductNotFoundException, StripeException {
        Product product = get(id);
        S3Utility.removeFolder("product-photos/" + product.getId());

        easyshipService.deleteProduct(product.getEasyshipId());

        productRepository.delete(product);
    }

    /**
     * Updates the enabled status of a product.
     *
     * @param id     The ID of the product to update.
     * @param status The new enabled status of the product.
     * @throws ProductNotFoundException If no product is found with the provided ID.
     */
    public void updateProductEnabledStatus(Integer id, boolean status)
            throws ProductNotFoundException, StripeException {
        Product product = productRepository.findById(id).orElseThrow(
                ProductNotFoundException::new);
        product.setEnabled(status);

        productRepository.save(product);
    }
}

