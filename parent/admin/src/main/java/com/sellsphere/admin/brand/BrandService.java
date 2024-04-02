package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BrandService {

    private static final int BRANDS_PER_PAGE = 10;

    private final BrandRepository brandRepository;

    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, BRANDS_PER_PAGE, brandRepository);
    }

}
