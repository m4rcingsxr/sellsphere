package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import util.PagingTestHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

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

    @Mock
    private PagingAndSortingHelper pagingAndSortingHelper;

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

    @Test
    void deleteExistingUser_ShouldCallRepositoryDelete()
            throws UserNotFoundException {
        // Given
        Integer userId = 1;
        User existingUser = new User();
        existingUser.setId(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

        // When
        userService.delete(userId);

        // Then
        then(userRepository).should().findById(userId);
        then(userRepository).should().delete(existingUser);
    }

    @Test
    void deleteNonExistingUser_ShouldThrowUserNotFoundException() {
        // Given
        Integer nonExistingUserId = -1;
        given(userRepository.findById(nonExistingUserId)).willReturn(Optional.empty());

        // When
        Executable thrown = () -> userService.delete(nonExistingUserId);

        // Then
        assertThrows(UserNotFoundException.class, thrown);
        then(userRepository).should().findById(nonExistingUserId);
        then(userRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void updateUserEnabledStatus_WhenUserExists_ShouldUpdateStatus()
            throws UserNotFoundException {

        // Given
        Integer userId = 1;
        boolean newStatus = true;
        User user = new User();
        user.setId(userId);
        user.setEnabled(!newStatus);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        userService.updateUserEnabledStatus(userId, newStatus);

        // Then
        then(userRepository).should().findById(userId);
        then(userRepository).should().save(userCaptor.capture());

        // Assert that the captured User object has the correct enabled status
        User capturedUser = userCaptor.getValue();
        assertEquals(newStatus, capturedUser.isEnabled(),
                     "The enabled status of the user should be updated."
        );
    }

    @Test
    void updateUserEnabledStatus_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        Integer nonExistingUserId = 99;
        given(userRepository.findById(nonExistingUserId)).willReturn(
                Optional.empty());

        // When
        Executable thrown = () -> userService.updateUserEnabledStatus(
                nonExistingUserId, true);

        // Then
        assertThrows(UserNotFoundException.class, thrown);
        then(userRepository).should().findById(nonExistingUserId);
        then(userRepository).shouldHaveNoMoreInteractions();
    }

}
