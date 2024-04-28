package com.sellsphere.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CountryDTO {

    private Integer id;
    private String name;
    private String code;

    public CountryDTO(Country country) {
        this.id = country.getId();
        this.name = country.getName();
        this.code = country.getCode();
    }

}
