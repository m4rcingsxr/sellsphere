package com.sellsphere.admin;

import com.sellsphere.admin.customer.CustomerRepository;
import com.sellsphere.admin.product.ProductRepository;
import com.sellsphere.easyship.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = {
        "classpath:sql/categories.sql", "classpath:sql/brands.sql", "classpath:sql/customers.sql", "classpath:sql/products.sql"})
public class Test {

    @Autowired
    private ApiService apiService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;

//    @org.junit.jupiter.api.Test
//    public void test() {
//        Customer byId = customerRepository.findById(1).orElseThrow(RuntimeException::new);
//        Product product1 = productRepository.findById(1).orElseThrow(RuntimeException::new);
//        Product product2 = productRepository.findById(2).orElseThrow(RuntimeException::new);
//
//        Address address = byId.getAddresses().get(0);
//
//        RatesResponse rates = apiService.getRates(AddressDtoMin.builder()
//                                                          .state(address.getState())
//                                                          .line2(address.getAddressLine2())
//                                                          .line1(address.getAddressLine1())
//                                                          .city(address.getCity())
//                                                          .countryAlpha2(address.getCountry().getCode())
//                                                          .postalCode(address.getPostalCode())
//                                                   .build(),
//                                            List.of(new CartItem(byId, product1, 2),
//                                                   new CartItem(byId, product2, 4)
//                                           )
//        );
//
//        System.out.println(rates);
//
//    }

}

