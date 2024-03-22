package com.sellsphere.admin;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PagingHelper {

    public static PageRequest getPageRequest(Integer pageNum, int usersPerPage, String sortField,
                                             String sortDirection) {
        return PageRequest.of(pageNum, usersPerPage, getSort(sortField, sortDirection));
    }

    public static Sort getSort(String sortField, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return Sort.by(direction, sortField);
    }
}
