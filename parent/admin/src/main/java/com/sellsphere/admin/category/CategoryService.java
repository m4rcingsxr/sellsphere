package com.sellsphere.admin.category;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingHelper;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.common.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Service class for handling operations related to categories, including CRUD operations,
 * pagination, sorting, file management, and validation for uniqueness.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class CategoryService {

    private static final int CATEGORIES_PER_PAGE = 5;
    private static final String CATEGORY_PHOTOS_DIR = "category-photos/";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Retrieves a category by its ID.
     *
     * @param id the category ID
     * @return the found category
     * @throws CategoryNotFoundException if the category is not found
     */
    public Category getCategoryById(Integer id) throws CategoryNotFoundException {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
    }

    /**
     * Lists all root categories, sorted by the specified field and direction,
     * and converts them into a hierarchical structure.
     *
     * @param sortField the field to sort by
     * @param sortDir   the sorting direction (ASC or DESC)
     * @return the hierarchical list of root categories
     */
    public List<Category> listAllRootCategoriesSorted(String sortField, Sort.Direction sortDir) {
        Sort sort = PagingHelper.getSort(sortField, sortDir);
        List<Category> rootCategories = categoryRepository.findAllByParentIsNull(sort);
        return createHierarchy(rootCategories);
    }

    /**
     * Checks whether a category name is unique, allowing for an optional category ID
     * (used during update scenarios to exclude the current category).
     *
     * @param id   the category ID (optional)
     * @param name the category name
     * @return true if the name is unique, false otherwise
     */
    public boolean isCategoryNameUnique(Integer id, String name) {
        return categoryRepository.findByName(name)
                .map(category -> category.getId().equals(id))
                .orElse(true);
    }

    /**
     * Checks whether a category alias is unique, allowing for an optional category ID
     * (used during update scenarios to exclude the current category).
     *
     * @param id    the category ID (optional)
     * @param alias the category alias
     * @return true if the alias is unique, false otherwise
     */
    public boolean isCategoryAliasUnique(Integer id, String alias) {
        return categoryRepository.findByAlias(alias)
                .map(category -> category.getId().equals(id))
                .orElse(true);
    }


    /**
     * Paginates the list of categories based on the page number and helper for sorting and filtering.
     *
     * @param pageNum the page number to display
     * @param helper  the pagination and sorting helper
     */
    public void listCategoriesByPage(Integer pageNum, PagingAndSortingHelper helper) {
        Pageable pageable = helper.createPageable(CATEGORIES_PER_PAGE, pageNum);
        String searchKeyword = helper.getKeyword();

        if (searchKeyword == null) {
            listRootCategoriesByPage(pageable, pageNum, helper);
        } else {
            helper.listEntities(pageNum, CATEGORIES_PER_PAGE, categoryRepository);
        }
    }

    /**
     * Paginates the list of root categories and updates the model attributes in the helper.
     *
     * @param pageable the pagination object
     * @param pageNum  the page number
     * @param helper   the pagination and sorting helper
     */
    private void listRootCategoriesByPage(Pageable pageable, Integer pageNum, PagingAndSortingHelper helper) {
        Page<Category> page = categoryRepository.findAllByParentIsNull(pageable);
        List<Category> hierarchicalCategories = createHierarchy(page.getContent());
        helper.updateModelAttributes(pageNum, page.getTotalPages(), page.getTotalElements(), hierarchicalCategories);
    }

    /**
     * Deletes a category branch, including all its subcategories, by its root category ID.
     *
     * @param categoryId the root category ID
     * @throws CategoryNotFoundException if the category is not found
     */
    public void deleteCategoryBranch(Integer categoryId) throws CategoryNotFoundException {
        Category root = getCategoryById(categoryId);
        List<Integer> categoryIds = expandCategoryHierarchy(root, 0)
                .map(Category::getId)
                .toList();
        categoryRepository.deleteAllById(categoryIds);
    }


    /**
     * Creates a hierarchy of categories, starting from the root categories.
     *
     * @param categoryList the list of root categories
     * @return the hierarchical list of categories
     */
    public List<Category> createHierarchy(List<Category> categoryList) {
        return categoryList.stream()
                .filter(category -> category.getParent() == null)
                .flatMap(category -> expandCategoryHierarchy(category, 0))
                .toList();
    }

    /**
     * Expands the hierarchy for a category, adding all its children recursively.
     *
     * @param category the root category
     * @param level    the hierarchy level (depth)
     * @return the stream of categories in the hierarchy
     */
    private Stream<Category> expandCategoryHierarchy(Category category, int level) {
        Category categoryCopy = new Category(category);
        categoryCopy.setName(prefixedName(category.getName(), level));
        return Stream.concat(Stream.of(categoryCopy),
                             categoryCopy.getChildren().stream().flatMap(child -> expandCategoryHierarchy(child, level + 1)));
    }

    /**
     * Prefixes a category's name with dashes according to its hierarchy level.
     *
     * @param name  the category name
     * @param level the hierarchy level
     * @return the prefixed name
     */
    private String prefixedName(String name, int level) {
        return "-".repeat(level) + name;
    }

    /**
     * Saves a category and its associated image file.
     *
     * @param category the category entity to save
     * @param file     the image file (optional)
     * @return the saved category
     * @throws IOException                   if an I/O error occurs
     * @throws CategoryIllegalStateException if the category state is illegal
     */
    public Category saveCategory(Category category, MultipartFile file) throws IOException, CategoryIllegalStateException {
        setupCategoryIcon(category);

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            category.setImage(fileName);
            Category savedCategory = saveCategoryInternal(category);

            String folderName = CATEGORY_PHOTOS_DIR + savedCategory.getId();
            FileService.saveSingleFile(file, folderName, fileName);
            return savedCategory;
        } else {
            return saveCategoryInternal(category);
        }
    }

    private void setupCategoryIcon(Category category) {
        CategoryIcon categoryIcon = category.getCategoryIcon();
        if(categoryIcon.getIconPath() == null) {
            category.setCategoryIcon(null);
        } else {
            categoryIcon.setCategory(category);
        }
    }

    /**
     * Saves a category entity to the repository and validates its hierarchy.
     *
     * @param category the category entity to save
     * @return the saved category
     * @throws CategoryIllegalStateException if the category state is invalid
     */
    private Category saveCategoryInternal(Category category) throws CategoryIllegalStateException {
        if (category.getParent() != null && category.getId() != null
                && category.getId().equals(category.getParent().getId())) {
            throw new CategoryIllegalStateException("Category cannot reference itself as a parent.");
        }
        updateCategoryHierarchyPath(category);
        return categoryRepository.save(category);
    }

    /**
     * Updates the hierarchy path of a category based on its parent's hierarchy.
     *
     * @param category the category to update
     */
    private void updateCategoryHierarchyPath(Category category) {
        if (category.getParent() != null) {
            String parentPath = category.getParent().getAllParentIDs();
            String updatedPath = (parentPath == null ? "-" : parentPath) + category.getParent().getId() + "-";
            category.setAllParentIDs(updatedPath);
        }
    }

    /**
     * Deletes a category by its ID, ensuring it has no child categories.
     *
     * @param id the category ID
     * @throws CategoryNotFoundException      if the category is not found
     * @throws CategoryIllegalStateException if the category has child categories
     */
    public void deleteCategoryById(Integer id) throws CategoryNotFoundException, CategoryIllegalStateException {
        Category category = getCategoryById(id);
        if (!category.getChildren().isEmpty()) {
            List<String> childNames = category.getChildren().stream()
                    .map(Category::getName)
                    .toList();
            throw new CategoryIllegalStateException("Category has children: " + childNames + ". Please delete them first.");
        }

        categoryRepository.delete(category);
    }

    /**
     * Toggles the enabled status of a category and all its child categories.
     *
     * @param id      the category ID
     * @param enabled the new enabled status
     * @throws CategoryNotFoundException if the category is not found
     */
    public void toggleCategoryEnabledStatus(Integer id, boolean enabled) throws CategoryNotFoundException {
        Category category = getCategoryById(id);
        updateEnabledStatusRecursively(category, enabled);
        categoryRepository.save(category);
    }

    /**
     * Recursively updates the enabled status for a category and all its children.
     *
     * @param category the root category
     * @param enabled  the new enabled status
     */
    private void updateEnabledStatusRecursively(Category category, boolean enabled) {
        category.setEnabled(enabled);
        category.getChildren().forEach(child -> updateEnabledStatusRecursively(child, enabled));
    }


    public List<Category> listAll(String name, Sort.Direction direction) {
        return categoryRepository.findAll(Sort.by(direction, name));
    }

    public List<Category> listCategoriesParents(Set<Category> categories) {
        return categories.stream().map(this::getRoot).toList();
    }

    private Category getRoot(Category category) {
        if(category.getParent() == null) return category;
        return getRoot(category.getParent());
    }


}
