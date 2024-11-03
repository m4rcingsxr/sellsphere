package com.sellsphere.admin.page;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

/**
 * Utility class for managing pagination and sorting.
 */
@UtilityClass
public class PagingHelper {
    /**
     * Creates a Sort object with the specified parameters.
     *
     * @param sortField the field to sort by
     * @param sortDirection the direction to sort (asc/desc)
     * @return the Sort object
     */
    public static Sort getSort(String sortField, Sort.Direction sortDirection) {
        return Sort.by(sortDirection, sortField);
    }

}
