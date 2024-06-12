package com.sellsphere.easyship;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.easyship.payload.*;
import com.sellsphere.easyship.payload.shipment.Product;
import com.sellsphere.easyship.payload.shipment.ProductResponse;
import com.sellsphere.easyship.payload.shipment.ShipmentResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Type;
import java.util.List;

import static com.sellsphere.easyship.Constants.BASE_URL;
import static com.sellsphere.easyship.Constants.BEARER_TOKEN;

/**
 * Service class for integrating with Easyship API.
 */
@Singleton
public class EasyshipIntegrationService implements EasyshipService {

    private final Client client;
    private final Gson gson;

    @Inject
    public EasyshipIntegrationService(Client client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    /**
     * Retrieves shipping rates from Easyship API.
     *
     * @param pageNum          The page number for pagination.
     * @param recipient        The recipient address details.
     * @param cart             The list of cart items.
     * @param baseCurrencyCode The base currency code.
     * @return The Easyship rate response.
     */
    @Override
    public ShippingRatesResponse getShippingRates(Integer pageNum, Address recipient, List<CartItem> cart, String baseCurrencyCode) {
        String outputCurrency = recipient.getCurrencyCode() == null ? baseCurrencyCode : recipient.getCurrencyCode();

        WebTarget target = client.target(BASE_URL).path("/rates").queryParam("page", pageNum).queryParam("sortBy", "cost_rank");

        ShippingRatesRequest rateRequest = ShippingRatesRequest.builder()
                .courierSelection(ShippingRatesRequest.CourierSelection.builder()
                                          .applyShippingRules(true)
                                          .showCourierLogoUrl(true)
                                          .build())
                .destinationAddress(Address.builder()
                                            .countryAlpha2(recipient.getCountryAlpha2())
                                            .city(recipient.getCity())
                                            .postalCode(recipient.getPostalCode())
                                            .line1(recipient.getLine1())
                                            .line2(recipient.getLine2())
                                            .state(recipient.getState())
                                            .build())
                .originAddress(Address.builder()
                                       .countryAlpha2("BE")
                                       .city("Antwerp")
                                       .postalCode("1000")
                                       .build())
                .incoterms("DDU")
                .insurance(ShippingRatesRequest.Insurance.builder()
                                   .isInsured(false)
                                   .build())
                .parcels(cart.stream().map(cartItem -> ShippingRatesRequest.Parcel.builder()
                        .items(List.of(ShippingRatesRequest.Item.builder()
                                               .quantity(cartItem.getQuantity())
                                               .actualWeight(cartItem.getProduct().getWeight().doubleValue())
                                               .declaredCurrency("EUR")
                                               .declaredCustomsValue(cartItem.getProduct().getPrice().doubleValue())
                                               .dimensions(ShippingRatesRequest.Item.Dimensions.builder()
                                                                   .length(cartItem.getProduct().getLength().doubleValue())
                                                                   .width(cartItem.getProduct().getWidth().doubleValue())
                                                                   .height(cartItem.getProduct().getHeight().doubleValue())
                                                                   .build())
                                               .hsCode(1)
                                               .build()))
                        .build()).toList())
                .shippingSettings(ShippingRatesRequest.ShippingSettings.builder()
                                          .units(ShippingRatesRequest.Units.builder()
                                                         .dimensions(Constants.UNIT_OF_LENGTH)
                                                         .weight(Constants.UNIT_OF_WEIGHT)
                                                         .build())
                                          .outputCurrency(outputCurrency)
                                          .build())
                .build();

        String payload = gson.toJson(rateRequest);

        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        try (Response response = invocationBuilder.post(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            return processShippingRatesResponse(response);
        }
    }

    private ShippingRatesResponse processShippingRatesResponse(Response response) {
        if (response.getStatus() == Response.Status.OK.getStatusCode() ||
                response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String jsonResponse = response.readEntity(String.class);
            return gson.fromJson(jsonResponse, ShippingRatesResponse.class);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }

    /**
     * Retrieves the account information from Easyship API.
     *
     * @return The account information as a JSON string.
     */
    @Override
    public String getAccountInfo() {
        WebTarget target = client.target(BASE_URL).path("/account");

        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        try (Response response = invocationBuilder.get()) {
            return processStringResponse(response);
        }
    }


    /**
     * Updates the sender address in Easyship.
     *
     * @param addressDto The address details to be updated.
     * @return The response from Easyship API as a JSON string.
     */
    @Override
    public String updateSenderAddress(Address addressDto) {
        int currentPage = 0;
        boolean senderFound = false;
        String nextPage;

        do {
            WebTarget getTarget = client.target(BASE_URL).path("/addresses")
                    .queryParam("page", currentPage)
                    .queryParam("perPage", "10");

            Invocation.Builder getInvocation = getTarget.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", BEARER_TOKEN)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept-Language", "en-US,en;q=0.5");

            try (Response getResponse = getInvocation.get()) {
                AddressResponse addressResponse = processAddressResponse(getResponse);

                for (Address address : addressResponse.getAddresses()) {
                    if (address.getDefaultFor().isSender()) {
                        WebTarget deleteTarget = client.target(BASE_URL).path("/addresses/" + address.getId());
                        Invocation.Builder deleteInvocation = deleteTarget.request(MediaType.APPLICATION_JSON)
                                .header("Authorization", BEARER_TOKEN)
                                .header("User-Agent", "Mozilla/5.0")
                                .header("Accept-Language", "en-US,en;q=0.5");

                        try (Response deleteResponse = deleteInvocation.delete()) {
                            processStringResponse(deleteResponse);
                            senderFound = true;
                            break;
                        }
                    }
                }

                nextPage = addressResponse.getMeta().getPagination().getNext();
                currentPage++;
            }

        } while (nextPage != null && !senderFound);

        WebTarget postTarget = client.target(BASE_URL).path("/addresses");
        Invocation.Builder postInvocation = postTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        String payload = gson.toJson(addressDto);
        try (Response postResponse = postInvocation.post(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            return processStringResponse(postResponse);
        }
    }

    private AddressResponse processAddressResponse(Response response) {
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String jsonResponse = response.readEntity(String.class);
            Type listType = new TypeToken<AddressResponse>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }

    /**
     * Creates a shipment in Easyship.
     *
     * @return The response from Easyship API.
     */
    @Override
    public ShipmentResponse createShipment() {
        // origin address - same as sender address
        // destination address
        // packaging details - parcels, items

        return null;
    }

    /**
     * Saves a product in Easyship.
     *
     * @param product The product details to be saved.
     * @return The response from Easyship API.
     */
    public ProductResponse saveProduct(Product product) {
        WebTarget postTarget = client.target(BASE_URL).path("/products");

        Invocation.Builder postInvocation = postTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        String payload = gson.toJson(product);

        try (Response postResponse = postInvocation.post(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            return processProductResponse(postResponse);
        }
    }

    private ProductResponse processProductResponse(Response response) {
        if (response.getStatus() == Response.Status.OK.getStatusCode() ||
                response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String rawResponse = response.readEntity(String.class);
            JsonElement jsonElement = JsonParser.parseString(rawResponse);
            Type responseType = new TypeToken<ProductResponse>() {}.getType();
            return gson.fromJson(jsonElement, responseType);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }

    private String processStringResponse(Response response) {
        if (response.getStatus() == Response.Status.OK.getStatusCode() ||
                response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String result = response.readEntity(String.class);
            JsonElement jsonElement = JsonParser.parseString(result);
            return gson.toJson(jsonElement);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }

}