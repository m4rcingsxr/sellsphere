package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Represents a configuration setting for the application.
 * Each setting has a key, value, and category.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "settings")
public class Setting {

    /**
     * The unique key for the setting.
     * This key is used to identify the setting.
     */
    @Id
    @Column(name = "setting_key", nullable = false, length = 128)
    @NotNull(message = "Key must not be null")
    @Size(max = 128, message = "Key must be less than or equal to 128 characters")
    private String key;

    /**
     * The value associated with the key.
     * This value stores the actual configuration data.
     */
    @Column(name = "value", nullable = false, length = 1024)
    @NotNull(message = "Value must not be null")
    @Size(max = 1024, message = "Value must be less than or equal to 1024 characters")
    private String value;

    /**
     * The category of the setting.
     * This indicates the group to which the setting belongs, such as 'GENERAL', 'CURRENCY', etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 45, nullable = false)
    @NotNull(message = "Category must not be null")
    private SettingCategory category;

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
        Setting setting = (Setting) o;
        return getKey() != null && Objects.equals(getKey(), setting.getKey());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
