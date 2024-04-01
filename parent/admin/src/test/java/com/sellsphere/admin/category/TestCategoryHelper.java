package com.sellsphere.admin.category;

import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIcon;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class TestCategoryHelper {

    public static void assertCategoryEnabledStatus(Category category, boolean expectedStatus) {
        assertEquals(expectedStatus, category.isEnabled(), "Category with ID " + category.getId() + " should have enabled status " + expectedStatus);
        category.getChildren().forEach(child -> assertCategoryEnabledStatus(child, expectedStatus));
    }

    public static void assertRootCategoriesSortedByName(List<Category> categories) {
        List<String> rootNames = categories.stream()
                .filter(category -> category.getParent() == null)
                .map(Category::getName)
                .collect(Collectors.toList());

        List<String> sortedRootNames = rootNames.stream().sorted().collect(Collectors.toList());

        assertEquals(sortedRootNames, rootNames, "Root categories should be sorted by name");
    }

    public static void assertHierarchy(Category copy, List<Category> rootCategories) {
        String hierarchicalName = rootCategories.stream()
                .map(root -> getHierarchicalName(root, copy.getId(), 0))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        assertNotNull(hierarchicalName, "Hierarchical name should not be null");
        assertEquals(hierarchicalName, copy.getName(), "Names should match for category ID: " + copy.getId());

        List<Category> children = copy.getChildren().stream()
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .collect(Collectors.toList());

        assertEquals(children, children, "Children should be sorted by name for category ID: " + copy.getId());
    }

    private static String getHierarchicalName(Category current, Integer expectedId, int level) {
        if (current.getId().equals(expectedId)) {
            return "-".repeat(level) + current.getName();
        }

        for (Category child : current.getChildren()) {
            String name = getHierarchicalName(child, expectedId, level + 1);
            if (name != null) {
                return name;
            }
        }

        return null;
    }

    public static void assertCategoryBranchDeleted(CategoryRepository categoryRepository, Category rootCategory) {
        List<Integer> ids = rootCategory.getChildren().stream()
                .flatMap(child -> expandHierarchy(child, 0))
                .map(Category::getId)
                .collect(Collectors.toList());

        ids.add(rootCategory.getId());

        ids.forEach(id -> assertFalse(categoryRepository.existsById(id), "Category with ID " + id + " should be deleted"));
    }

    public static void assertCategoryNotDeleted(CategoryRepository categoryRepository, Category rootCategory) {
        List<Integer> ids = expandHierarchy(rootCategory, 0)
                .map(Category::getId)
                .collect(Collectors.toList());

        ids.add(rootCategory.getId());

        ids.forEach(id -> assertTrue(categoryRepository.existsById(id), "Category with ID " + id + " should not be deleted"));
    }


    private static Stream<Category> expandHierarchy(Category category, int level) {
        return Stream.concat(Stream.of(category), category.getChildren().stream().flatMap(child -> expandHierarchy(child, level + 1)));
    }

    public static List<Category> generateRootCategories() {
        return List.of(generateComputersCategory(), generateTabletsCategory());
    }

    public static void assertCategoryDeleted(CategoryRepository categoryRepository, Category category) {
        assertFalse(categoryRepository.existsById(category.getId()), "Category with ID " + category.getId() + " should be deleted");
    }

    public static Category generateTabletsCategory() {
        Category tablets = new Category();
        tablets.setId(5);
        tablets.setName("Tablets");
        tablets.setAlias("tablets");
        tablets.setImage("tablets.png");

        return tablets;
    }

    public static Category generateComputersCategory() {
        Category computers = new Category();
        computers.setId(1);
        computers.setName("Computers");
        computers.setAlias("computers");
        computers.setImage("computers.png");

        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("<i>icon</i>");
        categoryIcon.setCategory(computers);

        computers.setCategoryIcon(categoryIcon);

        Category computerComponents = new Category();
        computerComponents.setId(2);
        computerComponents.setName("Computer Components");
        computerComponents.setAlias("computer_components");
        computerComponents.setImage("computer_components.png");

        Category hardDrives = new Category();
        hardDrives.setId(3);
        hardDrives.setName("Hard Drives");
        hardDrives.setAlias("hard_drives");
        hardDrives.setImage("hard_drives.png");

        Category memory = new Category();
        memory.setId(4);
        memory.setName("Memory");
        memory.setAlias("memory");
        memory.setImage("memory.png");

        computerComponents.addChild(hardDrives);
        computerComponents.addChild(memory);

        computers.addChild(computerComponents);

        return computers;
    }

    public static Category generateComputersCategoryWithoutId() {
        Category computers = new Category();
        computers.setName("Computers unique");
        computers.setAlias("computers_unique");
        computers.setImage("computers.png");

        CategoryIcon categoryIcon = new CategoryIcon();
        categoryIcon.setIconPath("<i>icon</i>");
        categoryIcon.setCategory(computers);

        computers.setCategoryIcon(categoryIcon);

        Category computerComponents = new Category();
        computerComponents.setName("Computer Components unique");
        computerComponents.setAlias("computer_components_unique");
        computerComponents.setImage("computer_components.png");

        Category hardDrives = new Category();
        hardDrives.setName("Hard Drives");
        hardDrives.setAlias("hard_drives");
        hardDrives.setImage("hard_drives.png");

        Category memory = new Category();
        memory.setName("Memory unique");
        memory.setAlias("memory_unique");
        memory.setImage("memory.png");

        computerComponents.addChild(hardDrives);
        computerComponents.addChild(memory);

        computers.addChild(computerComponents);

        return computers;
    }

    public static Category generateCategoryWithNoChildren() {
        Category category = new Category();
        category.setId(10);
        category.setName("CategoryWithNoChildren");
        category.setAlias("category_no_children");
        category.setImage("category.png");
        return category;
    }
}