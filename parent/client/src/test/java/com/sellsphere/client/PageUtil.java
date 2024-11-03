package com.sellsphere.client;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;

@UtilityClass
public class PageUtil {

    public static Sort generateSort(String sortField, Sort.Direction sortDir) {
        return Sort.by(sortDir, sortField);
    }

    public static <T> void assertSortOrder(List<T> list, Comparator<T> comparator) {
        for (int i = 0; i < list.size() - 1; i++) {
            T current = list.get(i);
            T next = list.get(i + 1);
            if (comparator.compare(current, next) > 0) {
                throw new AssertionError("List is not sorted correctly. Element at index " + i + " is greater than element at index " + (i + 1));
            }
        }
    }

}
