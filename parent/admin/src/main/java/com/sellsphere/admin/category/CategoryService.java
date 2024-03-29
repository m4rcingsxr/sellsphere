package com.sellsphere.admin.category;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public static final int CATEGORY_PER_PAGE = 5;

    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        Pageable pageable = helper.createPageable(CATEGORY_PER_PAGE, pageNum);
        String searchKeyword = helper.getKeyword();

        if (searchKeyword == null) {
            listRootCategories(pageable, pageNum, helper);
        } else {
            helper.listEntities(pageNum, CATEGORY_PER_PAGE, categoryRepository);
        }
    }

    private void listRootCategories(Pageable pageable, Integer pageNum,
                                    PagingAndSortingHelper helper) {
        Page<Category> page = categoryRepository.findAllByParentIsNull(pageable);

        // Converts the list of root category to a hierarchical list
        // including all descendants.
        List<Category> hierarchicalCategories = createHierarchy(page.getContent());

        helper.updateModelAttributes(pageNum, page.getTotalPages(),
                                     page.getTotalElements(),
                                     hierarchicalCategories
        );
    }

    private List<Category> createHierarchy(List<Category> categoryList) {
        return categoryList.stream()
                .filter(category -> category.getParent() == null)
                .flatMap(category -> expandHierarchy(category, 0))
                .toList();
    }

    private Stream<Category> expandHierarchy(Category category, int level) {

        // Adjusts the category's name based on its level in the hierarchy
        // and includes all its descendants. Create copy of category to not mess
        // with persistence context
        Category categoryCopy = new Category(category);
        categoryCopy.setName(prefixedName(category.getName(), level));

        // Recursively processes children, incrementing the level to adjust
        // their names accordingly.
        return Stream.concat(Stream.of(categoryCopy),
                             categoryCopy.getChildren().stream().flatMap(
                                     child -> expandHierarchy(child, level + 1))
        );
    }

    private String prefixedName(String name, int level) {

        // Prefixes the category's name with dashes ("-") corresponding to
        // its depth level in the hierarchy.
        return "-".repeat(level) + name;
    }
}
