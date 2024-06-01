package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.easyship.payload.AddressDto;
import com.sellsphere.easyship.payload.AddressDtoMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class RestAddressController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<AddressDtoMin>> getAddresses(Principal principal)
            throws CustomerNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        return ResponseEntity.ok(customer.getAddresses().stream().map(address ->
            AddressDtoMin.builder()
                    .fullName(address.getFullName())
                    .state(address.getState())
                    .line1(address.getAddressLine1())
                    .line2(address.getAddressLine2())
                    .postalCode(address.getPostalCode())
                    .city(address.getCity())
                    .countryAlpha2(address.getCountry().getCode())
                    .build()
        ).toList());
    }

    private Customer getAuthenticatedCustomer(Principal principal) throws
            CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
