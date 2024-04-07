package com.sellsphere.admin.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

/**
 * Helper class that facilitates pagination and sorting logic for entities within the application.
 * It wraps common functionality needed across different views and controllers to apply consistent
 * pagination and sorting based on user input.
 */
@Getter
@Setter
@AllArgsConstructor
public class PagingAndSortingHelper {

    private ModelAndViewContainer model;

    private String listName;

    private String sortField;

    private String sortDir;

    private String keyword;

    /**
     * Lists entities using a repository with pagination and sorting applied. It also filters entities
     * based on a provided keyword if applicable.
     *
     * @param pageNum The page number to retrieve.
     * @param pageSize The size of the page to retrieve.
     * @param repo The repository used to retrieve entities.
     */
    public void listEntities(int pageNum, int pageSize,
                             SearchRepository<?, Integer> repo) {
        Pageable pageable = createPageable(pageSize, pageNum);
        Page<?> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = repo.findAll(keyword, pageable);
        } else {
            page = repo.findAll(pageable);
        }

        updateModelAttributes(pageNum, page);
    }

    /**
     * Updates model attributes for the current page, total pages, total items, and the list of entities.
     *
     * @param pageNum The current page number.
     * @param page The Page object containing entities and pagination information.
     */
    public void updateModelAttributes(int pageNum, Page<?> page) {
        updateModelAttributes(pageNum, page.getTotalPages(), page.getTotalElements(), page.getContent());
    }

    /**
     * Updates model attributes with pagination details and content list.
     *
     * @param pageNum The current page number.
     * @param totalPages The total number of pages available.
     * @param totalItems The total number of items across all pages.
     * @param content The list of entities for the current page.
     */
    public void updateModelAttributes(int pageNum, int totalPages,
                                      long totalItems, List<?> content) {
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages",totalPages);
        model.addAttribute("totalItems",totalItems);
        model.addAttribute(listName, content);
    }

    /**
     * Creates a Pageable object to define pagination and sorting configuration.
     *
     * @param pageSize The number of entities per page.
     * @param pageNum The current page number.
     * @return A Pageable object with the specified configurations.
     */
    public Pageable createPageable(int pageSize, int pageNum) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        return PageRequest.of(pageNum, pageSize, sort);
    }

    /**
     * Gets the keyword for filtering entities. If the keyword is empty, it returns null.
     *
     * @return The keyword if it's not empty; otherwise, null.
     */
    public String getKeyword() {
        return (keyword != null && keyword.isEmpty()) ? null : keyword;
    }
}
