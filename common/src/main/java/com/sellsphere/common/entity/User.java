package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users")
public class User extends IdentifiedEntity {

    @NotNull(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    @Column(name = "email", length = 128, nullable = false, unique = true)
    private String email;

    @NotNull(message = "First name is required.")
    @Size(min = 1, max = 45, message = "First name must be between 1 and 45 characters.")
    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    @NotNull(message = "Last name is required.")
    @Size(min = 1, max = 45, message = "Last name must be between 1 and 45 characters.")
    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

    @NotNull(message = "Password is required.")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters.")
    @Column(name = "user_password", length = 64, nullable = false)
    private String password;

    @Column(name = "main_image", length = 256, nullable = true)
    private String mainImage;

    @Column(name = "enabled", nullable = false, columnDefinition = "TINYINT")
    private boolean enabled;

    @NotEmpty(message = "At least one role must be assigned to the user.")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        roles.add(role);
    }

    @Transient
    public String getMainImagePath() {
        return Constants.S3_BASE_URI + (id == null || mainImage == null ?
                "/default.png" :
                "/user-photos/" + this.id + "/" + this.mainImage);
    }

    @Transient
    public String getRoleNames() {
        return this.roles.stream()
                .map(Role::getSimpleName)
                .collect(Collectors.joining(",", "[", "]"));
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
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}

