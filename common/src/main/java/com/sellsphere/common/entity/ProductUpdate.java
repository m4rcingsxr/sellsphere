package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_updates")
public class ProductUpdate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @OneToOne
    @MapsId // product id is both foreign and primary key
    @JoinColumn(name = "product_id")
    private Product product;

}