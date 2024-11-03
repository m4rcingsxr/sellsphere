package com.sellsphere.admin.report;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ReportResponse {

    List<ReportUnit> reports;

    BigDecimal shippingCost;

    BigDecimal fee;

}
