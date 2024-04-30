package com.sellsphere.client.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ProductRestControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnFilteredProductsWhenValidFiltersProvided() throws Exception {
        ProductPageResponse mockResponse = ProductPageResponse.builder().build();
        when(productService.listProductsPage(new ProductPageRequest())).thenReturn(mockResponse);

        mockMvc.perform(get("/products/filter/page")
                                .param("filter", "brand,apple")
                                .param("filter", "category,pc")
                                .param("category_alias", "someCategory")
                                .param("keyword", "someKeyword")
                                .param("pageNum", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).listProductsPage(any(ProductPageRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredParamsMissing() throws Exception {
        mockMvc.perform(get("/products/filter/page")
                                .param("filter", "brand,apple")
                                .param("pageNum", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
