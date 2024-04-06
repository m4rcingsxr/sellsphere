package com.sellsphere.admin;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Utility class for managing pagination and sorting.
 */
@UtilityClass
public class PagingHelper {

    /**
     * Creates a PageRequest object with the specified parameters.
     *
     * @param pageNum the page number
     * @param itemsPerPage the number of items per page
     * @param sortField the field to sort by
     * @param sortDirection the direction to sort (asc/desc)
     * @return the PageRequest object
     */
    public static PageRequest getPageRequest(Integer pageNum, int itemsPerPage, String sortField, String sortDirection) {
        return PageRequest.of(pageNum, itemsPerPage, getSort(sortField, sortDirection));
    }

    /**
     * Creates a Sort object with the specified parameters.
     *
     * @param sortField the field to sort by
     * @param sortDirection the direction to sort (asc/desc)
     * @return the Sort object
     */
    public static Sort getSort(String sortField, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return Sort.by(direction, sortField);
    }
}
