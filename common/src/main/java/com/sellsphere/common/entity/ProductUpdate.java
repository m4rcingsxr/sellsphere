package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_updates")
public class ProductUpdate extends IdentifiedEntity {

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}