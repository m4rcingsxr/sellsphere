package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserServiceIntegrationTest {

    private final UserService userService;

    private final TestUserHelper testUserHelper;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceIntegrationTest(UserService userService, EntityManager entityManager,
                                      PasswordEncoder passwordEncoder) {
        this.testUserHelper = new TestUserHelper(entityManager);
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    void givenExistingUserId_whenGetUser_thenReturnUser() throws UserNotFoundException {
        // given
        Integer userId = 1;

        // when
        User foundUser = userService.get(userId);

        // then
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    void givenNonExistingUserId_whenGetUser_thenShouldThrowUserNotFoundException() {

        // given
        Integer userId = -1;

        // when, then
        assertThrows(UserNotFoundException.class, () -> userService.get(userId));
    }

    @Test
    void whenListPage_thenReturnPageOfUsers() {

        // Given
        int expectedTotalElements = 10;
        int expectedPages = 1;
        int expectedContentSize = 10;

        // When
        Page<User> users = userService.listPage(0, "firstName", Constants.SORT_ASCENDING);

        // Then
        PagingTestHelper.assertPagingResults(users, expectedContentSize, expectedPages,
                                             expectedTotalElements, "firstName", true
        );
    }

    @Test
    void givenNewUserWithoutFile_whenSave_thenUserIsSavedSuccessfully()
            throws IOException, UserNotFoundException {
        // Given
        Set<Role> expectedRoles = testUserHelper.getRoles("ROLE_ADMIN", "ROLE_EDITOR");
        String expectedRawPassword = "password";

        User newUser = new User();
        newUser.setEmail("newusernofile@example.com");
        newUser.setFirstName("NoFile");
        newUser.setLastName("User");
        newUser.setPassword(expectedRawPassword);
        newUser.setRoles(expectedRoles);

        // When
        User savedUser = userService.save(newUser, null);

        // Then
        assertNotNull(savedUser, "Expected saved user to not be null");
        assertNotNull(savedUser.getId(), "Expected saved user to have an ID");
        assertEquals("newusernofile@example.com", savedUser.getEmail(), "Expected email to match");
        assertIterableEquals(savedUser.getRoles(), expectedRoles);
        assertTrue(passwordEncoder.matches(expectedRawPassword, savedUser.getPassword()));
    }


    @Test
    void givenNoExistingId_whenSave_thenUserNotFoundExceptionIsThrown() {
        // Given
        User userWithNonExistentId = new User();
        userWithNonExistentId.setId(999);

        // Expect exception to be thrown
        assertThrows(UserNotFoundException.class, () -> userService.save(userWithNonExistentId));
    }

}