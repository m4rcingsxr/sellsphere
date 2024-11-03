package com.sellsphere.easyship;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.payload.ShippingRateRequestDTO;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.easyship.payload.HsCodeResponse;
import com.sellsphere.easyship.payload.ShippingRatesRequest;
import com.sellsphere.easyship.payload.ShippingRatesResponse;
import com.sellsphere.easyship.payload.shipment.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sellsphere.easyship.Constants.BASE_URL;
import static com.sellsphere.easyship.Constants.BEARER_TOKEN;

/**
 * Service class for integrating with Easyship API.
 */
@Singleton
public class EasyshipIntegrationService implements EasyshipService {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final Client client;
    private final Gson gson;

    @Inject
    public EasyshipIntegrationService(Client client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    /**
     * Get shipping rates based on the cart and destination details.
     *
     * @param pageNum     The page number for pagination.
     * @param requestDTO  The request details containing shipping and destination information.
     * @param cart        The list of cart items.
     * @return The Easyship rate response.
     */
    @Override
    public ShippingRatesResponse getShippingRates(Integer pageNum, ShippingRateRequestDTO requestDTO, List<CartItem> cart) {
        WebTarget target = client.target(BASE_URL).path("/rates")
                .queryParam("page", pageNum)
                .queryParam("sortBy", "cost_rank");

        ShippingRatesRequest request = buildShippingRatesRequest(requestDTO, cart);

        Invocation.Builder invocationBuilder = buildRequest(target);
        String payload = gson.toJson(request);

        try (Response response = invocationBuilder.post(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            return processResponse(response, ShippingRatesResponse.class);
        }
    }

    /**
     * Creates a shipment in Easyship.
     *
     * @return The response from Easyship API.
     */
    @Override
    public ShipmentResponse createShipment(ShipmentRequest request) {
        WebTarget target = client.target(BASE_URL).path("/shipments");
        Invocation.Builder invocationBuilder = buildRequest(target);

        String payload = gson.toJson(request);

        try (Response response = invocationBuilder.post(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            return processResponse(response, ShipmentResponse.class);
        }
    }

    /**
     * Saves a product in Easyship.
     *
     * @param product The product details to be saved.
     * @return The response from Easyship API.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public SaveProductResponse saveProduct(Product product) {
        WebTarget target = client.target(BASE_URL).path("/products");

        if (product.getId() != null) {
            target = target.path(product.getId());
        }
        Invocation.Builder invocationBuilder = buildRequest(target);
        String payload = gson.toJson(product);


        try (Response response = product.getId() == null ?
                invocationBuilder.post(Entity.entity(payload, MediaType.APPLICATION_JSON)) :
                invocationBuilder.put(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            return processResponse(response, SaveProductResponse.class);
        }
    }

    /**
     * Saves a product from Easyship.
     *
     * @param productId The product id to be removed.
     * @return The response from Easyship API.
     */
    @Override
    public DeleteProductResponse deleteProduct(String productId) {
        WebTarget target = client.target(BASE_URL).path("/products").path(productId);
        Invocation.Builder invocationBuilder = buildRequest(target);

        try (Response response = invocationBuilder.delete()) {
            return processResponse(response, DeleteProductResponse.class);
        }
    }

    /**
     * Fetches HS Codes from Easyship.
     *
     * @param page        The page number.
     * @param code        The HS Code.
     * @param description The description of the HS Code.
     * @return The response containing HS Codes.
     */
    @Override
    public HsCodeResponse fetchHsCodes(Integer page, String code, String description) {
        WebTarget target = client.target(BASE_URL).path("/hs_codes")
                .queryParam("per_page", Constants.HS_CODE_PER_PAGE)
                .queryParam("page", page)
                .queryParam("description", description)
                .queryParam("code", code);

        Invocation.Builder invocationBuilder = buildRequest(target);

        try (Response response = invocationBuilder.get()) {
            return processResponse(response, HsCodeResponse.class);
        }
    }


    /**
     * Builds the ShippingRatesRequest.
     *
     * @param requestDTO The shipping rate request details.
     * @param cart       The list of cart items.
     * @return The ShippingRatesRequest.
     */
    private ShippingRatesRequest buildShippingRatesRequest(ShippingRateRequestDTO requestDTO, List<CartItem> cart) {
        ShippingRatesRequest.ShippingRatesRequestBuilder ratesRequestBuilder = ShippingRatesRequest.builder()
                .incoterms("DDU")
                .insurance(ShippingRatesRequest.Insurance.builder().isInsured(false).build());

        setCourierOptions(ratesRequestBuilder);
        setDestinationAddress(ratesRequestBuilder, requestDTO);
        setOriginAddress(ratesRequestBuilder);
        setShippingSettings(ratesRequestBuilder, requestDTO.getCurrencyCode());
        addParcels(ratesRequestBuilder, cart, requestDTO.getCurrencyCode());

        return ratesRequestBuilder.build();
    }


    private void setCourierOptions(ShippingRatesRequest.ShippingRatesRequestBuilder ratesRequestBuilder) {
        ratesRequestBuilder.courierSelection(ShippingRatesRequest.CourierSelection.builder()
                                                     .applyShippingRules(true)
                                                     .showCourierLogoUrl(true)
                                                     .build());
    }

    private void setDestinationAddress(ShippingRatesRequest.ShippingRatesRequestBuilder ratesRequestBuilder, ShippingRateRequestDTO requestDTO) {
        ratesRequestBuilder.destinationAddress(Address.builder()
                                                       .city(requestDTO.getAddress().getCity())
                                                       .state(requestDTO.getAddress().getState())
                                                       .line1(requestDTO.getAddress().getAddressLine1())
                                                       .line2(requestDTO.getAddress().getAddressLine2())
                                                       .postalCode(requestDTO.getAddress().getPostalCode())
                                                       .contactName(requestDTO.getAddress().getFullName())
                                                       .contactPhone(requestDTO.getAddress().getPhoneNumber())
                                                       .countryAlpha2(requestDTO.getAddress().getCountryCode())
                                                       .build());
    }

    private void setOriginAddress(ShippingRatesRequest.ShippingRatesRequestBuilder ratesRequestBuilder) {
        ratesRequestBuilder.originAddress(Address.builder()
                                                  .countryAlpha2("BE")
                                                  .city("Antwerp")
                                                  .postalCode("1000")
                                                  .build());
    }

    private void setShippingSettings(ShippingRatesRequest.ShippingRatesRequestBuilder ratesRequestBuilder, String currencyCode) {
        ratesRequestBuilder.shippingSettings(ShippingRatesRequest.ShippingSettings.builder()
                                                     .units(ShippingRatesRequest.Units.builder()
                                                                    .dimensions(Constants.UNIT_OF_LENGTH)
                                                                    .weight(Constants.UNIT_OF_WEIGHT)
                                                                    .build())
                                                     .outputCurrency(currencyCode)
                                                     .build());
    }

    /**
     * Builds the request with appropriate headers.
     *
     * @param target The WebTarget.
     * @return The Invocation.Builder for building and sending the request.
     */
    private Invocation.Builder buildRequest(WebTarget target) {
        return target.request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .header(USER_AGENT_HEADER, "Mozilla/5.0")
                .header(ACCEPT_LANGUAGE_HEADER, "en-US,en;q=0.5");
    }

    private void addParcels(ShippingRatesRequest.ShippingRatesRequestBuilder ratesRequestBuilder, List<CartItem> cart, String currencyCode) {
        ratesRequestBuilder.parcels(cart.stream()
                                            .map(cartItem -> ShippingRatesRequest.Parcel.builder()
                                                    .items(List.of(ShippingRatesRequest.Item.builder()
                                                                           .quantity(cartItem.getQuantity())
                                                                           .actualWeight(cartItem.getProduct().getWeight())
                                                                           .declaredCurrency(currencyCode)
                                                                           .declaredCustomsValue(cartItem.getProduct().getPrice())
                                                                           .dimensions(ShippingRatesRequest.Item.Dimensions.builder()
                                                                                               .length(cartItem.getProduct().getLength())
                                                                                               .width(cartItem.getProduct().getWidth())
                                                                                               .height(cartItem.getProduct().getHeight())
                                                                                               .build())
                                                                           .hsCode(Integer.parseInt(cartItem.getProduct().getHsCode()))
                                                                           .build()))
                                                    .build()).toList());
    }


    /**
     * Generic method to process a response from Easyship API.
     *
     * @param <T>       The type of response expected.
     * @param response  The HTTP response.
     * @param classType The class of the expected response.
     * @return The response deserialized into the expected class.
     */
    private <T> T processResponse(Response response, Class<T> classType) {
        if (response.getStatus() == Response.Status.OK.getStatusCode() ||
                response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String jsonResponse = response.readEntity(String.class);
            return gson.fromJson(jsonResponse, classType);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }

}