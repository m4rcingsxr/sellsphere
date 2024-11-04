package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "wishlist")
public class Wishlist extends IdentifiedEntity {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "wishlist_product",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public void addProduct(Product product) {
        products.add(product);
        product.getWishlists().add(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.getWishlists().remove(this);
    }
}
