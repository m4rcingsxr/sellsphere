package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "roles")
public class Role extends IdentifiedEntity {

    @NotEmpty(message = "Role name is required.")
    @Size(max = 40, message = "Role name must not exceed 40 characters.")
    @Column(name = "name", length = 40, nullable = false, unique = true)
    private String name;

    @NotEmpty(message = "Role description is required.")
    @Size(max = 150, message = "Role description must not exceed 150 characters.")
    @Column(name = "description", length = 150, nullable = false)
    private String description;

}
