package com.sellsphere.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Represents an address entity associated with a customer.
 * The Address entity contains information about the address, including
 * the first and last name of the recipient, phone number, address lines, city,
 * state, country, postal code, and whether it is the primary address.
 * It is linked to a specific customer and country.
 */
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "addresses")
public class Address extends IdentifiedEntity{

    /**
     * The first name of the recipient.
     */
    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    /**
     * The last name of the recipient.
     */
    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

    /**
     * The phone number of the recipient.
     */
    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    /**
     * The first line of the address.
     */
    @Column(name = "address_line_1", length = 64, nullable = false)
    private String addressLine1;

    /**
     * The second line of the address (optional).
     */
    @Column(name = "address_line_2", length = 64, nullable = true)
    private String addressLine2;

    /**
     * The city of the address.
     */
    @Column(name = "city", length = 45, nullable = false)
    private String city;

    /**
     * The state or region of the address (optional).
     */
    @Column(name = "state", length = 45, nullable = true)
    private String state;

    /**
     * The country associated with the address.
     */
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    /**
     * The postal code of the address.
     */
    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    /**
     * Indicates whether this is the primary address for the customer.
     */
    @Column(name = "primary_address", nullable = false, columnDefinition = "TINYINT")
    private boolean primary;

    /**
     * The customer associated with this address.
     */
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Returns the full name of the recipient.
     *
     * @return the full name of the recipient.
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the full address as a formatted string.
     *
     * @return the full address.
     */
    @Transient
    public String getFullAddress() {
        return (state != null ? state + " " : "") + postalCode + ", " + country.getName() + "\n" + addressLine1 + (addressLine2 != null ? ", " + addressLine2 : "") + ", " + city;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
                o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
                this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Address address = (Address) o;
        return getId() != null && Objects.equals(getId(), address.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + "firstName = " + firstName + ", " + "lastName =" +
                " " + lastName + ", " + "phoneNumber = " + phoneNumber + ", " + "addressLine1 = " + addressLine1 + ", " + "addressLine2 = " + addressLine2 + ", " + "city = " + city + ", " + "state = " + state + ", " + "country = " + country + ", " + "postalCode = " + postalCode + ", " + ")";
    }
}
