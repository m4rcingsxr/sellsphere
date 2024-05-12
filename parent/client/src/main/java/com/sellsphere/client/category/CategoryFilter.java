package com.sellsphere.client.category;

import com.sellsphere.client.util.FilterUtil;
import com.sellsphere.common.entity.Category;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * CategoryFilter responsible for loading categories.
 * CategoryList is required on every client view as part of a navigation.
 */
@Component
@RequiredArgsConstructor
public class CategoryFilter extends GenericFilter {

    private final CategoryService categoryService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        boolean allowedUrl = FilterUtil.isRequestForResource(servletRequest);

        if (!allowedUrl) {
            List<Category> categoryList = categoryService.listRootCategories();
            request.setAttribute("categoryList", categoryList);
        }

        chain.doFilter(request, response);
    }

}
