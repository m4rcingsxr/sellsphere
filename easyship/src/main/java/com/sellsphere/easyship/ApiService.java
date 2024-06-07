package com.sellsphere.easyship;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.easyship.payload.EasyshipAddressDTO;
import com.sellsphere.easyship.payload.EasyshipRateResponse;

import java.util.List;

public interface ApiService {

    EasyshipRateResponse getRates(Integer pageNum, EasyshipAddressDTO recipient,
                                  List<CartItem> cart, String baseCurrencyCode);

    String getAccount();

    String updateSender(EasyshipAddressDTO addressDto);
}
