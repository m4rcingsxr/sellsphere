package com.sellsphere.client.address;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import com.sellsphere.easyship.payload.AddressDtoMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class RestAddressController {


    private final String apiKey = System.getenv("GOOGLE_VALIDATION_KEY");
    private final CustomerService customerService;
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDtoMin>> getAddresses(Principal principal)
            throws CustomerNotFoundException {
        Customer customer = getAuthenticatedCustomer(principal);
        return ResponseEntity.ok(customer.getAddresses().stream().map(
                address -> AddressDtoMin.builder().fullName(address.getFullName()).state(
                        address.getState()).line1(address.getAddressLine1()).line2(
                        address.getAddressLine2()).postalCode(address.getPostalCode()).city(
                        address.getCity()).countryAlpha2(
                        address.getCountry().getCode()).build()).toList());
    }


    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateAddress(
            @RequestBody AddressValidationRequest addressRequest) {
        String url = "https://addressvalidation.googleapis.com/v1:validateAddress?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AddressValidationRequest> requestEntity = new HttpEntity<>(addressRequest,
                                                                              headers
        );

        ResponseEntity<AddressValidationResponse> responseEntity = restTemplate.exchange(url,
                                                                                         HttpMethod.POST,
                                                                                         requestEntity,
                                                                                         AddressValidationResponse.class
        );

        AddressValidationResponse response = responseEntity.getBody();


        if (response == null) {
            throw new RuntimeException("Failed to fetch the verification result");
        }

        return ResponseEntity.ok(addressService.isValid(response));
    }


    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }

}
