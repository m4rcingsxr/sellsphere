package com.sellsphere.easyship.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsCode {

    private String code;
    private String description;

}
