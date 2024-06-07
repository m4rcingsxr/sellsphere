package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.CartItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MinCartItemDTO {

    @NotNull(message = "Product id is required")
    @Min(value = 1, message = "Product id cannot be less than 1")
    private int productId;

    @NotNull(message = "Quantity is required")
    @Max(value = 5, message = "Maximum 5 same products in cart")
    private int quantity;

    public MinCartItemDTO(CartItem cartItem) {
        this.productId = cartItem.getProduct().getId();
        this.quantity = cartItem.getQuantity();
    }

}