package com.sellsphere.admin.brand;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class BrandService {

    private static final int BRANDS_PER_PAGE = 10;
    private static final String BRAND_PHOTOS_DIR = "brand-photos/";

    private final BrandRepository brandRepository;
    private final FileService fileService;

    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, BRANDS_PER_PAGE, brandRepository);
    }

    public Brand get(Integer id) throws BrandNotFoundException {
        return brandRepository.findById(id).orElseThrow(BrandNotFoundException::new);
    }

    public Brand save(Brand brand, MultipartFile file) throws IOException {
        removeLeadingDashFromCategories(brand);

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            brand.setLogo(fileName);

            Brand savedBrand = brandRepository.save(brand);

            String folderName = BRAND_PHOTOS_DIR + savedBrand.getId();
            fileService.saveSingleFile(file, folderName, fileName);
            return savedBrand;

        } else {
            return brandRepository.save(brand);
        }
    }

    // Removes leading dashes(hierarchy) from category names associated with the brand.
    private void removeLeadingDashFromCategories(Brand brand) {
        brand.getCategories().forEach(
                category -> category.setName(category.getName().replace("-", "")));
    }

}
