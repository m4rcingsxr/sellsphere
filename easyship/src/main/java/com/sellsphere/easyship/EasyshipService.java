package com.sellsphere.easyship;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.easyship.payload.HsCodeResponse;
import com.sellsphere.easyship.payload.ShippingRatesResponse;
import com.sellsphere.easyship.payload.shipment.*;

import java.util.List;

public interface EasyshipService {

    ShippingRatesResponse getShippingRates(Integer pageNum, Address recipient,
                                           List<CartItem> cart, String baseCurrencyCode);

    ShipmentResponse createShipment(ShipmentRequest request);

    String getAccountInfo();

    String updateSenderAddress(Address addressDto);

    SaveProductResponse saveProduct(Product product);

    DeleteProductResponse deleteProduct(String productId);

    HsCodeResponse fetchHsCodes(Integer page, String code, String description);
}
