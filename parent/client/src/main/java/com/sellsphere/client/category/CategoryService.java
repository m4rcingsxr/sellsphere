package com.sellsphere.client.category;

import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> listRootCategories() {
        return categoryRepository.findAllByParentIsNull();
    }

    public Category getCategoryByAlias(String alias) throws CategoryNotFoundException {
        return categoryRepository
                .findByAliasAndEnabledIsTrue(alias)
                .orElseThrow(CategoryNotFoundException::new);
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public List<Category> getCategoryParents(Category child) {
        List<Category> categoryParents = new ArrayList<>();

        Category parent = child.getParent();

        while(parent != null) {
            categoryParents.add(0, parent);
            parent = parent.getParent();
        }

        categoryParents.add(child);

        return categoryParents;
    }

}
