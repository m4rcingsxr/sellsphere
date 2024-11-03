package com.sellsphere.provider.customer.external;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String key;

    /**
     * The value associated with the key.
     * This value stores the actual configuration data.
     */
    @Column(name = "\"value\"", nullable = false, length = 2048)
    @NotNull(message = "Value must not be null")
    @Size(max = 2048, message = "Value must be less than or equal to 1024 characters")
    private String value;

    /**
     * The category of the setting.
     * This indicates the group to which the setting belongs, such as 'GENERAL', 'CURRENCY', etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 45, nullable = false)
    @NotNull(message = "Category must not be null")
    private SettingCategory category;


}
