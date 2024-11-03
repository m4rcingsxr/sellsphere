package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setRoles(Set.of(new Role(1, "Admin")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void givenValidUser_whenShowAccountForm_thenReturnAccountFormView() throws Exception {
        // Given
        given(userService.get(anyString())).willReturn(mockUser);
        given(userService.listAllRoles()).willReturn(List.of());

        // When
        mockMvc.perform(get("/account").param("email", "test@example.com"))
                // Then
                .andExpect(status().isOk())
                .andExpect(view().name("user/account_form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roleList"));

        then(userService).should(times(1)).get("test@example.com");
        then(userService).should(times(1)).listAllRoles();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void givenValidUser_whenUpdateUser_thenRedirectToAccountForm() throws Exception {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("newImage", "test.png",
                                                           "image/png", "some-image-content".getBytes());

        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPassword("PlainText123");
        mockUser.setMainImage("test.png");
        mockUser.setRoles(Set.of(new Role(1, "Admin")));

        given(userService.save(any(User.class), any(MultipartFile.class))).willReturn(mockUser);

        // When
        mockMvc.perform(multipart("/account/update")
                                .file(mockFile)
                                .param("email", "test@example.com")
                                .flashAttr("user", mockUser)
                                .with(csrf()))
                // Then
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account?email=test@example.com"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify
        then(userService).should(times(1)).save(any(User.class), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(username = "wronguser@example.com")
    void givenWrongEmail_whenShowAccountForm_thenReturn403() throws Exception {
        // When
        mockMvc.perform(get("/account").param("email", "test@example.com"))
                // Then
                .andExpect(status().isForbidden());

        then(userService).shouldHaveNoInteractions();
    }
}
