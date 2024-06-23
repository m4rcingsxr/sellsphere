package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * It helps determine the correct tax rates for the products sold in different locations. Stripe
 * Tax uses product tax codes (PTCs) to associate products with tax rates. Each product in Stripe
 * can have a product tax code assigned to it, which is associated with a Tax Code object in the
 * Stripe API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_taxes")
public class ProductTax {

    @Id
    @Column(name = "id", nullable = false, length = 13)
    private String id;

    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private TaxType type;

    @Column(name = "description", nullable = false, length = 1024)
    private String description;

}
