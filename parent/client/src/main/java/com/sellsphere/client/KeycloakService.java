package com.sellsphere.client;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    public String getAdminAccessToken() {
        String authUrl = "http://192.168.0.234:8180/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "admin-cli");
        map.add("username", System.getenv("KEYCLOAK_USERNAME"));
        map.add("password", System.getenv("KEYCLOAK_PASSWORD"));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);
        return response.getBody().get("access_token").toString();
    }

    public HttpStatusCode changeUserPassword(String userId,    String accessToken, String newPassword) {
        String keycloakUrl = "http://192.168.0.234:8180/admin/realms/SellSphere/users/" + userId + "/reset-password";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("type", "password");
        body.put("value", newPassword);
        body.put("temporary", "false");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Void> response = restTemplate.exchange(URI.create(keycloakUrl), HttpMethod.PUT, request, Void.class);
        return response.getStatusCode();
    }

    public String getUserIdByUsername(String username) {
        String accessToken = getAdminAccessToken();
        String keycloakUrl = "http://192.168.0.234:8180/admin/realms/SellSphere/users?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List> response = restTemplate.exchange(keycloakUrl, HttpMethod.GET, request, List.class);
        if (response.getBody() != null && !response.getBody().isEmpty()) {
            Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
            return (String) user.get("id"); // Returns the Keycloak user ID
        }
        throw new RuntimeException("User not found");
    }

}
