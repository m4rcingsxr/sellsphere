package com.sellsphere.easyship;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.easyship.payload.AddressDto;
import com.sellsphere.easyship.payload.AddressDtoMin;
import com.sellsphere.easyship.payload.RatesResponse;

import java.util.List;

public interface ApiService {

    RatesResponse getRates(AddressDtoMin address , List<CartItem> cart);

    String getAccount();

    String updateSender(AddressDto addressDto);
}
