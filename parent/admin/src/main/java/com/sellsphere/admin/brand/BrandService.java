package com.sellsphere.admin.brand;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.PagingHelper;
import com.sellsphere.admin.page.PagingAndSortingHelper;
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
 * Service class for managing Brand-related operations.
 */
@RequiredArgsConstructor
@Service
public class BrandService {

    private static final int BRANDS_PER_PAGE = 10;
    private static final String BRAND_PHOTOS_DIR = "brand-photos/";

    private final BrandRepository brandRepository;

    /**
     * Lists brands by page.
     *
     * @param pageNum the page number
     * @param helper the PagingAndSortingHelper
     */
    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, BRANDS_PER_PAGE, brandRepository);
    }

    /**
     * Lists all brands sorted by the specified field and direction.
     *
     * @param sortField the sort field
     * @param sortDirection the sort direction
     * @return the list of brands
     */
    public List<Brand> listAll(String sortField, String sortDirection) {
        Sort sort = PagingHelper.getSort(sortField, sortDirection);
        return brandRepository.findAll(sort);
    }

    /**
     * Gets a brand by its ID.
     *
     * @param id the brand ID
     * @return the brand
     * @throws BrandNotFoundException if the brand is not found
     */
    public Brand get(Integer id) throws BrandNotFoundException {
        return brandRepository.findById(id).orElseThrow(BrandNotFoundException::new);
    }

    /**
     * Saves a brand and its logo file.
     *
     * @param brand the brand
     * @param file the logo file
     * @return the saved brand
     * @throws IOException if an I/O error occurs
     */
    public Brand save(Brand brand, MultipartFile file) throws IOException {
        removeLeadingDashFromCategories(brand);

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            brand.setLogo(fileName);

            Brand savedBrand = brandRepository.save(brand);

            String folderName = BRAND_PHOTOS_DIR + savedBrand.getId();
            FileService.saveSingleFile(file, folderName, fileName);
            return savedBrand;

        } else {
            return brandRepository.save(brand);
        }
    }

    /**
     * Removes leading dashes (hierarchy) from category names associated with the brand.
     *
     * @param brand the brand
     */
    private void removeLeadingDashFromCategories(Brand brand) {
        brand.getCategories().forEach(this::removeCategoryDashes);
    }

    /**
     * Removes leading dashes from a category name.
     *
     * @param category the category
     */
    private void removeCategoryDashes(Category category) {
        category.setName(category.getName().replace("-", ""));
        category.getChildren().forEach(this::removeCategoryDashes);
    }

    /**
     * Checks if a brand name is unique.
     *
     * @param id the brand ID (optional)
     * @param name the brand name
     * @return true if the name is unique, false otherwise
     */
    public boolean isNameUnique(Integer id, String name) {
        return brandRepository.findByName(name)
                .map(existingBrand -> existingBrand.getId().equals(id))
                .orElse(true);
    }

    /**
     * Deletes a brand by its ID.
     *
     * @param id the brand ID
     * @throws BrandNotFoundException if the brand is not found
     */
    public void delete(Integer id) throws BrandNotFoundException {
        Brand brand = get(id);
        brandRepository.delete(brand);
    }

}
