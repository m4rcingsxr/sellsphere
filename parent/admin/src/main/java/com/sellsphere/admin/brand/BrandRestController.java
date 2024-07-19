package com.sellsphere.admin.brand;

import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.payload.BrandDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Brand-related operations.
 */
@RequiredArgsConstructor
@RestController
public class BrandRestController {

    private final BrandService service;
    private final CategoryService categoryService;

    /**
     * Checks the uniqueness of a brand alias.
     *
     * @param brandId the brand ID (optional)
     * @param alias the brand alias
     * @return ResponseEntity with a Boolean indicating uniqueness
     */
    @PostMapping("/brands/check_uniqueness")
    public ResponseEntity<Boolean> isAliasUnique(
            @RequestParam(value = "id", required = false) Integer brandId,
            @RequestParam(value = "name") String alias) {
        if(alias.length() > 45) {
            throw new IllegalArgumentException("Brand name length should not exceeds 45 characters");
        }

        boolean isUnique = service.isNameUnique(brandId, alias);
        return ResponseEntity.ok(isUnique);
    }

    @GetMapping("/brands/{id}/categories")
    public ResponseEntity<List<CategoryDTO>> listCategoriesForBrand(@PathVariable Integer id)
            throws BrandNotFoundException {
        Brand brand = service.get(id);
        List<Category> brandCategories = brand.getCategories().stream().toList();
        List<Category> hierarchy = categoryService.createHierarchy(brandCategories);
        List<CategoryDTO> hierarchyDTO = hierarchy.stream().map(CategoryDTO::new).toList();

        return ResponseEntity.ok(hierarchyDTO);
    }

    @GetMapping("/brands/fetch-all")
    public ResponseEntity<List<BrandDTO>> listAllBrands() {
        List<Brand> brandList = service.listAll("name", Constants.SORT_ASCENDING);
        return ResponseEntity.ok(brandList.stream().map(BrandDTO::new).toList());
    }

}
