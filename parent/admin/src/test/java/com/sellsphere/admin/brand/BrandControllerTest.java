package com.sellsphere.admin.brand;

import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BrandControllerTest {

    private static final String DEFAULT_REDIRECT_URL = "/brands/page/0?sortField=name&sortDir=asc";

    @MockBean
    private BrandService brandService;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenRootUrl_whenAccessed_thenRedirectToBrandList() throws Exception {
        mockMvc.perform(get("/brands"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));
    }

    @Test
    void givenNewBrandUrl_whenAccessed_thenDisplayCreateBrandForm() throws Exception {
        given(categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC)).willReturn(List.of());

        mockMvc.perform(get("/brands/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("brand/brand_form"))
                .andExpect(model().attributeExists("brand"))
                .andExpect(model().attribute("pageTitle", "Create New Brand"))
                .andExpect(model().attributeExists("categoryList"));
    }

    @Test
    void givenExistingBrandId_whenEditBrandUrlAccessed_thenDisplayEditBrandForm() throws Exception {
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Test Brand");
        List<Category> categories = List.of(new Category(1, "Electronics"));

        given(brandService.getBrandById(1)).willReturn(brand);
        given(categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC)).willReturn(categories);

        mockMvc.perform(get("/brands/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("brand/brand_form"))
                .andExpect(model().attribute("brand", brand))
                .andExpect(model().attribute("pageTitle", "Edit Brand [ID: 1]"))
                .andExpect(model().attributeExists("categoryList"));
    }

    @Test
    void givenValidBrandAndFile_whenSaveBrand_thenRedirectToBrandList() throws Exception {
        MockMultipartFile file = new MockMultipartFile("newImage", "test.png", MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Test Brand");

        willAnswer(invocation -> null).given(brandService).saveBrand(any(Brand.class), any());

        mockMvc.perform(multipart("/brands/save")
                                .file(file)
                                .flashAttr("brand", brand))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(brandService).should().saveBrand(any(Brand.class), any());
    }

    @Test
    void givenExistingBrandId_whenDeleteBrand_thenRedirectToBrandList() throws Exception {
        willDoNothing().given(brandService).deleteBrandById(1);

        mockMvc.perform(get("/brands/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(brandService).should().deleteBrandById(1);
    }

    @Test
    void givenValidExportFormat_whenExportBrands_thenReturnFile() throws Exception {
        mockMvc.perform(get("/brands/export/csv"))
                .andExpect(status().isOk());

        then(brandService).should().listAllBrands("name", Sort.Direction.ASC);
    }

    @Test
    void givenValidPageNumber_whenListBrandsByPage_thenReturnBrandsForSpecificPage() throws Exception {
        int pageNum = 1;
        String sortField = "name";
        String sortDir = "asc";
        String keyword = "electronics";

        willDoNothing().given(brandService).listBrandsByPage(eq(pageNum), any(PagingAndSortingHelper.class));

        mockMvc.perform(get("/brands/page/{pageNum}", pageNum)
                                .param("sortField", sortField)
                                .param("sortDir", sortDir)
                                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("brand/brands"));

        then(brandService).should().listBrandsByPage(eq(pageNum), any(PagingAndSortingHelper.class));
    }
}
