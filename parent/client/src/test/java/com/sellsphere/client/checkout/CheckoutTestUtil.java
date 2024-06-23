package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.AddressDTO;
import com.sellsphere.common.entity.payload.CalculationRequestDTO;
import com.stripe.model.tax.Calculation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckoutTestUtil {

    public static Customer generateDummyCustomer() {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setEmail("example@gmail.com");
        customer.setStripeId("cus_123");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        return customer;
    }

    public static CalculationRequestDTO generateDummyCalculationRequestDTO(BigDecimal shippingCost, String currency, BigDecimal exchangeRate) {
        return CalculationRequestDTO.builder()
                .address(createAddressDTO())
                .shippingCost(BigDecimal.TEN)
                .currencyCode(currency)
                .exchangeRate(exchangeRate)
                .build();
    }

    public static CalculationRequestDTO generateDummyCalculationRequestDTO() {
        return CalculationRequestDTO.builder()
                .address(createAddressDTO())
                .shippingCost(BigDecimal.TEN)
                .build();
    }

    public static Calculation generateDummyCalculation() {
        Calculation calculation = new Calculation();

        calculation.setId("calc_123");
        calculation.setAmountTotal(10000L);
        calculation.setTaxAmountInclusive(2300L);
        calculation.setCurrency("eur");

        Calculation.ShippingCost shippingCost = new Calculation.ShippingCost();
        shippingCost.setAmount(1000L);
        shippingCost.setAmountTax(230L);
        calculation.setShippingCost(shippingCost);

        return calculation;
    }

    public static Calculation generateDummyCalculation(long amountTotal, long taxAmount, long shippingCostAmount, long taxShipping, String currencyCode) {
        Calculation calculation = new Calculation();

        calculation.setId("calc_123");
        calculation.setAmountTotal(amountTotal);
        calculation.setTaxAmountInclusive(taxAmount);
        calculation.setCurrency(currencyCode);

        Calculation.ShippingCost shippingCost = new Calculation.ShippingCost();
        shippingCost.setAmount(shippingCostAmount);
        shippingCost.setAmountTax(taxShipping);
        calculation.setShippingCost(shippingCost);

        return calculation;
    }

    public static List<CartItem> generateDummyCartItems() {
        List<CartItem> cartItems = new ArrayList<>();

        // Create a ProductTax object
        ProductTax productTax = new ProductTax();
        productTax.setId("tax_123");
        productTax.setName("Standard Tax");
        productTax.setType(TaxType.PHYSICAL);
        productTax.setDescription("Standard sales tax for products.");

        // Create a Category object
        Category category = new Category();
        category.setId(1);
        category.setName("Category 1");
        category.setAlias("category-1");
        category.setImage("category1.jpg");
        category.setEnabled(true);

        // Create a Brand object
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Brand 1");
        brand.setLogo("brand1.jpg");

        // Create a Product object
        Product product = new Product();
        product.setId(1);
        product.setName("Product 1");
        product.setAlias("product-1");
        product.setShortDescription("Short description of product 1.");
        product.setFullDescription("Detailed description of product 1.");
        product.setCreatedTime(LocalDateTime.now());
        product.setEnabled(true);
        product.setInStock(true);
        product.setCost(BigDecimal.valueOf(5.00));
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setDiscountPercent(BigDecimal.ZERO);
        product.setLength(BigDecimal.valueOf(10.0));
        product.setWidth(BigDecimal.valueOf(5.0));
        product.setHeight(BigDecimal.valueOf(3.0));
        product.setWeight(BigDecimal.valueOf(1.0));
        product.setMainImage("product1.jpg");
        product.setCategory(category);
        product.setBrand(brand);
        product.setTax(productTax);
        product.setContainsBatteryPi966(false);
        product.setContainsBatteryPi967(false);
        product.setContainsLiquids(false);
        product.setHsCode("123456");

        // Create a CartItem object
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(1);
        cartItem.setProduct(product);
        cartItem.setCustomer(generateDummyCustomer());

        // Add the CartItem to the list
        cartItems.add(cartItem);

        return cartItems;
    }

    public static Currency generateDummyEurCurrency() {
        Currency currency = new Currency();
        currency.setId(1);
        currency.setName("Euro");
        currency.setSymbol("E");
        currency.setCode("eur");
        currency.setUnitAmount(BigDecimal.valueOf(100)); // 1 unit equals 100 cents

        return currency;
    }

    public static Currency generateDummyKwdCurrency() {
        Currency currency = new Currency();
        currency.setId(2);
        currency.setName("Kuwaiti dinar");
        currency.setSymbol("kd");
        currency.setCode("kwd");
        currency.setUnitAmount(BigDecimal.valueOf(1000)); // 1 unit equals 100 cents

        return currency;
    }


    public static Currency generateDummyJpyCurrency() {
        Currency currency = new Currency();
        currency.setId(3);
        currency.setName("Japanese Yen");
        currency.setSymbol("¥");
        currency.setCode("jpy");
        currency.setUnitAmount(BigDecimal.valueOf(1)); // 1 unit equals 100 cents

        return currency;
    }





    public static PaymentIntent createTransaction(Currency baseCurrency, Currency targetCurrency, Address address, Customer customer, Courier courier) {
        return PaymentIntent.builder()
                .shippingAddress(address)
                .amount(10000L)
                .stripeId("pi_123")
                .shippingAmount(100L)
                .exchangeRate(new BigDecimal("4.323212"))
                .taxAmount(230L)
                .shippingTax(23L)
                .targetCurrency(targetCurrency)
                .baseCurrency(baseCurrency)
                .customer(customer)
                .courier(courier)
                .created(Instant.now().toEpochMilli())
                .status("succeeded")
                .build();
    }

    private static AddressDTO createAddressDTO() {
        return AddressDTO.builder()
                .countryCode("US")
                .addressLine1("123 Main St")
                .postalCode("10001")
                .city("New York")
                .build();
    }


}