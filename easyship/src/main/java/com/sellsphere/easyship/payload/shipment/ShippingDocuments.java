package com.sellsphere.easyship.payload.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShippingDocuments {

    private String category;

    private String format;

    @JsonProperty("page_size")
    private String pageSize;

    private boolean required;

    private String url;

}
