package com.sellsphere.client.shipping;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.ApiService;
import com.sellsphere.easyship.payload.AddressDtoMin;
import com.sellsphere.easyship.payload.RatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shipping")
public class ShippingRestController {

    private final ApiService apiService;
    private final CustomerService customerService;
    private final CartItemRepository cartItemRepository;
    private final SettingService settingService;

    @PostMapping("/rates")
    public ResponseEntity<RatesResponse> getAvailableRates(@RequestBody AddressDtoMin addressDto,
                                                           Principal principal)
            throws CustomerNotFoundException {

        // todo: validate addresses before getting rates
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);
        RatesResponse rates = apiService.getRates(addressDto, cart);

        return ResponseEntity.ok(rates);
    }

    // todo: validate addresses
    @GetMapping("/shippable-addresses")
    public ResponseEntity<List<AddressDtoMin>> getAvailableRates(Principal principal)
            throws CustomerNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        List<Address> addresses = customer.getAddresses();

        return ResponseEntity.ok(addresses.stream().map(address ->
                AddressDtoMin.builder().fullName(
                        address.getFullName()).state(
                        address.getState()).line1(address.getAddressLine1()).line2(
                        address.getAddressLine2()).postalCode(address.getPostalCode()).city(
                        address.getCity()).countryAlpha2(
                        address.getCountry().getCode()).build()
        ).toList());
    }

    @GetMapping("/supported-countries")
    public ResponseEntity<List<CountryDTO>> getSupportedCountries() {
        List<CountryDTO> supportedCountries = settingService.getSupportedCountries().stream().map(
                CountryDTO::new).toList();

        return ResponseEntity.ok(supportedCountries);
    }

    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
