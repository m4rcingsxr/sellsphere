package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class Customer extends IdentifiedEntity {

    /**
     * The email address of the customer.
     */
    @Email(message = "Email should be valid")
    @NotNull(message = "Email must not be null")
    @Size(max = 45, message = "Email must be less than or equal to 45 characters")
    @Column(name = "email", unique = true, length = 45, nullable = false)
    private String email;

    /**
     * The password for the customer's account.
     */
    @NotNull(message = "Password must not be null")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Column(name = "customer_password", length = 64, nullable = false)
    private String password;

    /**
     * The first name of the customer.
     */
    @NotNull(message = "First name must not be null")
    @Size(max = 45, message = "First name must be less than or equal to 45 characters")
    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    /**
     * The last name of the customer.
     */
    @NotNull(message = "Last name must not be null")
    @Size(max = 45, message = "Last name must be less than or equal to 45 characters")
    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

    /**
     * Indicates whether the customer account is enabled.
     */
    @NotNull(message = "Enabled status must not be null")
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * Indicates whether the customer's email is verified.
     */
    @NotNull(message = "Email verified status must not be null")
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    /**
     * The date and time when the customer account was created.
     */
    @NotNull(message = "Created time must not be null")
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