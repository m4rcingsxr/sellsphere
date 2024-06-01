package com.sellsphere.easyship;

import com.sellsphere.easyship.payload.AddressDto;

public interface ApiService {

    String getRates();

    String getAccount();

    String updateSender(AddressDto addressDto);
}
