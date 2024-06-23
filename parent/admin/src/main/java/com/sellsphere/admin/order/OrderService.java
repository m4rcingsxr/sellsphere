package com.sellsphere.admin.order;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Integer ORDERS_PER_PAGE = 10;

    private final OrderRepository repository;


    public void listPage(Integer page, PagingAndSortingHelper helper) {
        helper.listEntities(page, ORDERS_PER_PAGE, repository);
    }
}
