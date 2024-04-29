package com.sellsphere.provider.customer.external;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a customer client that will log in as a client.
 * This entity will also be used by Keycloak authentication server for authentication and authorization.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The email address of the customer.
     */
    @Column(name = "email", unique = true, length = 45, nullable = false)
    private String email;

    /**
     * The password for the customer's account.
     */
    @Column(name = "customer_password", length = 64, nullable = false)
    private String password;

    /**
     * The first name of the customer.
     */
    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    /**
     * The last name of the customer.
     */
    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

    /**
     * Indicates whether the customer account is enabled.
     */
    @Column(name = "enabled", nullable = false, columnDefinition = "TINYINT")
    private boolean enabled;

    /**
     * Indicates whether the customer's email is verified.
     */
    @Column(name = "email_verified", nullable = false, columnDefinition = "TINYINT")
    private boolean emailVerified;

    /**
     * The date and time when the customer account was created.
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    /**
     * Returns the full name of the customer.
     *
     * @return the full name of the customer.
     */
    @Transient
    public String getFullName() {
        return String.join(" ", this.firstName, this.lastName);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Customer customer = (Customer) o;
        return getId() != null && Objects.equals(getId(), customer.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}
