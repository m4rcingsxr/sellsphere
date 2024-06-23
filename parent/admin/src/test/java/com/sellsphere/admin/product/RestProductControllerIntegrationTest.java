package com.sellsphere.admin.product;

import com.sellsphere.common.entity.ProductTax;
import com.sellsphere.common.entity.TaxType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RestProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductTaxRepository productTaxRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenProductName_whenCheckUniqueness_thenReturnResult() throws Exception {
        // Given
        Integer productId = 1;
        String productName = "com.sellsphere.admin.Test Product";

        when(productService.isNameUnique(productId, productName)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/products/check_uniqueness")
                                .param("id", String.valueOf(productId))
                                .param("name", productName)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenTaxType_whenFindByTaxType_thenReturnTaxes() throws Exception {

        // Given
        String taxType = "PHYSICAL";

        List<ProductTax> taxes = Arrays.asList(
                new ProductTax("Tax 1","1" , TaxType.PHYSICAL, "desc_1"),
                new ProductTax("Tax 2","2" , TaxType.PHYSICAL, "desc_2")
        );

        when(productTaxRepository.findByType(TaxType.valueOf(taxType))).thenReturn(taxes);

        // When & Then
        mockMvc.perform(get("/products/tax/{type}", taxType)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'id':'Tax 1','name':'1','type':'PHYSICAL'},{'id':'Tax 2','name':'2','type':'PHYSICAL'}]"));
    }

}