package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryRepository;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.payload.BasicProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductFilterRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryRepository categoryRepository;

    @Test
    void givenProductPageRequest_whenPageFilteredProducts_thenReturnProductPageResponse() throws Exception {
        Category category = new Category();
        category.setId(1);
        when(categoryRepository.findByAliasAndEnabledIsTrue("category")).thenReturn(Optional.of(category));

        BasicProductDTO product1 = new BasicProductDTO();
        product1.setId(1);
        product1.setName("Product1");
        BasicProductDTO product2 = new BasicProductDTO();
        product2.setId(2);
        product2.setName("Product2");

        ProductPageResponse pageResponse = ProductPageResponse.builder()
                .page(0)
                .totalElements(2L)
                .totalPages(1)
                .minPrice(BigDecimal.valueOf(180.00).setScale(2))
                .maxPrice(BigDecimal.valueOf(270.00).setScale(2))
                .content(List.of(product1, product2))
                .build();

        when(productService.listProducts(any(ProductPageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/filter/products")
                                .param("category_alias", "category")
                                .param("filter", "Color,Red", "Size,M")
                                .param("sortBy", "LOWEST")
                                .param("pageNum", "0")
                                .param("minPrice", "100")
                                .param("maxPrice", "1000")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.minPrice").value(180.00))
                .andExpect(jsonPath("$.maxPrice").value(270.00))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Product1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Product2"));
    }

    @Test
    void givenFilterMapCountRequest_whenGetFilterMapCount_thenReturnFilterCounts() throws Exception {
        FilterMapCountRequest mapRequest = new FilterMapCountRequest();
        mapRequest.setKeyword("keyword");
        mapRequest.setFilter(new String[]{"Color,Red", "Size,M"});
        mapRequest.setMinPrice(BigDecimal.valueOf(100));
        mapRequest.setMaxPrice(BigDecimal.valueOf(1000));

        Map<String, Map<String, Long>> filterCounts = getMockFilterCountResponse();

        when(productService.calculateAllFilterCounts(any(FilterMapCountRequest.class))).thenReturn(filterCounts);

        mockMvc.perform(get("/filter/filter_counts")
                                .param("keyword", "keyword")
                                .param("filter", "Color,Red", "Size,M")
                                .param("minPrice", "100")
                                .param("maxPrice", "1000")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.Color.Red").value(2))
                .andExpect(jsonPath("$.Color.Blue").value(1))
                .andExpect(jsonPath("$.Size.M").value(2))
                .andExpect(jsonPath("$.Size.L").value(1))
                .andExpect(jsonPath("$.Brand.Brand1").value(2))
                .andExpect(jsonPath("$.Brand.Brand2").value(1));
    }

    private static Map<String, Map<String, Long>> getMockFilterCountResponse() {
        Map<String, Map<String, Long>> filterCounts = new HashMap<>();
        Map<String, Long> colorCounts = new HashMap<>();
        colorCounts.put("Red", 2L);
        colorCounts.put("Blue", 1L);
        filterCounts.put("Color", colorCounts);

        Map<String, Long> sizeCounts = new HashMap<>();
        sizeCounts.put("M", 2L);
        sizeCounts.put("L", 1L);
        filterCounts.put("Size", sizeCounts);

        Map<String, Long> brandCounts = new HashMap<>();
        brandCounts.put("Brand1", 2L);
        brandCounts.put("Brand2", 1L);
        filterCounts.put("Brand", brandCounts);
        return filterCounts;
    }

}
