package com.sellsphere.admin.article;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public class ListUtil {

    public static <T> List<T> prepareListWithNulls(List<T> itemList, int size, Function<T, Integer> indexFunction) {
        List<T> preparedList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            preparedList.add(null);
        }

        for (T item : itemList) {
            int index = indexFunction.apply(item);
            if (index >= 1 && index <= size) {
                preparedList.set(index - 1, item);
            }
        }

        return preparedList;
    }

}
