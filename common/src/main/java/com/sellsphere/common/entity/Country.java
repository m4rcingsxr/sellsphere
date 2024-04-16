package com.sellsphere.common.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a country.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "countries")
public class Country extends IdentifiedEntity {

    /**
     * The name of the country.
     * This field is required, must be unique, and can have a maximum length of 64 characters.
     */
    @NotNull(message = "Country name is required")
    @NotBlank(message = "Country name cannot be blank")
    @Size(max = 64, message = "Country name must be 64 characters or less")
    @Column(name = "name", length = 64, nullable = false, unique = true)
    private String name;

    /**
     * The ISO 3166-1 alpha-3 code of the country.
     * This field is required and can have a maximum length of 3 characters.
     */
    @NotNull(message = "Country code is required")
    @NotBlank(message = "Country code cannot be blank")
    @Size(max = 3, message = "Country code must be 3 characters or less")
    @Column(name = "code", length = 3, nullable = false)
    private String code;

    /**
     * The states or regions within the country.
     * This field is lazily loaded and cascades remove operations.
     */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("name asc")
    private Set<State> states = new HashSet<>();


    public void addState(State state) {
        state.setCountry(this);
        this.states.add(state);
    }

}