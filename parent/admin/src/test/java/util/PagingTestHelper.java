package util;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility class for helping with paging and sorting assertions in tests.
 */
@UtilityClass
public class PagingTestHelper {

    public static <T> void assertPagingResults(PagingAndSortingHelper helper, int expectedPages,
                                               int expectedTotalItems, int expectedContentSize,
                                               String sortProperty, boolean ascending) {
        String listName = helper.getListName();
        List<T> entities = (List<T>) helper.getModel().getModel().get(listName);

        Integer totalPages = (Integer) helper.getModel().getModel().get("totalPages");
        Long totalItems = (Long) helper.getModel().getModel().get("totalItems");

        assertNotNull(entities, "Content should not be null");
        assertEquals(expectedContentSize, entities.size(),
                     "Page content should be size " + expectedContentSize
        );
        assertEquals(expectedPages, totalPages, "Total pages should be size " + expectedPages);
        assertEquals(expectedTotalItems, totalItems,
                     "Total elements should be " + expectedTotalItems
        );
        assertSorting(entities, sortProperty, ascending);
    }

    /**
     * Asserts the paging results for a given page.
     *
     * @param page                  the page to check
     * @param expectedContentSize   the expected size of the page content
     * @param expectedPages         the expected number of total pages
     * @param expectedTotalElements the expected total number of elements
     * @param sortProperty          the property used for sorting
     * @param ascending             true if sorting should be in ascending order, false if
     *                              descending
     * @param <T>                   the type of the elements in the page
     */
    public static <T> void assertPagingResults(Page<T> page, int expectedContentSize,
                                               int expectedPages, int expectedTotalElements,
                                               String sortProperty, boolean ascending) {
        assertNotNull(page, "Page should not be null");
        assertEquals(expectedContentSize, page.getContent().size(),
                     "Page content should be size " + expectedContentSize
        );
        assertEquals(expectedPages, page.getTotalPages(),
                     "Total pages should be size " + expectedPages
        );
        assertEquals(expectedTotalElements, page.getTotalElements(),
                     "Total elements should be " + expectedTotalElements
        );

        assertSorting(page.getContent(), sortProperty, ascending);
    }

    public static <T> void assertSorting(List<T> entities, String sortProperty, boolean ascending) {
        List<Comparable> sortProperties = entities.stream().map(entity -> {
            try {
                Method getter = entity.getClass().getMethod("get" + capitalize(sortProperty));
                return (Comparable) getter.invoke(entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access sort property", e);
            }
        }).toList();

        boolean isSortedCorrectly = IntStream.range(0, sortProperties.size() - 1).allMatch(i -> {
            Comparable first = sortProperties.get(i);
            Comparable second = sortProperties.get(i + 1);
            return ascending ? first.compareTo(second) <= 0 : first.compareTo(second) >= 0;
        });

        assertTrue(isSortedCorrectly);
    }

    /**
     * Creates a PageRequest object for pagination and sorting.
     *
     * @param page          the page number (zero-based)
     * @param size          the size of the page
     * @param sortField     the field to sort by
     * @param sortDirection the direction of the sort ("ASC" or "DESC")
     * @return the PageRequest object
     */
    public static PageRequest createPageRequest(int page, int size, String sortField,
                                                Sort.Direction sortDirection) {
        Sort sort = createSort(sortField, sortDirection);
        return PageRequest.of(page, size, sort);
    }


    public static Sort createSort(String sortField, Sort.Direction sortDirection) {
        return Sort.by(sortDirection, sortField);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}