package com.sellsphere.admin.page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure paging and sorting parameters for controller methods in Spring MVC applications.
 * It allows the specification of a module URL and a list name which are used by {@link PagingAndSortingHelper}
 * to manage paging and sorting in a standardized way across different modules of the application.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PagingAndSortingParam {

    /**
     * Specifies the base URL for the module. This URL is used for generating links for paging and sorting operations.
     * @return The base module URL as a String.
     */
    String moduleURL() default "";

    /**
     * Specifies the name of the list that will be used in the model. This name is used to identify the list of entities
     * within the model, facilitating the retrieval and display of the list in the view layer.
     * @return The name of the list as a String.
     */
    String listName();

}
