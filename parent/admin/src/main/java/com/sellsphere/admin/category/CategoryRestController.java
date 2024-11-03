package com.sellsphere.admin.category;

import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.payload.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for category management operations,
 * including checking uniqueness and listing categories in a hierarchical structure.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    /**
     * Checks if the alias of a category is unique, with an optional category ID for updating scenarios.
     *
     * @param categoryId the category ID (optional, used for update scenarios)
     * @param alias      the alias of the category to check for uniqueness
     * @return ResponseEntity containing a Boolean indicating whether the alias is unique
     */
    @PostMapping("/check-alias-uniqueness")
    public ResponseEntity<Boolean> checkAliasUniqueness(
            @RequestParam(value = "id", required = false) Integer categoryId,
            @RequestParam("alias") String alias) {

        // Validate alias length
        if (alias.length() > 64) {
            throw new IllegalArgumentException("Category alias should not exceed 64 characters.");
        }

        // Check if the alias is unique
        boolean isUnique = categoryService.isCategoryAliasUnique(categoryId, alias);
        return ResponseEntity.ok(isUnique);
    }

    /**
     * Checks if the name of a category is unique, with an optional category ID for updating scenarios.
     *
     * @param categoryId the category ID (optional, used for update scenarios)
     * @param name       the name of the category to check for uniqueness
     * @return ResponseEntity containing a Boolean indicating whether the name is unique
     */
    @PostMapping("/check-name-uniqueness")
    public ResponseEntity<Boolean> checkNameUniqueness(
            @RequestParam(value = "id", required = false) Integer categoryId,
            @RequestParam("name") String name) {

        // Validate name length
        if (name.length() > 128) {
            throw new IllegalArgumentException("Category name should not exceed 128 characters.");
        }

        // Check if the name is unique
        boolean isUnique = categoryService.isCategoryNameUnique(categoryId, name);
        return ResponseEntity.ok(isUnique);
    }

    /**
     * Fetches all categories in a hierarchical structure, sorted by name in ascending order.
     *
     * @return ResponseEntity containing a list of CategoryDTOs representing the hierarchical category structure
     */
    @GetMapping("/fetch-all")
    public ResponseEntity<List<CategoryDTO>> fetchAllCategories() {

        // Retrieve root categories and create the hierarchy
        List<Category> rootCategories = categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC);
        List<Category> categoryHierarchy = categoryService.createHierarchy(rootCategories);

        // Convert to DTOs and return
        List<CategoryDTO> categoryDTOs = categoryHierarchy.stream().map(CategoryDTO::new).toList();
        return ResponseEntity.ok(categoryDTOs);
    }
}
