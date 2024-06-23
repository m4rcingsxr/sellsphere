package com.sellsphere.client.checkout;

import com.sellsphere.client.PriceService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.payment.checkout.StripeCheckoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    @Bean
    public CheckoutService checkoutService(StripeCheckoutService stripeService,
                                           ShoppingCartService cartService,
                                           SettingService settingService,
                                           CurrencyService currencyService,
                                           PriceService priceService,
                                           CartItemRepository cartItemRepository) {
        return new CheckoutService(stripeService, cartService, settingService, currencyService,
                                   priceService, cartItemRepository
        );
    }

    @Bean
    @Primary
    public StripeCheckoutService stripeCheckoutService() {
        return mock(StripeCheckoutService.class);
    }

    @Bean
    @Primary
    public ShoppingCartService shoppingCartService() {
        return mock(ShoppingCartService.class);
    }

    @Bean
    @Primary
    public SettingService settingService() {
        return mock(SettingService.class);
    }

    @Bean
    @Primary
    public CurrencyService currencyService() {
        return mock(CurrencyService.class);
    }

    @Bean
    @Primary
    public CartItemRepository cartItemRepository() {
        return mock(CartItemRepository.class);
    }

    @Bean
    public PriceService priceService() {
        return new PriceService(currencyService());
    }
}
