package com.sellsphere.common.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Entity representing a state or region within a country.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "states")
public class State extends IdentifiedEntity {

    /**
     * The name of the state or region.
     */
    @NotNull(message = "State name is required.")
    @Size(max = 255, message = "State name must be 255 characters or less")
    @Column(name = "name", length = 255, nullable = false, unique = true)
    private String name;

    /**
     * The country to which this state or region belongs.
     */
    @NotNull(message = "Country is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        State state = (State) o;
        return getId() != null && Objects.equals(getId(), state.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}