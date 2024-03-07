package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users")
public class User extends IdentifiedEntity {

    @NotNull(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    @Column(length = 128, nullable = false, unique = true)
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

    @Column(name = "main_image")
    private String mainImage;

    @Column(nullable = false)
    private boolean enabled;

    @NotEmpty(message = "At least one role must be assigned to the user.")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();


}

