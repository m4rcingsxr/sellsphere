package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "metadata_entries")
public class MetadataEntry extends IdentifiedEntity {

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

}
