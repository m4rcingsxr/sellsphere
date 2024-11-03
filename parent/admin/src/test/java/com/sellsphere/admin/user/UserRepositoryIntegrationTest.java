package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void givenKeyword_whenFindingAllUsers_thenReturnMatchingUsers() {
        // Given
        String keyword = "john";
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "firstName",
                                                               Sort.Direction.ASC
        );

        // When
        Page<User> result = userRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 1, 1, 1, "firstName", true);
        assertEquals("john.doe@example.com", result.getContent().get(0).getEmail(),
                     "The user's email should match 'john.doe@example.com'."
        );
    }

    @Test
    void givenInvalidKeyword_whenFindingAllUsers_thenReturnEmptyResult() {
        // Given
        String keyword = "nomatch";
        Pageable pageable = PagingTestHelper.createPageRequest(0, 5, "firstName",
                                                               Sort.Direction.ASC
        );

        // When
        Page<User> result = userRepository.findAll(keyword, pageable);

        // Then
        PagingTestHelper.assertPagingResults(result, 0, 0, 0, "firstName", true);
    }

    @Test
    void givenValidEmail_whenFindingUserByEmail_thenReturnUser() {
        // Given
        String email = "john.doe@example.com";

        // When
        Optional<User> result = userRepository.findByEmail(email);

        // Then
        assertTrue(result.isPresent(),
                   "A user with the email 'john.doe@example.com' should be present."
        );
        assertEquals("John", result.get().getFirstName(), "The first name should match 'John'.");
        assertEquals("Doe", result.get().getLastName(), "The last name should match 'Doe'.");
    }

    @Test
    void givenNonExistentEmail_whenFindingUserByEmail_thenReturnEmptyOptional() {
        // Given
        String email = "nonexistent@example.com";

        // When
        Optional<User> result = userRepository.findByEmail(email);

        // Then
        assertFalse(result.isPresent(),
                    "No user should be found with the email 'nonexistent@example.com'."
        );
    }

    @Test
    void givenValidUserId_whenDeletingUserById_thenUserIsDeleted() {
        // Given
        String email = "john.doe@example.com";
        Optional<User> user = userRepository.findByEmail(email);
        assertTrue(user.isPresent(), "User 'john.doe@example.com' should exist.");
        Integer userId = user.get().getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent(), "User with ID " + userId + " should be deleted.");
    }

    @Test
    void givenUsersExist_whenFindingAll_thenReturnAllUsers() {
        // Given
        Pageable pageable = PagingTestHelper.createPageRequest(0, 10, "firstName",
                                                               Sort.Direction.ASC
        );

        // When
        Page<User> users = userRepository.findAll(pageable);

        // Then
        PagingTestHelper.assertPagingResults(users, 10, 1, 10,
                                             "firstName", true
        );
    }

    @Test
    void givenValidUserId_whenFindingUserById_thenReturnCorrectUser() {
        // Given
        String email = "jane.smith@example.com";
        Optional<User> user = userRepository.findByEmail(email);
        assertTrue(user.isPresent(), "User 'jane.smith@example.com' should exist.");
        Integer userId = user.get().getId();

        // When
        Optional<User> result = userRepository.findById(userId);

        // Then
        assertTrue(result.isPresent(), "User with ID " + userId + " should be found.");
        assertEquals("Jane", result.get().getFirstName(), "The first name should match 'Jane'.");
        assertEquals("Smith", result.get().getLastName(), "The last name should match 'Smith'.");
    }

    @Test
    void givenNewUser_whenSavingUser_thenUserIsSavedSuccessfully() {
        // Given
        User newUser = new User();
        newUser.setEmail("new.user@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPassword("password123");
        newUser.setMainImage("default.png");
        newUser.setEnabled(true);

        Role role = new Role();
        role.setName("ROLE_USER");
        newUser.addRole(role);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId(), "User ID should not be null after saving.");

        Optional<User> result = userRepository.findById(savedUser.getId());
        assertTrue(result.isPresent(), "Saved user should be present in the repository.");

        User retrievedUser = result.get();
        assertEquals("new.user@example.com", retrievedUser.getEmail(),
                     "Email should match the saved user."
        );
        assertEquals("New", retrievedUser.getFirstName(),
                     "First name should match the saved user."
        );
        assertEquals("User", retrievedUser.getLastName(), "Last name should match the saved user.");
        assertEquals("default.png", retrievedUser.getMainImage(),
                     "Main image should match the saved user."
        );
        assertTrue(retrievedUser.isEnabled(), "User should be enabled.");
        assertFalse(retrievedUser.getRoles().isEmpty(),
                    "User should have at least one role assigned."
        );
    }

    @Test
    void givenInvalidUserId_whenDeletingUserById_thenNoExceptionIsThrown() {
        // Given
        Integer nonExistentUserId = 99999;

        // When
        userRepository.deleteById(nonExistentUserId);

        // Then
        Optional<User> deletedUser = userRepository.findById(nonExistentUserId);
        assertFalse(deletedUser.isPresent(), "User with ID " + nonExistentUserId + " should not exist.");
    }

}
