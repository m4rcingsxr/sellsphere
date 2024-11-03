package com.sellsphere.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "roles")
public class Role extends IdentifiedEntity {

    @NotEmpty(message = "Role name is required.")
    @Size(max = 40, message = "Role name must not exceed 40 characters.")
    @Column(name = "name", length = 40, nullable = false, unique = true)
    private String name;

    @NotEmpty(message = "Role description is required.")
    @Size(max = 150, message = "Role description must not exceed 150 characters.")
    @Column(name = "description", length = 150, nullable = false)
    private String description;

    public Role(int id, String name) {
        super(id);
        this.name = name;
    }

    @Transient
    public String getSimpleName() {
        return this.name.replace("ROLE_", "");
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
        Role role = (Role) o;
        return getId() != null && Objects.equals(getId(), role.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}
