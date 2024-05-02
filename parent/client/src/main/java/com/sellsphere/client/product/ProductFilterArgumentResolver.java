package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class ProductFilterArgumentResolver implements HandlerMethodArgumentResolver {

    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductFilterArgumentResolver(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(ProductFilter.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
            throws Exception {
        ProductPageRequest params = new ProductPageRequest();

        String[] filter = webRequest.getParameterValues("filter");
        String categoryAlias = webRequest.getParameter("category_alias");
        String keyword = webRequest.getParameter("keyword");
        String pageNumStr = webRequest.getParameter("pageNum");

        // Set parameters
        params.setFilter(filter);
        params.setCategoryAlias(categoryAlias);
        params.setKeyword(keyword);

        // Validate that pageNum is provided
        if(pageNumStr != null) {
            params.setPageNum(Integer.parseInt(pageNumStr));
        }

        // Validate that either categoryAlias or keyword is provided
        if ((categoryAlias == null || categoryAlias.isEmpty()) && (keyword == null || keyword.isEmpty())) {
            throw new IllegalArgumentException("Either categoryAlias or keyword must be provided");
        }

        if (categoryAlias != null) {
            Category category = categoryRepository.findByAliasAndEnabledIsTrue(
                    categoryAlias).orElseThrow(CategoryNotFoundException::new);
            params.setCategoryId(category.getId());
        }

        return params;
    }
}