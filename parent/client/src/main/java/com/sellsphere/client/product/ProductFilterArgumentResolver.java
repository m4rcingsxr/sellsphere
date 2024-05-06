package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.CategoryNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class ProductFilterArgumentResolver implements HandlerMethodArgumentResolver {

    private final CategoryRepository categoryRepository;
    private final Validator validator;

    @Autowired
    public ProductFilterArgumentResolver(CategoryRepository categoryRepository, Validator validator) {
        this.categoryRepository = categoryRepository;
        this.validator = validator;
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

            Set<ConstraintViolation<ProductPageRequest>> violations = validator.validate(pageRequest);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

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

            Set<ConstraintViolation<FilterMapCountRequest>> violations = validator.validate(mapRequest);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return mapRequest;
        }

        throw new IllegalArgumentException("Not supported endpoint: " + endpoint);
    }

    private Integer getCategoryId(String categoryAlias) throws CategoryNotFoundException {
        return categoryRepository.findByAliasAndEnabledIsTrue(categoryAlias).orElseThrow(
                CategoryNotFoundException::new).getId();
    }


}