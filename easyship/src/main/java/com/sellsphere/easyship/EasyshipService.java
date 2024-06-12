package com.sellsphere.easyship;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.easyship.payload.ShippingRatesResponse;
import com.sellsphere.easyship.payload.shipment.Product;
import com.sellsphere.easyship.payload.shipment.ProductResponse;
import com.sellsphere.easyship.payload.shipment.ShipmentResponse;

import java.util.List;

public interface EasyshipService {

    ShippingRatesResponse getShippingRates(Integer pageNum, Address recipient,
                                           List<CartItem> cart, String baseCurrencyCode);

    ShipmentResponse createShipment();

    String getAccountInfo();

    String updateSenderAddress(Address addressDto);

    ProductResponse saveProduct(Product product);

}
