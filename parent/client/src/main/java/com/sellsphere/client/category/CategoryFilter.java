package com.sellsphere.client.category;

import com.sellsphere.common.entity.Category;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CategoryFilter responsible for loading categories.
 * CategoryList is required on every client view as part of a navigation.
 */
@Component
@RequiredArgsConstructor
public class CategoryFilter extends GenericFilter {

    private final CategoryService categoryService;

    private static final String[] RESOURCE_EXTENSION = {".css", ".js", ".png"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        boolean allowedUrl = isRequestForResource(servletRequest);

        if (!allowedUrl) {
            loadRootCategories(servletRequest);
        }

        chain.doFilter(request, response);
    }

    private void loadRootCategories(HttpServletRequest servletRequest) {
        List<Category> categoryList = categoryService.listRootCategories().stream()
                .sorted(Comparator.comparingInt((Category category) -> category.getChildren().size()).reversed())
                .peek(this::sortSubCategoriesByChildrenSize)
                .toList();

        servletRequest.setAttribute("categoryList", categoryList);
    }

    private void sortSubCategoriesByChildrenSize(Category category) {
        Set<Category> sortedChildren = category.getChildren().stream()
                .sorted(Comparator.comparingInt((Category subCategory) -> subCategory.getChildren().size()).reversed())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        category.setChildren(sortedChildren);
    }

    private boolean isRequestForResource(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        for (String allowedExtension : RESOURCE_EXTENSION) {
            if (url.endsWith(allowedExtension)) {
                return true;
            }
        }

        return false;
    }

}
