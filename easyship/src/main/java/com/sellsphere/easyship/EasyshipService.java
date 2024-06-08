package com.sellsphere.easyship;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.easyship.payload.EasyshipAddress;
import com.sellsphere.easyship.payload.EasyshipRateResponse;

import java.util.List;

public interface EasyshipService {

    EasyshipRateResponse getShippingRates(Integer pageNum, EasyshipAddress recipient,
                                  List<CartItem> cart, String baseCurrencyCode);

    String getAccountInfo();

    String updateSenderAddress(EasyshipAddress addressDto);
}
