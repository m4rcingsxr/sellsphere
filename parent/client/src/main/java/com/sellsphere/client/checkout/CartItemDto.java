package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.payload.BasicProductDTO;
import com.sellsphere.common.entity.CartItem;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CartItemDto implements Serializable {

    @NotNull(message = "Product is required")
    private BasicProductDTO product;

    private int quantity;

    private BigDecimal subtotal;

    public CartItemDto(CartItem cartItem) {
        this.product = new BasicProductDTO(cartItem.getProduct());
        this.quantity = cartItem.getQuantity();
        this.subtotal = cartItem.getSubtotal();
    }

}