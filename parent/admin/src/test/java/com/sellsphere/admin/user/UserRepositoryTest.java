package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserRepositoryTest {

    private final UserRepository userRepository;
    private final TestUserHelper testUserHelper;


    @Autowired
    public UserRepositoryTest(EntityManager entityManager, UserRepository userRepository) {
        this.testUserHelper = new TestUserHelper(entityManager);
        this.userRepository = userRepository;
    }



    @Test
    void whenFindUserByEmail_thenReturnUser() {

        // Given
        String expectedEmail = "john.doe@example.com";
        Set<Role> expectedRoles = testUserHelper.getRoles("ROLE_ADMIN", "ROLE_SALESPERSON");

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
        Set<Role> expectedRoles = testUserHelper.getRoles("ROLE_ADMIN", "ROLE_SHIPPER");

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
        Role admin = testUserHelper.getRole("ROLE_ADMIN");

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

    @Test
    void whenListWithPageRequest_thenReturnPageOfUsers() {

        // Given
        String sortField = "firstName";
        PageRequest pageRequest = PagingTestHelper.createPageRequest(0, 5, sortField, Constants.SORT_ASCENDING);
        int expectedTotalElements = 10;
        int expectedPages = 2;
        int expectedContentSize = 5;

        // When
        Page<User> users = userRepository.findAll(pageRequest);

        // Then
        PagingTestHelper.assertPagingResults(users, expectedContentSize, expectedPages, expectedTotalElements, sortField, true);
    }


}