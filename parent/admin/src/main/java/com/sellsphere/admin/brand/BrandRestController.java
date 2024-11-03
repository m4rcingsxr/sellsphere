package com.sellsphere.admin.brand;

import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.payload.BrandDTO;
import com.sellsphere.common.entity.payload.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing brand-related operations such as checking uniqueness,
 * listing categories, and fetching all brands.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/brands")
public class BrandRestController {

    private final BrandService brandService;
    private final CategoryService categoryService;

    /**
     * Checks if the alias of a brand is unique, with an optional brand ID for updating scenarios.
     *
     * @param brandId the brand ID (optional, used for update scenarios)
     * @param alias   the alias of the brand to check for uniqueness
     * @return a ResponseEntity containing a boolean indicating whether the alias is unique
     */
    @PostMapping("/check-uniqueness")
    public ResponseEntity<Boolean> checkBrandAliasUniqueness(
            @RequestParam(value = "id", required = false) Integer brandId,
            @RequestParam("name") String alias) {

        // Validate alias length
        if (alias.length() > 45) {
            throw new IllegalArgumentException("Brand alias should not exceed 45 characters.");
        }

        // Check if the alias is unique
        boolean isUnique = brandService.isBrandNameUnique(brandId, alias);
        return ResponseEntity.ok(isUnique);
    }

    /**
     * Retrieves the category hierarchy for a specific brand.
     *
     * @param brandId the ID of the brand
     * @return a ResponseEntity containing a list of CategoryDTOs representing the category hierarchy
     * @throws BrandNotFoundException if the brand is not found
     */
    @GetMapping("/{brandId}/categories")
    public ResponseEntity<List<CategoryDTO>> getBrandCategoryHierarchy(@PathVariable("brandId") Integer brandId)
            throws BrandNotFoundException {

        // Fetch brand by ID
        Brand brand = brandService.getBrandById(brandId);

        // Get categories associated with the brand and build the hierarchy
        Set<Category> brandCategories = brand.getCategories();
        List<Category> hierarchy = categoryService.createHierarchy(brandCategories.stream().toList());
        List<CategoryDTO> hierarchyDTOs = hierarchy.stream().map(CategoryDTO::new).toList();

        return ResponseEntity.ok(hierarchyDTOs);
    }

    /**
     * Fetches all brands sorted by name in ascending order.
     *
     * @return a ResponseEntity containing a list of BrandDTOs representing all brands
     */
    @GetMapping("/fetch-all")
    public ResponseEntity<List<BrandDTO>> fetchAllBrands() {

        // Retrieve all brands sorted by name in ascending order
        List<Brand> brandList = brandService.listAllBrands("name", Sort.Direction.ASC);
        List<BrandDTO> brandDTOs = brandList.stream().map(BrandDTO::new).toList();

        return ResponseEntity.ok(brandDTOs);
    }

}
