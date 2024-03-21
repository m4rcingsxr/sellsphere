package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserRepositoryTest {

    private final UserRepository userRepository;

    private final EntityManager entityManager;
    private final Map<String, Role> roles = new HashMap<>();

    @Autowired
    public UserRepositoryTest(EntityManager entityManager, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        initializeRoles();
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

    @Test
    void whenFindUserByEmail_thenReturnUser() {

        // Given
        String expectedEmail = "john.doe@example.com";
        Set<Role> expectedRoles = getRoles("ROLE_ADMIN", "ROLE_SALESPERSON");

        // When
        Optional<User> user = userRepository.findByEmail(expectedEmail);

        // Then
        assertTrue(user.isPresent(), "User should be present");
        assertEquals(expectedEmail, user.get().getEmail(), "Email should match");
        assertNotNull(user.get().getRoles(), "User roles should not be null");
        assertFalse(user.get().getRoles().isEmpty(), "User should have at least one role");
        assertIterableEquals(expectedRoles, user.get().getRoles(), "Roles should match");
    }

    @Test
    void givenNotExistingEmail_whenFindUserByEmail_thenReturnEmptyOptional() {

        // Given
        String email = "notfound@example.com";

        // When
        Optional<User> user = userRepository.findByEmail(email);

        // Then
        assertTrue(user.isEmpty(), "User should not be present");
    }

    @Test
    void whenFindById_thenReturnUser() {

        // Given
        Integer userId = 6;
        String expectedEmail = "diana.miller@example.com";
        Set<Role> expectedRoles = getRoles("ROLE_ADMIN", "ROLE_SHIPPER");

        // When
        Optional<User> foundUser = userRepository.findById(userId);

        // Then
        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals(expectedEmail, foundUser.get().getEmail(), "Email should match");
        assertIterableEquals(expectedRoles, foundUser.get().getRoles(), "Roles should match");
    }

    @Test
    void whenFindByIdWithNonExistingId_thenReturnEmpty() {

        // Given
        int userId = -1;

        // When
        Optional<User> notFoundUser = userRepository.findById(userId);

        // Then
        assertFalse(notFoundUser.isPresent(), "Non-existing user should not be found");
    }

    @Test
    void whenFindAll_thenShouldReturnUsers() {

        // Given
        Integer expectedSize = 10;

        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertEquals(expectedSize, allUsers.size(), "Should be 10 users");
    }


    @Test
    void whenSaveUser_thenUserIsSaved() {

        // Given
        int expectedId = 11;
        String expectedEmail = "JohnDoe@example.com";
        Role admin = getRole("ROLE_ADMIN");

        User newUser = new User();
        newUser.setEmail(expectedEmail);
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setPassword("password");
        newUser.addRole(admin);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId(), "Saved user should have a non-null ID");
        assertEquals(expectedId, savedUser.getId(), "Saved user should have a non-null ID");
        assertEquals(expectedEmail, savedUser.getEmail(), "Email should match");
        assertFalse(savedUser.getRoles().isEmpty(), "User should have at least one role");
        assertFalse(savedUser.isEnabled(), "User shouldn't be enabled by default");
    }

    @Test
    void whenDeleteById_thenUserIsDeleted() {

        // Given
        int existingUserId = 1; // Sql
        Optional<User> user = userRepository.findById(existingUserId);
        assertTrue(user.isPresent(), "User should be present");

        // When
        userRepository.deleteById(existingUserId);
        user = userRepository.findById(existingUserId);

        // Then
        assertTrue(user.isEmpty(), "User should not be present");
    }

    @Test
    void whenCount_thenReturnCorrectNumber() {
        // Given
        long expectedCount = 10;

        // When
        long actualCount = userRepository.count();

        // Then
        assertEquals(expectedCount, actualCount, "User count should match expected value");
    }

    @Test
    void whenExistsById_thenReturnTrue() {

        // given
        Integer userId = 1;

        // when
        boolean exists = userRepository.existsById(userId);

        // then
        assertTrue(exists, "User should exist");
    }

    private void initializeRoles() {
        List<Role> roles = entityManager
                .createQuery("SELECT r FROM Role r", Role.class)
                .getResultList();

        roles.forEach(role -> this.roles.put(role.getName(), role));
    }


}