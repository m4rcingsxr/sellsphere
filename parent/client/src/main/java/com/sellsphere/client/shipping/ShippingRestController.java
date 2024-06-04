package com.sellsphere.client.shipping;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.CartItemRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.AddressDTO;
import com.sellsphere.common.entity.payload.CountryDTO;
import com.sellsphere.easyship.ApiService;
import com.sellsphere.easyship.payload.EasyshipAddressDTO;
import com.sellsphere.easyship.payload.EasyshipRateResponse;
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
    private final CartItemRepository cartItemRepository;
    private final CustomerService customerService;
    private final SettingService settingService;

    @PostMapping("/rates")
    public ResponseEntity<EasyshipRateResponse> getAvailableRates(
            @RequestBody AddressDTO addressDto,@RequestParam Integer page,
            Principal principal) throws CustomerNotFoundException {

        Customer customer = getAuthenticatedCustomer(principal);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);
        EasyshipRateResponse rates = apiService.getRates(
                page,
                EasyshipAddressDTO.builder()
                        .city(addressDto.getCity())
                        .state(addressDto.getState())
                        .line1(addressDto.getAddressLine1())
                        .line2(addressDto.getAddressLine2())
                        .postalCode(addressDto.getPostalCode())
                        .contactName(addressDto.getFullName())
                        .contactPhone(addressDto.getPhoneNumber())
                        .countryAlpha2(addressDto.getCountryCode())
                        .build(),
                cart);

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
