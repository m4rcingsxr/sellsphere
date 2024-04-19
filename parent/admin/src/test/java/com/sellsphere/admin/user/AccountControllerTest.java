package com.sellsphere.admin.user;

import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void givenValidUser_whenShowAccountForm_thenReturnAccountFormView() throws Exception {
        // given
        when(userService.get(anyString())).thenReturn(mockUser);
        when(userService.listAllRoles()).thenReturn(List.of());

        // when
        mockMvc.perform(get("/account").param("email", "test@example.com"))
                // then
                .andExpect(status().isOk())
                .andExpect(view().name("user/account_form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roleList"));

        verify(userService, times(1)).get("test@example.com");
        verify(userService, times(1)).listAllRoles();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void givenValidUser_whenUpdateUser_thenRedirectToAccountForm() throws Exception {
        // given
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(userService.save(any(User.class), any(MultipartFile.class))).thenReturn(mockUser);

        // when
        mockMvc.perform(multipart("/account/update")
                                .file("newImage", mockFile.getBytes())
                                .param("email", "test@example.com")
                                .flashAttr("user", mockUser)
                                .with(csrf()))
                // then
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account?email=test@example.com"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).save(any(User.class), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(username = "wronguser@example.com")
    void givenWrongEmail_whenShowAccountForm_thenReturn403() throws Exception {
        // given
        when(userService.get(anyString())).thenReturn(mockUser);
        when(userService.listAllRoles()).thenReturn(List.of());

        // when
        mockMvc.perform(get("/account").param("email", "test@example.com"))
                // then
                .andExpect(status().isForbidden());

        verify(userService, times(0)).get(anyString());
        verify(userService, times(0)).listAllRoles();
    }
}