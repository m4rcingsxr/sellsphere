package com.sellsphere.easyship;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.easyship.payload.AddressDto;
import com.sellsphere.easyship.payload.AddressDtoMin;
import com.sellsphere.easyship.payload.AddressResponse;
import com.sellsphere.easyship.payload.RatesResponse;
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

@Singleton
public class ApiServiceImpl implements ApiService {

    private final Client client;
    private final Gson gson;

    @Inject
    public ApiServiceImpl(Client client, Gson gson) {
        this.client = client;
        this.gson= gson;
    }

    @Override
    public RatesResponse getRates(AddressDtoMin recipient, List<CartItem> cart) {
        WebTarget target = client.target(BASE_URL).path("/rates");

        JsonObject jsonPayload = new JsonObject();

        JsonObject courierSelection = new JsonObject();
        courierSelection.addProperty("apply_shipping_rules", true);
        courierSelection.addProperty("show_courier_logo_url", true);
        jsonPayload.add("courier_selection", courierSelection);

        JsonObject destinationAddress = new JsonObject();
        destinationAddress.addProperty("country_alpha2", recipient.getCountryAlpha2());
        destinationAddress.addProperty("city", recipient.getCity());
        destinationAddress.addProperty("postal_code", recipient.getPostalCode());
        destinationAddress.addProperty("line_1", recipient.getLine1());
        destinationAddress.addProperty("line_2", recipient.getLine2());
        destinationAddress.addProperty("state", recipient.getState());
        jsonPayload.add("destination_address", destinationAddress);

        // Hard coded origin address
        JsonObject originAddress = new JsonObject();
        originAddress.addProperty("country_alpha2", "BE");
        originAddress.addProperty("city", "Antwerp");
        originAddress.addProperty("postal_code", "1000");
        jsonPayload.add("origin_address", originAddress);

        jsonPayload.addProperty("incoterms", "DDU");

        // Insurance option
        JsonObject insurance = new JsonObject();
        insurance.addProperty("is_insured", false);
        jsonPayload.add("insurance", insurance);

        JsonArray parcelsArray = new JsonArray();
        for (CartItem cartItem : cart) {
            JsonObject parcel = new JsonObject();
            JsonArray itemsArray = new JsonArray();

            JsonObject item = new JsonObject();
            item.addProperty("quantity", cartItem.getQuantity());
            item.addProperty("actual_weight", cartItem.getProduct().getWeight().doubleValue());
            item.addProperty("declared_currency", "EUR");
            item.addProperty("declared_customs_value", cartItem.getProduct().getPrice().doubleValue());

            JsonObject dimensions = new JsonObject();
            dimensions.addProperty("length", cartItem.getProduct().getLength().doubleValue());
            dimensions.addProperty("width", cartItem.getProduct().getWidth().doubleValue());
            dimensions.addProperty("height", cartItem.getProduct().getHeight().doubleValue());
            item.add("dimensions", dimensions);

            item.addProperty("hs_code", 1); // Replace with actual HS code if available

            itemsArray.add(item);
            parcel.add("items", itemsArray);
            parcelsArray.add(parcel);
        }
        jsonPayload.add("parcels", parcelsArray);

        JsonObject shippingSettings = new JsonObject();
        JsonObject units = new JsonObject();
        units.addProperty("dimensions", "cm");
        units.addProperty("weight", "kg");
        shippingSettings.add("units", units);
        shippingSettings.addProperty("output_currency", "EUR");
        jsonPayload.add("shipping_settings", shippingSettings);

        String payload = gson.toJson(jsonPayload);

        System.out.println(payload);

        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        Response response = invocationBuilder.post(Entity.entity(payload, MediaType.APPLICATION_JSON));

        return getRatesResponseFromResponse(response);
    }

    private RatesResponse getRatesResponseFromResponse(Response response) {
        if (response.getStatus() == 200 || response.getStatus() == 201) {
            String jsonResponse = response.readEntity(String.class);
            return gson.fromJson(jsonResponse, RatesResponse.class);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
        }

    @Override
    public String getAccount() {
        WebTarget target = client.target(BASE_URL).path("/account");

        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        Response response = invocationBuilder.get();

        return processResponse(response);

    }

    @Override
    public String updateSender(AddressDto addressDto) {
        int currentPage = 0;
        boolean senderFound = false;
        String nextPage = null;

        do {
            // Fetch the current page of addresses
            WebTarget getTarget = client.target(BASE_URL).path("/addresses")
                    .queryParam("page", currentPage)
                    .queryParam("perPage", "10");

            Invocation.Builder getInvocation = getTarget.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", BEARER_TOKEN)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept-Language", "en-US,en;q=0.5");

            Response getResponse = getInvocation.get();
            AddressResponse addressResponse = getAddressPageResponse(getResponse);

            // Check if a sender address exists in the current page
            for (AddressDto address : addressResponse.getAddresses()) {
                if (address.getDefaultFor().isSender()) {
                    // Delete the sender address
                    WebTarget deleteTarget = client.target(BASE_URL).path("/addresses/" + address.getId());
                    Invocation.Builder deleteInvocation = deleteTarget.request(MediaType.APPLICATION_JSON)
                            .header("Authorization", BEARER_TOKEN)
                            .header("User-Agent", "Mozilla/5.0")
                            .header("Accept-Language", "en-US,en;q=0.5");

                    Response deleteResponse = deleteInvocation.delete();
                    processResponse(deleteResponse); // Handle delete response if needed
                    senderFound = true;
                    break;
                }
            }

            // Check if there is a next page
            nextPage = addressResponse.getMeta().getPagination().getNext();
            currentPage++;

        } while (nextPage != null && !senderFound);

        // Create the new sender address
        WebTarget postTarget = client.target(BASE_URL).path("/addresses");
        Invocation.Builder postInvocation = postTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER_TOKEN)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en-US,en;q=0.5");

        String payload = gson.toJson(addressDto, AddressDto.class);
        Response postResponse = postInvocation.post(Entity.entity(payload, MediaType.APPLICATION_JSON));
        return processResponse(postResponse);
    }

    private AddressResponse getAddressPageResponse(Response response) {
        if (response.getStatus() == 200) {
            String jsonResponse = response.readEntity(String.class);
            Type listType = new TypeToken<AddressResponse>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }


    private List<AddressDto> getAddressesFromResponse(Response response) {
        if (response.getStatus() == 200) {
            String jsonResponse = response.readEntity(String.class);
            Type listType = new TypeToken<List<AddressDto>>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }


    private String processResponse(Response response) {
        if (response.getStatus() == 200 || response.getStatus() == 201) {
            String result = response.readEntity(String.class);
            // Parse the JSON response
            JsonElement jsonElement = JsonParser.parseString(result);

            // Convert the JSON element to a pretty-printed string
            return gson.toJson(jsonElement);
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("API request failed with status " + response.getStatus() + ": " + error);
        }
    }



}
