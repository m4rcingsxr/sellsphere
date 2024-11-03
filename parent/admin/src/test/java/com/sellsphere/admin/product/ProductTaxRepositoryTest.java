package com.sellsphere.admin.product;

import com.sellsphere.common.entity.ProductTax;
import com.sellsphere.common.entity.TaxType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/product_taxes.sql")
class ProductTaxRepositoryTest {

    @Autowired
    public ProductTaxRepository productTaxRepository;


    @Test
    void givenTaxType_whenFindByType_thenReturnListOfProductTax() {
        List<ProductTax> taxes = productTaxRepository.findByType(TaxType.PHYSICAL);

        assertEquals(2, taxes.size());
    }

    @Test
    void givenTaxId_whenFindById_thenReturnProductTax() {
        Optional<ProductTax> tax = productTaxRepository.findById("txcd_99999999");

        assertTrue(tax.isPresent());
    }

}