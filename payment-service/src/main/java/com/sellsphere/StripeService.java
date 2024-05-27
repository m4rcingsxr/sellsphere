package com.sellsphere;

import com.sellsphere.common.entity.Currency;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.param.*;

import java.math.BigDecimal;

public class StripeService {

    static {
        Stripe.apiKey =
                "sk_test_51PailoF45HI7vp7sJPtRQJn32yhMaPbqUITsFkNtbdKogwcUTeVvTcNXjMPHigCTXSqaSLfuWNuFR1KDGISGNlKa00a9EokJ6w";
    }

    public Product createProduct(com.sellsphere.common.entity.Product product, Currency currency)
            throws StripeException {
        ProductCreateParams params =
                ProductCreateParams.builder()
                        .setId(String.valueOf(product.getId()))
                        .setName(product.getName())
                        .setDefaultPriceData(
                                ProductCreateParams.DefaultPriceData.builder()
                                        .setUnitAmount(product.getPrice().multiply(
                                                BigDecimal.valueOf(
                                                        currency.getUnitAmount())).longValue())
                                        .setCurrency(currency.getCode())
                                        .build()
                        )
                        .addExpand("default_price")
                        .build();

        return Product.create(params);
    }

}
