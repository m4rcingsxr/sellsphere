package com.sellsphere.admin.category;

import com.sellsphere.admin.brand.CategoryDTO;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Category-related operations.
 */
@RequiredArgsConstructor
@RestController
public class CategoryRestController {

    private final CategoryService service;

    /**
     * Checks the uniqueness of a category alias.
     *
     * @param categoryId the category ID (optional)
     * @param alias the category alias
     * @return ResponseEntity with a Boolean indicating uniqueness
     */
    @PostMapping("/categories/check_alias_uniqueness")
    public ResponseEntity<Boolean> isAliasUnique(@RequestParam(value = "id", required = false) Integer categoryId,
                                                 @RequestParam(value = "alias") String alias) {
        if(alias.length() > 64) {
            throw new IllegalArgumentException("Alias length should not exceed 64 characters");
        }

        boolean isUnique = service.isAliasUnique(categoryId, alias);
        return ResponseEntity.ok(isUnique);
    }

    /**
     * Checks the uniqueness of a category name.
     *
     * @param categoryId the category ID (optional)
     * @param name the category name
     * @return ResponseEntity with a Boolean indicating uniqueness
     */
    @PostMapping("/categories/check_name_uniqueness")
    public ResponseEntity<Boolean> isNameUnique(@RequestParam(value = "id", required = false) Integer categoryId,
                                                @RequestParam(value = "name") String name) {
        if(name.length() > 128) {
            throw new IllegalArgumentException("Name length should not exceed 128 characters");
        }

        boolean isUnique = service.isNameUnique(categoryId, name);
        return ResponseEntity.ok(isUnique);
    }


    @GetMapping("/categories/fetch-all")
    public ResponseEntity<List<CategoryDTO>> listAllCategories() {
        List<Category> parentList = service.listAllRootCategoriesSorted("name",
                                                                                Constants.SORT_ASCENDING
        );
        List<Category> hierarchy = service.createHierarchy(parentList);

        return ResponseEntity.ok(hierarchy.stream().map(CategoryDTO::new).toList());
    }

}
