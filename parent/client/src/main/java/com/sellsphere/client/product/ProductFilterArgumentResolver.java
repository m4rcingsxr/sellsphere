package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;

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
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        assert servletRequest != null;

        String[] filter = webRequest.getParameterValues("filter");
        String categoryAlias = webRequest.getParameter("category_alias");
        String keyword = webRequest.getParameter("keyword");
        String pageNumStr = webRequest.getParameter("pageNum");
        String minPrice = webRequest.getParameter("minPrice");
        String maxPrice = webRequest.getParameter("maxPrice");
        String sortBy = webRequest.getParameter("sortBy");

        String endpoint = servletRequest.getRequestURL().toString();

        if (endpoint.contains("products")) {
            ProductPageRequest pageRequest = new ProductPageRequest();

            pageRequest.setFilter(filter);
            pageRequest.setCategoryAlias(categoryAlias);
            pageRequest.setKeyword(keyword);

            if (categoryAlias != null) {
                pageRequest.setCategoryId(getCategoryId(categoryAlias));
            }
            pageRequest.setMinPrice(minPrice != null ? new BigDecimal(minPrice) : null);
            pageRequest.setMaxPrice(maxPrice != null ? new BigDecimal(maxPrice) : null);

            pageRequest.setPageNum(pageNumStr != null ? Integer.parseInt(pageNumStr) : null);
            pageRequest.setSortBy(sortBy);

            return pageRequest;
        }

        if (endpoint.contains("filter_counts")) {
            FilterMapCountRequest mapRequest = new FilterMapCountRequest();

            mapRequest.setFilter(filter);
            mapRequest.setCategoryAlias(categoryAlias);
            mapRequest.setKeyword(keyword);

            if (categoryAlias != null) {
                mapRequest.setCategoryId(getCategoryId(categoryAlias));
            }

            mapRequest.setMinPrice(minPrice != null ? new BigDecimal(minPrice) : null);
            mapRequest.setMaxPrice(maxPrice != null ? new BigDecimal(maxPrice) : null);

            return mapRequest;
        }

        return null;
    }

    private Integer getCategoryId(String categoryAlias) throws CategoryNotFoundException {
        return categoryRepository.findByAliasAndEnabledIsTrue(categoryAlias).orElseThrow(
                CategoryNotFoundException::new).getId();
    }


}