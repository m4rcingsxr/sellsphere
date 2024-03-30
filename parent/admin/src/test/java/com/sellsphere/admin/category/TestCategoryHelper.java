package com.sellsphere.admin.category;

import com.sellsphere.common.entity.Category;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class TestCategoryHelper {

    public static void assertRootCategoriesSortedByName(List<Category> categories) {
        // Ensure the root categories are sorted by name
        List<String> rootNames = categories.stream().filter(
                category -> category.getParent() == null).map(Category::getName).collect(
                Collectors.toList());

        List<String> sortedRootNames = rootNames.stream().sorted().collect(Collectors.toList());

        assertEquals(sortedRootNames, rootNames, "Root categories should be sorted by name");
    }

    public static void assertHierarchy(Category copy, List<Category> rootCategories) {
        String hierarchicalName = rootCategories.stream().map(
                root -> getHierarchicalName(root, copy.getId(), 0)).filter(
                Objects::nonNull).findFirst().orElse(null);

        assertNotNull(hierarchicalName, "Hierarchical name should not be null");
        assertEquals(hierarchicalName, copy.getName(),
                     "Names should match for category ID: " + copy.getId()
        );

        // Check if children are sorted by name
        List<Category> children = copy.getChildren().stream().sorted(
                (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName())).collect(
                Collectors.toList());

        List<Category> sortedChildren = copy.getChildren().stream().sorted(
                (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName())).collect(
                Collectors.toList());

        assertEquals(sortedChildren, children,
                     "Children should be sorted by name for category ID: " + copy.getId()
        );
    }

    private static String getHierarchicalName(Category current, Integer expectedId, int level) {
        if (current.getId().equals(expectedId)) {
            // assert that original is unchanged
            assertFalse(current.getName().contains("-"));
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

    public List<Category> generateRootCategories() {
        Category computers = new Category();
        computers.setId(1);
        computers.setName("Computers");
        computers.setAlias("computers");
        computers.setImage("computers.png");


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

        Category tablets = new Category();
        tablets.setId(5);
        tablets.setName("Tablets");
        tablets.setAlias("tablets");
        tablets.setImage("tablets.png");

        return List.of(computers, tablets);
    }
}