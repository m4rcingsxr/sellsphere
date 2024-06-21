package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.payload.CountryDTO;
import com.sellsphere.common.entity.CountryNotFoundException;
import com.sellsphere.common.entity.payload.IpInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CountryRestController {

    private final String ipinfoApiKey = System.getenv("IPINFO_API_KEY");

    private final CountryRepository countryRepository;

    @PostMapping("/countries/country-ip")
    public ResponseEntity<CountryDTO> fetchCountryForIp(@RequestBody Map<String, String> request)
            throws CountryNotFoundException {
        RestTemplate restTemplate = new RestTemplate();

        // Fetch country code from IP address
        String infoUrl = String.format("https://ipinfo.io/%s?token=%s", request.get("ip"), ipinfoApiKey);
        IpInfoResponseDTO ipInfoResponse = restTemplate.getForObject(infoUrl, IpInfoResponseDTO.class);
        String countryCode = ipInfoResponse != null ? ipInfoResponse.getCountry() : null;

        if (countryCode == null) {
            return ResponseEntity.badRequest().build();
        }

        Country country = countryRepository.findByCode(countryCode).orElseThrow(CountryNotFoundException::new);
        return ResponseEntity.ok(new CountryDTO(country));
    }

}
