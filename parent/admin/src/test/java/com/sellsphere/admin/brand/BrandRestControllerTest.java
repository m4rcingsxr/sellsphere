package com.sellsphere.admin.brand;

import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BrandRestControllerTest {

    @MockBean
    private BrandService brandService;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidBrandAliasAndId_whenCheckBrandAliasUniqueness_thenReturnTrue() throws Exception {
        given(brandService.isBrandNameUnique(anyInt(), anyString())).willReturn(true);

        mockMvc.perform(post("/brands/check-uniqueness")
                                .param("id", "1")
                                .param("name", "UniqueBrand")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(brandService).should().isBrandNameUnique(1, "UniqueBrand");
    }

    @Test
    void givenValidBrandAliasWithoutId_whenCheckBrandAliasUniqueness_thenReturnTrue() throws Exception {
        given(brandService.isBrandNameUnique(null, "UniqueBrand")).willReturn(true);

        mockMvc.perform(post("/brands/check-uniqueness")
                                .param("name", "UniqueBrand")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(brandService).should().isBrandNameUnique(null, "UniqueBrand");
    }

    @Test
    void givenNonUniqueBrandAlias_whenCheckBrandAliasUniqueness_thenReturnFalse() throws Exception {
        given(brandService.isBrandNameUnique(anyInt(), anyString())).willReturn(false);

        mockMvc.perform(post("/brands/check-uniqueness")
                                .param("id", "1")
                                .param("name", "ExistingBrand")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(brandService).should().isBrandNameUnique(1, "ExistingBrand");
    }

    @Test
    void givenInvalidBrandAliasLength_whenCheckBrandAliasUniqueness_thenThrowIllegalArgumentException() throws Exception {
        mockMvc.perform(post("/brands/check-uniqueness")
                                .param("name", "a".repeat(46))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidBrandId_whenGetBrandCategoryHierarchy_thenReturnCategoryHierarchy() throws Exception {
        Brand brand = new Brand();
        brand.setId(1);
        List<Category> hierarchy = List.of(new Category(1, "Electronics"));

        given(brandService.getBrandById(1)).willReturn(brand);
        given(categoryService.createHierarchy(anyList())).willReturn(hierarchy);

        mockMvc.perform(get("/brands/{brandId}/categories", 1)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        then(brandService).should().getBrandById(1);
        then(categoryService).should().createHierarchy(anyList());
    }

    @Test
    void givenNonExistentBrandId_whenGetBrandCategoryHierarchy_thenThrowBrandNotFoundException() throws Exception {
        given(brandService.getBrandById(anyInt())).willThrow(new BrandNotFoundException("Brand not found"));

        mockMvc.perform(get("/brands/{brandId}/categories", 999)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(brandService).should().getBrandById(999);
    }

    @Test
    void whenFetchAllBrands_thenReturnAllBrands() throws Exception {
        Brand brand1 = new Brand();
        brand1.setName("Brand1");
        Brand brand2 = new Brand();
        brand2.setName("Brand2");
        List<Brand> brandList = List.of(brand1, brand2);

        given(brandService.listAllBrands(anyString(), any(Sort.Direction.class))).willReturn(brandList);

        mockMvc.perform(get("/brands/fetch-all")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Brand1"))
                .andExpect(jsonPath("$[1].name").value("Brand2"));

        then(brandService).should().listAllBrands("name", Sort.Direction.ASC);
    }
}
