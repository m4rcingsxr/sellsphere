package com.sellsphere.payment;

import com.sellsphere.common.entity.Currency;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;

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

    /**
     * Creates or updates a product in Stripe.
     * <p>
     * If an existing product ID is provided, the product's archive status is changed to active
     * before creating a new product in Stripe. The new product details are provided through
     * the {@code newProduct} parameter.
     *
     * @param existingProductId The ID of the existing product to be updated. Can be null.
     * @param newProduct        The new product details to be created in Stripe.
     * @param currency          The currency information for setting the price.
     * @return The created or updated Stripe {@code Product}.
     * @throws StripeException If an error occurs during the Stripe API interaction.
     */
    public Product saveProduct(Integer existingProductId,
                               com.sellsphere.common.entity.Product newProduct, Currency currency,
                               String taxBehaviorSetting)
            throws StripeException {

        if (existingProductId != null) {
            changeProductArchiveStatus(String.valueOf(existingProductId), false);
        }

        ProductCreateParams params = ProductCreateParams.builder().setId(
                String.valueOf(newProduct.getId())).setName(
                newProduct.getName()).setDefaultPriceData(
                ProductCreateParams.DefaultPriceData.builder().setUnitAmount(
                                newProduct.getDiscountPrice().multiply(BigDecimal.valueOf(
                                        currency.getUnitAmount())).longValue()).setCurrency(
                                currency.getCode())
                        .setTaxBehavior(ProductCreateParams.DefaultPriceData.TaxBehavior.valueOf(
                                taxBehaviorSetting))
                        .build()).addExpand("default_price").build();

        return Product.create(params);
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
