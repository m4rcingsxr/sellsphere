package com.sellsphere.admin.page;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Custom argument resolver for handling paging and sorting parameters in Spring MVC.
 * This class automatically resolves paging and sorting parameters from HTTP requests and
 * constructs a {@link PagingAndSortingHelper} object, which is then passed to controller methods
 * as an argument. It leverages the {@link PagingAndSortingParam} annotation to identify controller
 * method parameters that should be populated with paging and sorting information.
 */
public class PagingAndSortingArgumentResolver implements
        HandlerMethodArgumentResolver {

    /**
     * Determines whether the parameter should be processed by this resolver. This method checks
     * if the parameter is annotated with {@link PagingAndSortingParam}.
     *
     * @param parameter the method parameter to check
     * @return true if the parameter is annotated with {@link PagingAndSortingParam}; false otherwise
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(PagingAndSortingParam.class) != null;
    }

    /**
     * Resolves method parameters into an argument value. Specifically, it extracts paging and sorting
     * parameters from the request, such as sort direction, sort field, and keyword, and uses them to
     * instantiate a {@link PagingAndSortingHelper} object. This helper object is then passed to the
     * controller method.
     *
     * @param parameter the method parameter to resolve. This parameter must be annotated with
     *                  {@link PagingAndSortingParam}.
     * @param model the current model of the request being processed
     * @param request the current request
     * @param binderFactory a factory for creating {@link WebDataBinder} instances
     * @return an instance of {@link PagingAndSortingHelper} populated with the resolved paging and sorting
     *         parameters
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer model,
                                  NativeWebRequest request,
                                  WebDataBinderFactory binderFactory) {
        PagingAndSortingParam annotation = parameter.getParameterAnnotation(PagingAndSortingParam.class);

        String sortDir = request.getParameter("sortDir");
        String sortField = request.getParameter("sortField");
        String keyword = request.getParameter("keyword");

        assert sortDir != null;
        String reversedSortDir = sortDir.equals("asc") ? "desc" : "asc";

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reversedSortDir", reversedSortDir);
        model.addAttribute("keyword", keyword);

        assert annotation != null;
        model.addAttribute("moduleURL", annotation.moduleURL());

        return new PagingAndSortingHelper(model, annotation.listName(), sortField, sortDir, keyword);
    }

}
