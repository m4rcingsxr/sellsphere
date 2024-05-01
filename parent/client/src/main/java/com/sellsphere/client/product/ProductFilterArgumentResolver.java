package com.sellsphere.client.product;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class ProductFilterArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(ProductFilter.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
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
        if (pageNumStr == null) {
            throw new IllegalArgumentException("pageNum must be provided");
        }
        params.setPageNum(Integer.parseInt(pageNumStr));

        // Validate that either categoryAlias or keyword is provided
        if ((categoryAlias == null || categoryAlias.isEmpty()) && (keyword == null || keyword.isEmpty())) {
            throw new IllegalArgumentException("Either categoryAlias or keyword must be provided");
        }

        return params;
    }
}