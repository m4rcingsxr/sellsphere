package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.common.entity.payload.AddressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class RestAddressController {

    private final CustomerService customerService;
    private final AddressValidationService validationService;

    // assumption: provided addresses already validated
    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAddresses(Principal principal)
            throws CustomerNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        return ResponseEntity.ok(
                customer.getAddresses()
                        .stream()
                        .map(address ->
                                     AddressDTO.builder()
                                             .id(address.getId())
                                             .firstName(address.getFirstName())
                                             .lastName(address.getLastName())
                                             .fullName(address.getFullName())
                                             .state(address.getState())
                                             .phoneNumber(address.getPhoneNumber())
                                             .countryCode(address.getCountry().getCode())
                                             .city(address.getCity())
                                             .addressLine1(address.getAddressLine1())
                                             .addressLine2(address.getAddressLine2())
                                             .postalCode(address.getPostalCode())
                                             .currencyUnitAmount(
                                                     address.getCountry().getCurrency().getUnitAmount())
                                             .currencySymbol(
                                                     address.getCountry().getCurrency().getSymbol())
                                             .build()
                        )
                        .toList()
        );
    }

    // simple validation true/false
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateAddress(
            @RequestBody AddressValidationRequest addressRequest) {
        boolean addressValid = validationService.isAddressValid(addressRequest);
        return ResponseEntity.ok(addressValid);
    }


    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
