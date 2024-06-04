package com.sellsphere.client.shipping;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.payload.AddressDtoMin;
import com.sellsphere.common.entity.payload.CountryDTO;
import com.sellsphere.easyship.ApiService;
import com.sellsphere.easyship.payload.RatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
            throws CustomerNotFoundException, CurrencyNotFoundException {

        // todo: validate addresses before getting rates
        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);
        RatesResponse rates = apiService.getRates(addressDto, cart);

        return ResponseEntity.ok(rates);
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
