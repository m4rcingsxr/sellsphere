package com.sellsphere.common.entity.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO implements Serializable {

    private Integer id;

    @NotNull(message = "First name is required")
    @Size(message = "First name cannot exceed 45 characters", max = 45)
    private String firstName;

    @NotNull(message = "Last name is required")
    @Size(message = "Last name cannot exceed 45 characters", max = 45)
    private String lastName;

    @NotNull(message = "Phone number is required")
    @Size(message = "Phone number cannot exceed 15 characters", max = 15)
    private String phoneNumber;

    @NotNull(message = "Address Line 1 is required")
    @Size(message = "Address Line 1 cannot exceed 64 characters", max = 64)
    private String addressLine1;

    private String addressLine2;

    @NotNull(message = "City is required")
    @Size(message = "City cannot exceed 45 characters", max = 45)
    private String city;

    @Size(message = "City cannot exceed 45 characters", max = 45)
    private String state;

    private String countryCode;

    @NotNull(message = "Postal code is required")
    @Size(message = "Postal code cannot exceed 10 characters", max = 10)
    @NotBlank(message = "Postal code cannot be blank")
    private String postalCode;

    private String fullName;

    // easyship selected courier
    private String courierId;
}