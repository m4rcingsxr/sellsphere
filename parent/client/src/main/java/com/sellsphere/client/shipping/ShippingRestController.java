package com.sellsphere.client.shipping;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.CurrencyNotFoundException;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.payload.CountryDTO;
import com.sellsphere.common.entity.payload.ShippingRateRequestDTO;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.easyship.payload.ShippingRatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shipping")
public class ShippingRestController {

    private final EasyshipService apiService;
    private final ShoppingCartService shoppingCartService;
    private final CustomerService customerService;
    private final SettingService settingService;

    @PostMapping("/rates")
    public ResponseEntity<ShippingRatesResponse> getAvailableRates(
            @RequestBody ShippingRateRequestDTO request, @RequestParam Integer page,
            Principal principal) throws CustomerNotFoundException, CurrencyNotFoundException {

        // always base currency
        Customer customer = getAuthenticatedCustomer(principal);

        request.setCurrencyCode(settingService.getCurrencyCode(true));
        List<CartItem> cart = shoppingCartService.findAllByCustomer(customer);
        ShippingRatesResponse rates = apiService.getShippingRates(page, request, cart);
        rates.setAddress(request.getAddress());

        return ResponseEntity.ok(rates);
    }

    @GetMapping("/supported-countries")
    public ResponseEntity<List<CountryDTO>> getSupportedCountries() {
        List<CountryDTO> supportedCountries = settingService
                .getSupportedCountries()
                .stream()
                .map(CountryDTO::new)
                .toList();

        return ResponseEntity.ok(supportedCountries);
    }

    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
