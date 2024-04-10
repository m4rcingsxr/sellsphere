package com.sellsphere.admin.brand;

import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BrandNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBrandNotFoundException(
            BrandNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.",
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
