package com.sellsphere.common.entity.payload;

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
public class CartItemDTO implements Serializable {

    @NotNull(message = "Product is required")
    private BasicProductDTO product;

    private int quantity;

    private BigDecimal subtotal;

    public CartItemDTO(CartItem cartItem) {
        this.product = new BasicProductDTO(cartItem.getProduct());
        this.quantity = cartItem.getQuantity();
        this.subtotal = cartItem.getSubtotal();
    }

}