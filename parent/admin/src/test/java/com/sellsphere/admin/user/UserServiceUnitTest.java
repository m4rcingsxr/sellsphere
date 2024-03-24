package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static util.PagingTestHelper.assertPagingResults;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileService fileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistingUserId_whenGetUser_thenReturnUser() throws UserNotFoundException {
        Integer userId = 1;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.get(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    void givenNonExistingUserId_whenGetUser_thenShouldThrowUserNotFoundException() {
        Integer userId = -1;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.get(userId));
    }

    @Test
    void whenListPage_thenReturnPageOfUsers() {

        // Given
        int pageNum = 0;
        String sortField = "firstName";
        String sortDirection = "ASC";

        User user1 = new User();
        user1.setFirstName("Alice");
        User user2 = new User();
        user2.setFirstName("Bob");
        User user3 = new User();
        user3.setFirstName("Charlie");
        User user4 = new User();
        user4.setFirstName("David");
        User user5 = new User();
        user5.setFirstName("Eve");

        List<User> users = Arrays.asList(user1, user2, user3, user4, user5);
        Page<User> userPage = new PageImpl<>(users,
                                             PageRequest.of(pageNum, UserService.USERS_PER_PAGE),
                                             users.size()
        );
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        // When
        Page<User> result = userService.listPage(pageNum, sortField, sortDirection);

        // Then
        assertPagingResults(result, users.size(), 1, users.size(), sortField, true);
    }

    @Test
    void saveNewUserWithFile_ExpectFileSaved() throws IOException, UserNotFoundException {

        // Given
        User newUser = new User();
        newUser.setPassword("plaintext");

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("test.png");

        given(userRepository.save(any(User.class))).willAnswer(
                invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode(anyString())).willReturn("encrypted");

        // When
        userService.save(newUser, file);

        // Then
        then(userRepository).should().save(any(User.class));
        then(fileService).should().saveSingleFile(eq(file), anyString(), eq("test.png"));
        then(passwordEncoder).should().encode("plaintext");
    }

    @Test
    void saveExistingUserWithoutFile_ExpectUserSavedWithoutPasswordChange()
            throws IOException, UserNotFoundException {

        // Given
        User existingUser = new User();
        existingUser.setId(1);

        // This mock return simulates finding the existing user in the database.
        given(userRepository.findById(existingUser.getId()))
                .willReturn(Optional.of(existingUser));

        // This ensures the user is saved and returned as is.
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.save(existingUser, null);

        // Then
        // Verify the user is saved
        then(userRepository).should().save(
                any(User.class));

        // Verify no file operations are performed
        then(fileService).shouldHaveNoInteractions();

        // Verify the password is not re-encrypted
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void saveNewUserWithPassword_ExpectPasswordEncryptedAndSaved() throws UserNotFoundException {

        // Given
        User newUser = new User();
        newUser.setPassword("password");

        given(passwordEncoder.encode("password")).willReturn("encrypted_password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1);
            return user;
        });

        // When
        User savedUser = userService.save(newUser);

        // Then
        then(passwordEncoder).should().encode("password");
        then(userRepository).should().save(
                argThat(user -> "encrypted_password".equals(
                        user.getPassword())));

        assertEquals("encrypted_password", savedUser.getPassword(), "Password should be encrypted");
        assertNotNull(savedUser.getId(), "Saved user should have an ID set");
    }

    @Test
    void saveExistingUserWithNewPassword_ExpectPasswordUpdated() throws UserNotFoundException {

        // Given
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPassword("newPassword");

        given(passwordEncoder.encode(anyString())).willAnswer(
                invocation -> "encrypted_" + invocation.getArgument(0));
        given(userRepository.save(any(User.class))).willAnswer(
                invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.save(existingUser);

        // Then
        then(passwordEncoder).should().encode("newPassword");
        then(userRepository).should().save(existingUser);
        assertEquals("encrypted_newPassword", updatedUser.getPassword(),
                     "New password should be encrypted and updated"
        );
    }

}
