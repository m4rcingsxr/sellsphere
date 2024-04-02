package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
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

    public Brand get(Integer id) throws BrandNotFoundException {
        return brandRepository.findById(id).orElseThrow(
                BrandNotFoundException::new);
    }
}
