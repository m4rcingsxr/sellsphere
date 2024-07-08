package com.sellsphere.admin.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ReportUnit {

    private String identifier;
    private BigDecimal grossSales;
    private BigDecimal netSales;
    private int ordersCount;
    private int productsCount;

    public void addGrossSales(BigDecimal amount) {
        if(this.grossSales == null) this.grossSales = new BigDecimal(0);
        this.grossSales = this.grossSales.add(amount);
    }

    public void addNetSales(BigDecimal amount) {
        if(this.netSales == null) this.netSales = new BigDecimal(0);
        this.netSales = this.netSales.add(amount);
    }

    public void increaseOrdersCount() {
        this.ordersCount++;
    }

    public void increaseProductsCount() {
        this.productsCount++;
    }
}
