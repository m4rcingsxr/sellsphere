package com.sellsphere.admin.category;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.PagingHelper;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import com.sellsphere.common.entity.CategoryNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service class for managing Category-related operations.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class CategoryService {

    public static final int CATEGORY_PER_PAGE = 5;

    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    /**
     * Gets a category by its ID.
     *
     * @param id the category ID
     * @return the category
     * @throws CategoryNotFoundException if the category is not found
     */
    public Category get(Integer id) throws CategoryNotFoundException {
        return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
    }

    /**
     * Lists all root categories sorted by the specified field and direction.
     *
     * @param sortField the sort field
     * @param sortDir the sort direction
     * @return the list of categories
     */
    public List<Category> listAllRootCategoriesSorted(String sortField, String sortDir) {
        Sort sort = PagingHelper.getSort(sortField, sortDir);
        List<Category> parents = categoryRepository.findAllByParentIsNull(sort);

        return createHierarchy(parents);
    }

    /**
     * Checks if a category name is unique.
     *
     * @param id the category ID (optional)
     * @param name the category name
     * @return true if the name is unique, false otherwise
     */
    public boolean isNameUnique(Integer id, String name) {
        return categoryRepository.findByName(name)
                .map(category -> category.getId().equals(id))
                .orElse(true);
    }

    /**
     * Checks if a category alias is unique.
     *
     * @param id the category ID (optional)
     * @param alias the category alias
     * @return true if the alias is unique, false otherwise
     */
    public boolean isAliasUnique(Integer id, String alias) {
        return categoryRepository.findByAlias(alias)
                .map(category -> category.getId().equals(id))
                .orElse(true);
    }

    /**
     * Lists categories by page.
     *
     * @param pageNum the page number
     * @param helper the PagingAndSortingHelper
     */
    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        Pageable pageable = helper.createPageable(CATEGORY_PER_PAGE, pageNum);
        String searchKeyword = helper.getKeyword();

        if (searchKeyword == null) {
            listRootCategories(pageable, pageNum, helper);
        } else {
            helper.listEntities(pageNum, CATEGORY_PER_PAGE, categoryRepository);
        }
    }

    /**
     * Lists root categories by page.
     *
     * @param pageable the Pageable object
     * @param pageNum the page number
     * @param helper the PagingAndSortingHelper
     */
    private void listRootCategories(Pageable pageable, Integer pageNum, PagingAndSortingHelper helper) {
        Page<Category> page = categoryRepository.findAllByParentIsNull(pageable);

        // Converts the list of root categories to a hierarchical list including all descendants.
        List<Category> hierarchicalCategories = createHierarchy(page.getContent());

        helper.updateModelAttributes(pageNum, page.getTotalPages(), page.getTotalElements(), hierarchicalCategories);
    }

    /**
     * Deletes a branch of categories.
     *
     * @param categoryId the category ID
     * @throws CategoryNotFoundException if the category is not found
     */
    public void deleteCategoryBranch(Integer categoryId) throws CategoryNotFoundException {
        Category root = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        List<Integer> categoryList = expandHierarchy(root, 0)
                .map(Category::getId)
                .toList();

        categoryRepository.deleteAllById(categoryList);
    }

    /**
     * Creates a hierarchy of categories.
     *
     * @param categoryList the list of categories
     * @return the hierarchical list of categories
     */
    private List<Category> createHierarchy(List<Category> categoryList) {
        return categoryList.stream().filter(category -> category.getParent() == null).flatMap(
                category -> expandHierarchy(category, 0)).toList();
    }

    /**
     * Expands the hierarchy of a category.
     *
     * @param category the category
     * @param level the hierarchy level
     * @return the stream of categories
     */
    private Stream<Category> expandHierarchy(Category category, int level) {
        // Adjusts the category's name based on its level in the hierarchy and includes all its descendants.
        // Create copy of category to not mess with persistence context
        Category categoryCopy = new Category(category);
        categoryCopy.setName(prefixedName(category.getName(), level));

        // Recursively processes children, incrementing the level to adjust their names accordingly.
        return Stream.concat(Stream.of(categoryCopy), categoryCopy.getChildren().stream().flatMap(
                child -> expandHierarchy(child, level + 1)));
    }

    /**
     * Prefixes the category's name with dashes based on its hierarchy level.
     *
     * @param name the category name
     * @param level the hierarchy level
     * @return the prefixed name
     */
    private String prefixedName(String name, int level) {
        // Prefixes the category's name with dashes ("-") corresponding to its depth level in the hierarchy.
        return "-".repeat(level) + name;
    }

    /**
     * Saves a category and its image file.
     *
     * @param category the category
     * @param file the image file
     * @return the saved category
     * @throws CategoryIllegalStateException if the category state is illegal
     * @throws IOException if an I/O error occurs
     */
    public Category save(Category category, MultipartFile file) throws CategoryIllegalStateException, IOException {
        CategoryIcon categoryIcon = category.getCategoryIcon();

        if (categoryIcon != null) {
            if (categoryIcon.getIconPath() == null) {
                category.setCategoryIcon(null);
            } else {
                categoryIcon.setCategory(category);
                category.setCategoryIcon(categoryIcon);
            }
        }

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            category.setImage(fileName);
            Category savedCategory = save(category);

            String folderName = "category-photos/" + savedCategory.getId();
            fileService.saveSingleFile(file, folderName, fileName);

            return savedCategory;
        } else {
            return save(category);
        }
    }

    /**
     * Saves a category.
     *
     * @param category the category
     * @return the saved category
     * @throws CategoryIllegalStateException if the category state is illegal
     */
    public Category save(Category category) throws CategoryIllegalStateException {
        if (category.getParent() != null && (category.getId() != null && category.getId().equals(category.getParent().getId()))) {
            throw new CategoryIllegalStateException("Could not update Category. Category cannot reference itself as parent");
        }

        updateHierarchyPath(category);
        return categoryRepository.save(category);
    }

    /**
     * Updates the hierarchy path of a category.
     *
     * @param category the category
     */
    private void updateHierarchyPath(Category category) {
        if (category.getParent() != null) {
            // Obtains the hierarchy path from the parent, or starts with a dash if no path exists.
            String parentHierarchyPath = category.getParent().getAllParentIDs();
            parentHierarchyPath = parentHierarchyPath == null ? "-" : parentHierarchyPath;

            // Appends the current category's parent ID to the parent's path, followed by a dash, to update the hierarchy path.
            String updatedPath = parentHierarchyPath.concat(category.getParent().getId() + "-");
            category.setAllParentIDs(updatedPath);
        }
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id the category ID
     * @throws CategoryNotFoundException if the category is not found
     * @throws CategoryIllegalStateException if the category state is illegal
     */
    public void delete(Integer id) throws CategoryNotFoundException, CategoryIllegalStateException {
        Category category = get(id);

        if (!category.getChildren().isEmpty()) {
            List<String> childrenNames = category.getChildren().stream()
                    .map(Category::getName)
                    .toList();

            throw new CategoryIllegalStateException(
                    "Category " + category.getName() + " cannot be removed. " +
                            "First delete all referenced children:" + childrenNames);
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Toggles the enabled status of a category.
     *
     * @param id the category ID
     * @param enabled the new enabled status
     * @throws CategoryNotFoundException if the category is not found
     */
    public void toggleCategoryEnabledStatus(Integer id, boolean enabled) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        updateEnabledStatusRecursively(category, enabled);
        categoryRepository.save(category);
    }

    /**
     * Recursively updates the enabled status of a category and its children.
     *
     * @param category the category
     * @param enabled the new enabled status
     */
    private void updateEnabledStatusRecursively(Category category, boolean enabled) {
        // Sets the enabled status for the current category and recursively updates all children to match.
        category.setEnabled(enabled);

        // Iterates through each child, applying the same enabled status down the hierarchy.
        category.getChildren().forEach(child -> updateEnabledStatusRecursively(child, enabled));
    }

}
