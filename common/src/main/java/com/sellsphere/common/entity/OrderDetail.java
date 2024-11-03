package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "order_details")
public class OrderDetail extends IdentifiedEntity {


    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "product_cost", nullable = false)
    private BigDecimal productCost;


    @Column(name = "unit_price", nullable = false)
    private BigDecimal productPrice;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public OrderDetail(String name,
                       int quantity,
                       BigDecimal productCost,
                       BigDecimal productPrice,
                       BigDecimal subtotal,
                       DetailContext detailContext,
                       Order order
    ) {
        this.product = new Product();
        this.order = order;

        switch(detailContext) {
            case PRODUCT -> this.product.setName(name);
            case CATEGORY -> {
                Category category = new Category();
                category.setName(name);
                this.product.setCategory(category);
            }
        }

        this.quantity = quantity;
        this.productCost = productCost;
        this.productPrice = productPrice;
        this.subtotal = subtotal;
    }
}
