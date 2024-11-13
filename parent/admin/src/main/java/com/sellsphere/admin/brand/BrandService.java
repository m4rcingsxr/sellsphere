package com.sellsphere.admin.brand;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service class for handling operations related to brands, including CRUD operations,
 * pagination, sorting, file management, and validation for uniqueness.
 */
@RequiredArgsConstructor
@Service
public class BrandService {

    private static final int BRANDS_PER_PAGE = 10;
    private static final String BRAND_PHOTOS_DIR = "brand-photos/";

    private final BrandRepository brandRepository;

    /**
     * Paginates the list of brands based on the page number and sorting helper.
     *
     * @param pageNum the page number to display
     * @param helper  the pagination and sorting helper
     */
    public void listBrandsByPage(Integer pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, BRANDS_PER_PAGE, brandRepository);
    }

    /**
     * Retrieves all brands, sorted by the specified field and direction.
     *
     * @param sortField     the field to sort by
     * @param sortDirection the sorting direction (ASC or DESC)
     * @return a list of sorted brands
     */
    public List<Brand> listAllBrands(String sortField, Sort.Direction sortDirection) {
        Sort sort = PagingHelper.getSort(sortField, sortDirection);
        return brandRepository.findAll(sort);
    }

    /**
     * Fetches a brand by its ID.
     *
     * @param id the brand's ID
     * @return the found brand
     * @throws BrandNotFoundException if the brand is not found
     */
    public Brand getBrandById(Integer id) throws BrandNotFoundException {
        return brandRepository.findById(id).orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + id));
    }

    /**
     * Saves the brand and handles the logo file if present.
     *
     * @param brand the brand entity to save
     * @param file  the logo file to save (optional)
     * @return the saved brand
     * @throws IOException if an I/O error occurs during file saving
     */
    public Brand saveBrand(Brand brand, MultipartFile file) throws IOException {
        // Ensure no leading dashes remain in the category names
        cleanUpCategoryNames(brand);

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            brand.setLogo(fileName);

            Brand savedBrand = brandRepository.save(brand);

            // Save the logo file to the designated directory in S3
            String folderName = BRAND_PHOTOS_DIR + savedBrand.getId();
            FileService.saveSingleFile(file, folderName, fileName);
            return savedBrand;
        } else {
            return brandRepository.save(brand);
        }
    }

    /**
     * Cleans up category names by removing leading dashes for better consistency.
     *
     * @param brand the brand containing the categories to clean
     */
    private void cleanUpCategoryNames(Brand brand) {
        brand.getCategories().forEach(this::removeLeadingDashesFromCategory);
    }

    /**
     * Recursively removes leading dashes from category names.
     *
     * @param category the category entity to clean up
     */
    private void removeLeadingDashesFromCategory(Category category) {
        category.setName(category.getName().replace("-", ""));
        category.getChildren().forEach(this::removeLeadingDashesFromCategory);
    }

    /**
     * Validates if the brand name is unique, allowing for optional exclusion of a brand ID (used during updates).
     *
     * @param id   the ID of the brand to exclude (for updates), can be null
     * @param name the name to check for uniqueness
     * @return true if the name is unique, false otherwise
     */
    public boolean isBrandNameUnique(Integer id, String name) {
        return brandRepository.findByName(name)
                .map(existingBrand -> existingBrand.getId().equals(id))
                .orElse(true);
    }

    /**
     * Deletes a brand by its ID.
     *
     * @param id the brand's ID
     * @throws BrandNotFoundException if the brand is not found
     */
    public void deleteBrandById(Integer id) throws BrandNotFoundException {
        Brand brand = getBrandById(id);
        brandRepository.delete(brand);
    }


}
