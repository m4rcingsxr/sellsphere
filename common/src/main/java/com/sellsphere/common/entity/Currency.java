package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a currency entity with name, symbol, and code.
 * This entity is used to store currency information in the database.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "currencies")
public class Currency extends IdentifiedEntity {

    /**
     * The name of the currency.
     * This field is mandatory and must not exceed 128 characters.
     * Example: "US Dollar", "Euro"
     */
    @Column(name = "name", nullable = false, length = 128)
    @NotNull(message = "Currency name is required")
    @Size(max = 128, message = "Currency name must be less than or equal to 128 characters")
    private String name;

    /**
     * The symbol of the currency.
     * This field is mandatory and must not exceed 8 characters.
     * Example: "$", "€"
     */
    @Column(name = "symbol", nullable = false, length = 8)
    @NotNull(message = "Currency symbol is mandatory")
    @Size(max = 8, message = "Currency symbol must be less than or equal to 8 characters")
    private String symbol;

    /**
     * The code of the currency.
     * This field is mandatory and follows the ISO 4217 standard.
     * Example: "USD", "EUR"
     */
    @Column(name = "code", nullable = false)
    @NotNull(message = "Currency code is mandatory")
    private String code;

    @Column(name = "unit_amount", nullable = false)
    @NotNull(message = "Unit amount cannot be null")
    private Long unitAmount;

    @OneToMany(mappedBy = "currency")
    @OrderBy("name asc")
    List<Country> countries;

    public Currency(Integer id, String name, String symbol, String code) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.code = code;
    }

}
