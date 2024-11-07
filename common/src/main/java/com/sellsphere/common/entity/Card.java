package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "cards")
public class Card extends IdentifiedEntity {

    @Column(name = "stripe_id", nullable = false)
    private String stripeId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "exp_month", nullable = false)
    private Long expMonth;

    @Column(name = "exp_year", nullable = false)
    private Long expYear;

    @Column(name = "funding")
    private String funding;

    @Column(name = "last_4", nullable = false)
    private String last4;

    @Column(name = "created_time", nullable = false)
    private Long created;

    @Transient
    public String getLogoPath() {
        return Constants.S3_BASE_URI +  "/card-photos/" + brand + ".jpg";
    }

    @Transient
    public boolean isExpired() {
        Month month = Month.of(expMonth.intValue());
        Year year = Year.of(expYear.intValue());

        // Create a date representing the last day of the given month and year
        LocalDate lastDayOfMonth = LocalDate.of(year.getValue(), month, 1)
                .with(TemporalAdjusters.lastDayOfMonth());

        // Compare it with the current date
        LocalDate now = LocalDate.now();
        return now.isAfter(lastDayOfMonth); // true if now is after the last day of the month
    }

    @Transient
    public String getExpiredDate() {
        Month month = Month.of(expMonth.intValue());
        Year year = Year.of(expYear.intValue());

        // Create a date representing the last day of the given month and year
        LocalDate lastDayOfMonth = LocalDate.of(year.getValue(), month, 1)
                .with(TemporalAdjusters.lastDayOfMonth());

        // Format the date as "month/year"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        return lastDayOfMonth.format(formatter);
    }

}
