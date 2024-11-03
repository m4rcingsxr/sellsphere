package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

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

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        User foundUser = userService.get(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    void givenNonExistingUserId_whenGetUser_thenThrowUserNotFoundException() {
        Integer userId = -1;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.get(userId));
    }

    @Test
    void givenNewUserWithFile_whenSave_thenFileShouldBeSaved() throws IOException, UserNotFoundException {
        // Given
        User newUser = new User();
        newUser.setPassword("plaintext");

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("test.png");

        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(passwordEncoder.encode(anyString())).willReturn("encrypted");

        try (MockedStatic<FileService> mockedFileService = mockStatic(FileService.class)) {
            // When
            userService.save(newUser, file);

            // Then
            then(userRepository).should().save(any(User.class));
            mockedFileService.verify(() -> FileService.saveSingleFile(eq(file), anyString(), eq("test.png")));
            then(passwordEncoder).should().encode("plaintext");
        }
    }

    @Test
    void givenExistingUserWithoutFile_whenSave_thenUserShouldBeSavedWithoutPasswordChange()
            throws IOException, UserNotFoundException {

        // Given
        User existingUser = new User();
        existingUser.setId(1);

        given(userRepository.findById(existingUser.getId())).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.save(existingUser, null);

        // Then
        then(userRepository).should().save(any(User.class));
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void givenNewUserWithPassword_whenSave_thenPasswordShouldBeEncryptedAndSaved() throws UserNotFoundException {
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
        then(userRepository).should().save(argThat(user -> "encrypted_password".equals(user.getPassword())));

        assertEquals("encrypted_password", savedUser.getPassword(), "Password should be encrypted");
        assertNotNull(savedUser.getId(), "Saved user should have an ID set");
    }

    @Test
    void givenExistingUserWithNewPassword_whenSave_thenPasswordShouldBeUpdated() throws UserNotFoundException {
        // Given
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPassword("newPassword");

        given(passwordEncoder.encode(anyString())).willAnswer(invocation -> "encrypted_" + invocation.getArgument(0));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.save(existingUser);

        // Then
        then(passwordEncoder).should().encode("newPassword");
        then(userRepository).should().save(existingUser);
        assertEquals("encrypted_newPassword", updatedUser.getPassword(), "New password should be encrypted and updated");
    }

    @Test
    void givenExistingUserId_whenDelete_thenUserShouldBeDeleted() throws UserNotFoundException {
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
    void givenNonExistingUserId_whenDelete_thenThrowUserNotFoundException() {
        // Given
        Integer nonExistingUserId = -1;

        given(userRepository.findById(nonExistingUserId)).willReturn(Optional.empty());

        // When
        assertThrows(UserNotFoundException.class, () -> userService.delete(nonExistingUserId));

        // Then
        then(userRepository).should().findById(nonExistingUserId);
        then(userRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void givenExistingUserId_whenUpdateUserEnabledStatus_thenStatusShouldBeUpdated() throws UserNotFoundException {
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

        User capturedUser = userCaptor.getValue();
        assertEquals(newStatus, capturedUser.isEnabled(), "The enabled status of the user should be updated.");
    }

    @Test
    void givenNonExistingUserId_whenUpdateUserEnabledStatus_thenThrowUserNotFoundException() {
        // Given
        Integer nonExistingUserId = 99;

        given(userRepository.findById(nonExistingUserId)).willReturn(Optional.empty());

        // When
        assertThrows(UserNotFoundException.class, () -> userService.updateUserEnabledStatus(nonExistingUserId, true));

        // Then
        then(userRepository).should().findById(nonExistingUserId);
        then(userRepository).shouldHaveNoMoreInteractions();
    }
}
