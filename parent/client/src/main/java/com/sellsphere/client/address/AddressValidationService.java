package com.sellsphere.client.address;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressValidationService {

    private final String apiKey = System.getenv("GOOGLE_VALIDATION_KEY");
    private final RestTemplate restTemplate;

    public boolean isAddressValid(AddressValidationRequest request) {
        String url = "https://addressvalidation.googleapis.com/v1:validateAddress?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AddressValidationRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<AddressValidationResponse> responseEntity = restTemplate.exchange(url,
                                                                                         HttpMethod.POST,
                                                                                         requestEntity,
                                                                                         AddressValidationResponse.class
        );

        AddressValidationResponse response = responseEntity.getBody();

        assert response != null;
        return isValid(response);
    }

    private boolean isValid(AddressValidationResponse response) {
        AddressValidationResponse.Result result = response.getResult();

        if (result == null || result.getVerdict() == null || result.getAddress() == null) {
            return false;
        }

        AddressValidationResponse.Result.Verdict verdict = result.getVerdict();
        List<AddressValidationResponse.Result.Address.AddressComponent> components =
                result.getAddress().getAddressComponents();

        boolean inputGranularityValid = verdict.getInputGranularity().equals(
                "PREMISE") || (verdict.getInputGranularity().equals(
                "SUB_PREMISE") && verdict.getValidationGranularity().equals("PREMISE"));

        boolean addressComplete = verdict.isAddressComplete();

        if (!inputGranularityValid || !addressComplete) {
            return false;
        }

        boolean hasUnconfirmedComponents = verdict.isHasUnconfirmedComponents();
        if (hasUnconfirmedComponents) {
            return components.stream().allMatch(component -> component.getComponentType().equals(
                    "subpremise") && component.getConfirmationLevel().equals(
                    "UNCONFIRMED_BUT_PLAUSIBLE") || component.getConfirmationLevel().equals(
                    "CONFIRMED"));
        }

        return true;
    }

}
