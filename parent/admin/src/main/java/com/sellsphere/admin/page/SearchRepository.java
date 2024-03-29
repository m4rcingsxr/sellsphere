package com.sellsphere.admin.page;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * A base repository interface extending {@link JpaRepository} to support custom search operations.
 * This interface is used for implementing repository methods that support searching entities by a keyword and pagination.
 * Marked with @NoRepositoryBean to indicate that Spring should not create instances of this repository interface directly.
 * Implementations should extend this interface to provide entity-specific search functionality.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
@NoRepositoryBean
public interface SearchRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Finds all entities that match the given keyword and applies pagination and sorting.
     * Implementations should provide the logic to filter entities by the keyword across relevant fields.
     *
     * @param keyword the search keyword to filter entities
     * @param pageable the pagination and sorting information
     * @return a {@link Page} of entities that match the keyword, contained in the pageable object
     */
    Page<T> findAll(String keyword, Pageable pageable);

}
