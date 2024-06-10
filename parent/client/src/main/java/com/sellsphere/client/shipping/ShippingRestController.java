package com.sellsphere.client.shipping;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.ShoppingCartService;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.AddressDTO;
import com.sellsphere.common.entity.payload.CountryDTO;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.easyship.payload.Address;
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
            @RequestBody AddressDTO addressDto,@RequestParam Integer page,
            Principal principal) throws CustomerNotFoundException, CurrencyNotFoundException {

        Customer customer = getAuthenticatedCustomer(principal);

        String currencyCode = settingService.getCurrencyCode().toUpperCase();

        List<CartItem> cart = shoppingCartService.findCartItemsByCustomer(customer);
        ShippingRatesResponse rates = apiService.getShippingRates(
                page,
                Address.builder()
                        .city(addressDto.getCity())
                        .state(addressDto.getState())
                        .line1(addressDto.getAddressLine1())
                        .line2(addressDto.getAddressLine2())
                        .postalCode(addressDto.getPostalCode())
                        .contactName(addressDto.getFullName())
                        .contactPhone(addressDto.getPhoneNumber())
                        .countryAlpha2(addressDto.getCountryCode())
                        .currencyCode(addressDto.getCurrencyCode())
                        .build(),
                cart, currencyCode);

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
