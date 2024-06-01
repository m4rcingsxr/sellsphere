package com.sellsphere.client.checkout;

import com.stripe.model.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateTaxRequest {

    private Address address;
    private BigDecimal shippingCost;

}
