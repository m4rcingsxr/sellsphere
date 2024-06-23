package com.sellsphere.easyship;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.payload.AddressDTO;
import com.sellsphere.common.entity.payload.ShippingRateRequestDTO;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.easyship.payload.HsCodeResponse;
import com.sellsphere.easyship.payload.ShippingRatesRequest;
import com.sellsphere.easyship.payload.ShippingRatesResponse;
import com.sellsphere.easyship.payload.shipment.*;

import java.util.List;

public interface EasyshipService {

    ShippingRatesResponse getShippingRates(Integer pageNum, ShippingRateRequestDTO request, List<CartItem> cart);

    ShipmentResponse createShipment(ShipmentRequest request);

    SaveProductResponse saveProduct(Product product);

    DeleteProductResponse deleteProduct(String productId);

    HsCodeResponse fetchHsCodes(Integer page, String code, String description);

}
