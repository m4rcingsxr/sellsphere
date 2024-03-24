package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Role;
import jakarta.persistence.EntityManager;

import java.util.*;

public class TestUserHelper {

    private final Map<String, Role> roles = new HashMap<>();
    private final EntityManager entityManager;

    public TestUserHelper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private void initializeRoles() {
        List<Role> roles = entityManager
                .createQuery("SELECT r FROM Role r", Role.class)
                .getResultList();

        roles.forEach(role -> this.roles.put(role.getName(), role));
    }

    public Role getRole(String name) {
        return this.roles.get(name);
    }

    public Set<Role> getRoles(String... names) {
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            roles.add(this.roles.get(name));
        }

        return roles;
    }

}
