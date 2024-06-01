package com.sellsphere.easyship.payload;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Pagination {
    private int page;
    private String next;
    private int count;
}