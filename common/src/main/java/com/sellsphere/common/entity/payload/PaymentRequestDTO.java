package com.sellsphere.common.entity.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "Address cannot be null")
    private AddressDTO address;

    @NotNull(message = "Courier name cannot be null")
    @Size(min = 1, max = 100, message = "Courier name must be between 1 and 100 characters")
    private String courierName;

    private String courierLogoUrl;

    @NotNull(message = "Max delivery time cannot be null")
    @Min(value = 0, message = "Max delivery time must be a positive number")
    private Integer maxDeliveryTime;

    @NotNull(message = "Min delivery time cannot be null")
    @Min(value = 0, message = "Min delivery time must be a positive number")
    private Integer minDeliveryTime;

    @NotNull(message = "Amount total cannot be null")
    @Min(value = 0, message = "Amount total must be a positive number")
    private long amountTotal;

    @NotNull(message = "Amount tax cannot be null")
    @Min(value = 0, message = "Amount tax must be a positive number")
    private long amountTax;

    @NotNull(message = "Shipping amount cannot be null")
    @Min(value = 0, message = "Shipping amount must be a positive number")
    private long shippingAmount;

    @NotNull(message = "Shipping tax cannot be null")
    @Min(value = 0, message = "Shipping tax must be a positive number")
    private long shippingTax;

}