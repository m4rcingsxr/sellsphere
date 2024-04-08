package com.sellsphere.admin.product;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

    private static final int PRODUCTS_PER_PAGE = 12;

    private final ProductRepository productRepository;

    /**
     * Facilitates pagination for listing products
     *
     * @param pageNum the current page number
     * @param helper a helper class for pagination and sorting
     */
    public void listPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, PRODUCTS_PER_PAGE, productRepository);
    }

}
