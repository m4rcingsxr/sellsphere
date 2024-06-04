package com.sellsphere.common.entity.payload;

import com.sellsphere.common.entity.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StateDTO {

    private Integer id;
    private String name;
    private Integer countryId;

    public StateDTO(State state) {
        this.id = state.getId();
        this.name = state.getName();
        this.countryId = state.getCountry().getId();
    }
}
