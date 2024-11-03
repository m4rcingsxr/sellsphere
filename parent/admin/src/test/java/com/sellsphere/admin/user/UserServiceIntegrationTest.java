package com.sellsphere.admin.user;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sellsphere.admin.S3Utility;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.method.support.ModelAndViewContainer;
import software.amazon.awssdk.services.s3.S3Client;
import util.PagingTestHelper;
import util.S3TestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(S3MockExtension.class)
@Sql(scripts = {"classpath:sql/roles.sql", "classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserServiceIntegrationTest {

    private static final String BUCKET_NAME = "my-demo-test-bucket";
    private static S3Client s3Client;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    static void setUpS3(final S3Client client) {
        s3Client = client;
        S3Utility.setBucketName(BUCKET_NAME);
        S3Utility.setS3Client(s3Client);

        S3TestUtils.createBucket(s3Client, BUCKET_NAME);
    }

    @Test
    void givenExistingUserId_whenGetUser_thenReturnUser() throws UserNotFoundException {
        // Given
        Integer userId = 1;

        // When
        User user = userService.get(userId);

        // Then
        assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
    }

    @Test
    void givenNonExistingUserId_whenGetUser_thenThrowUserNotFoundException() {
        // Given
        Integer nonExistingUserId = 999;

        // When/Then
        assertThrows(UserNotFoundException.class, () -> userService.get(nonExistingUserId));
    }

    @Test
    void givenExistingEmail_whenGetUserByEmail_thenReturnUser() throws UserNotFoundException {
        // Given
        String email = "jane.smith@example.com";

        // When
        User user = userService.get(email);

        // Then
        assertNotNull(user);
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
    }

    @Test
    void givenNonExistingEmail_whenGetUserByEmail_thenThrowUserNotFoundException() {
        // Given
        String nonExistingEmail = "non.existing@example.com";

        // When/Then
        assertThrows(UserNotFoundException.class, () -> userService.get(nonExistingEmail));
    }

    @Test
    void whenListAllRoles_thenReturnAllRolesSortedByName() {
        // When
        List<Role> roles = userService.listAllRoles();

        // Then
        assertFalse(roles.isEmpty());
        assertEquals(5, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0).getName());
        assertEquals("ROLE_ASSISTANT", roles.get(1).getName());
    }

    @Test
    void whenListAllUsersSorted_thenReturnAllUsersSortedByField() {
        // When
        List<User> users = userService.listAll("firstName", Sort.Direction.ASC);

        // Then
        assertFalse(users.isEmpty());
        assertEquals("Alice", users.get(0).getFirstName());
        assertEquals("Bob", users.get(1).getFirstName());
    }

    @Test
    void givenUniqueEmail_whenCheckEmailUnique_thenReturnTrue() {
        // Given
        String uniqueEmail = "unique.email@example.com";

        // When
        boolean isUnique = userService.isEmailUnique(null, uniqueEmail);

        // Then
        assertTrue(isUnique);
    }

    @Test
    void givenExistingEmail_whenCheckEmailUnique_thenReturnFalse() {
        // Given
        String existingEmail = "john.doe@example.com";

        // When
        boolean isUnique = userService.isEmailUnique(null, existingEmail);

        // Then
        assertFalse(isUnique);
    }

    @Test
    void givenValidUserId_whenUpdateUserEnabledStatus_thenStatusIsUpdated()
            throws UserNotFoundException {
        // Given
        Integer userId = 1;
        boolean newStatus = false;

        // When
        userService.updateUserEnabledStatus(userId, newStatus);

        // Then
        User updatedUser = userService.get(userId);
        assertFalse(updatedUser.isEnabled());
    }

    @Test
    void givenNonExistingUserId_whenUpdateUserEnabledStatus_thenThrowUserNotFoundException() {
        // Given
        Integer nonExistingUserId = 999;

        // When/Then
        assertThrows(UserNotFoundException.class,
                     () -> userService.updateUserEnabledStatus(nonExistingUserId, true)
        );
    }

    @Test
    void givenValidUserId_whenDeleteUser_thenUserIsDeleted() throws UserNotFoundException {
        // Given
        Integer userId = 1;

        // When
        userService.delete(userId);

        // Then
        assertThrows(UserNotFoundException.class, () -> userService.get(userId));
    }

    @Test
    void givenNonExistingUserId_whenDeleteUser_thenThrowUserNotFoundException() {
        // Given
        Integer nonExistingUserId = 999;

        // When/Then
        assertThrows(UserNotFoundException.class, () -> userService.delete(nonExistingUserId));
    }

    @Test
    void givenNewUserWithPasswordAndRoles_whenSaveUser_thenPasswordShouldBeEncodedAndRolesAssigned()
            throws Exception {
        // Given
        User newUser = new User();
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setEmail("test.user@example.com");
        newUser.setPassword("plainPassword");
        newUser.setMainImage("test_image.png");

        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");

        Role roleSales = new Role();
        roleSales.setName("ROLE_SALESPERSON");

        newUser.addRole(roleAdmin);
        newUser.addRole(roleSales);

        // When
        User savedUser = userService.save(newUser, null);

        // Then
        assertNotNull(savedUser);
        assertNotEquals("plainPassword", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("plainPassword", savedUser.getPassword()));

        // Check if the roles are saved
        assertNotNull(savedUser.getRoles());
        assertEquals(2, savedUser.getRoles().size());
    }

    @Test
    void givenNewUserWithFile_whenSaveUser_thenSaveFileInS3AndReturnUser()
            throws IOException, UserNotFoundException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-profile.jpg",
                "image/jpeg",
                "Sample profile image".getBytes()
        );

        User newUser = new User();
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setEmail("test.user@example.com");
        newUser.setPassword("plainPassword");

        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");

        Role roleSales = new Role();
        roleSales.setName("ROLE_SALESPERSON");

        newUser.addRole(roleAdmin);
        newUser.addRole(roleSales);

        // When
        User savedUser = userService.save(newUser, file);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("test-profile.jpg", savedUser.getMainImage());

        // Verify the file is saved in S3
        S3TestUtils.verifyFileContent(s3Client, BUCKET_NAME,
                                      "user-photos/" + savedUser.getId() + "/test-profile.jpg",
                                      file.getInputStream()
        );

        // Check if the password is encoded
        assertNotEquals("plainPassword", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("plainPassword", savedUser.getPassword()));
    }

    @Test
    void givenPageNumber_whenListUsersByPage_thenReturnUsersForSpecificPage() {
        // Given
        int pageNum = 0;
        int expectedPageSize = 10;
        int expectedTotalElements = 10;
        int expectedPages = 1;
        String sortField = "firstName";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "userList", sortField,
                                                                   Sort.Direction.ASC, null
        );

        // When
        userService.listPage(pageNum, helper);

        // Then
        PagingTestHelper.assertPagingResults(helper, expectedPages, expectedPageSize,
                                             expectedTotalElements, sortField, true
        );
    }

    @Test
    void givenKeyword_whenSearchUsers_thenReturnFilteredResults() {
        // Given
        int pageNum = 0;
        String keyword = "john";
        String sortField = "firstName";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "userList", sortField,
                                                                   Sort.Direction.ASC, keyword
        );

        // When
        userService.listPage(pageNum, helper);

        // Then
        List<User> users = helper.getContent();  // Use the new method to get users
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.stream()
                           .allMatch(user -> user.getFirstName().toLowerCase().contains(keyword) ||
                                   user.getLastName().toLowerCase().contains(keyword)));
    }

    @Test
    void givenPageZero_whenListUsersByPage_thenReturnFirstPageUsers() {
        // Given
        String sortField = "firstName";
        PagingAndSortingHelper helper = new PagingAndSortingHelper(new ModelAndViewContainer(),
                                                                   "userList", sortField,
                                                                   Sort.Direction.ASC, null
        );

        // When
        userService.listPage(0, helper);

        // Then
        List<User> users = helper.getContent();  // Use the new method to get users
        assertFalse(users.isEmpty());
        assertEquals("Alice", users.get(0).getFirstName());
    }
}
