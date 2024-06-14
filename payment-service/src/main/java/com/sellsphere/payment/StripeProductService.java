package com.sellsphere.payment;

import com.sellsphere.common.entity.Currency;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceUpdateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * The {@code StripeProductService} class provides methods for interacting with Stripe's API
 * to create and update products and their prices.
 * <p>
 * This class is responsible for creating new products in Stripe, updating the archive status
 * of existing products, and managing product pricing information.
 */
public class StripeProductService {

    static {
        StripeConfig.init();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public Product saveProduct(Integer currentId, com.sellsphere.common.entity.Product product, Currency currency,
                               String taxBehaviorSetting)
            throws StripeException {

        if(currentId != null) {
            Product existingProduct = Product.retrieve(String.valueOf(currentId));
            String currentPriceId = existingProduct.getDefaultPrice();

            PriceCreateParams priceCreateParams = PriceCreateParams.builder()
                    .setUnitAmount(product.getDiscountPrice().multiply(BigDecimal.valueOf(currency.getUnitAmount())).longValue())
                    .setCurrency(currency.getCode())
                    .setProduct(String.valueOf(product.getId()))
                    .setTaxBehavior(PriceCreateParams.TaxBehavior.INCLUSIVE).build();
            Price newPrice = Price.create(priceCreateParams);


            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setActive(true)
                    .setName(product.getName())
                    .setDefaultPrice(newPrice.getId())
                    .addExpand("default_price")
                    .build();

            Product updatedProduct = existingProduct.update(params);

            // archive previous price
            Price currentPrice = Price.retrieve(currentPriceId);
            currentPrice.update(
                    PriceUpdateParams.builder()
                            .setActive(false)
                            .build()
            );

            return updatedProduct;
        } else {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setId(String.valueOf(product.getId()))
                    .setName(product.getName())
                    .setActive(true)
                    .setDefaultPriceData(
                            ProductCreateParams.DefaultPriceData.builder()
                                    .setUnitAmount(product.getDiscountPrice().multiply(BigDecimal.valueOf(currency.getUnitAmount())).longValue())
                                    .setCurrency(currency.getCode())
                                    .setTaxBehavior(ProductCreateParams.DefaultPriceData.TaxBehavior.valueOf(taxBehaviorSetting)).build())
                    .addExpand("default_price")
                    .build();

            return Product.create(params);
        }

    }

    /**
     * Changes the archive status of a product in Stripe.
     * <p>
     * This method retrieves the current product details from Stripe and updates its active status
     * based on the provided {@code status} parameter.
     *
     * @param id     The Stripe product ID to be updated.
     * @param status The new archive status for the product. If false, the product is unarchived
     *               (active).
     * @throws StripeException If an error occurs during the Stripe API interaction.
     */
    public void changeProductArchiveStatus(String id, boolean status) throws StripeException {
        Product currentProduct = Product.retrieve(id);

        ProductUpdateParams params = ProductUpdateParams.builder().setActive(status).build();
        currentProduct.update(params);
    }

}
