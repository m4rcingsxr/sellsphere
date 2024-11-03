package com.sellsphere.admin.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserRestControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidEmailAndUserId_whenCheckEmailUniqueness_thenReturnTrue() throws Exception {
        given(userService.isEmailUnique(anyInt(), anyString())).willReturn(true);

        mockMvc.perform(post("/users/email/unique-check")
                                .param("id", "1")
                                .param("email", "test@example.com")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(userService).should().isEmailUnique(1, "test@example.com");
    }

    @Test
    void givenValidEmail_whenCheckEmailUniquenessWithoutUserId_thenReturnTrue() throws Exception {
        given(userService.isEmailUnique(null, "test@example.com")).willReturn(true);

        mockMvc.perform(post("/users/email/unique-check")
                                .param("email", "test@example.com")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        then(userService).should().isEmailUnique(null, "test@example.com");
    }

    @Test
    void givenNonUniqueEmail_whenCheckEmailUniqueness_thenReturnFalse() throws Exception {
        given(userService.isEmailUnique(anyInt(), anyString())).willReturn(false);

        mockMvc.perform(post("/users/email/unique-check")
                                .param("id", "1")
                                .param("email", "existing@example.com")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        then(userService).should().isEmailUnique(1, "existing@example.com");
    }

    @Test
    void givenInvalidEmailLength_whenCheckEmailUniqueness_thenThrowIllegalArgumentException() throws Exception {
        mockMvc.perform(post("/users/email/unique-check")
                                .param("email", "a".repeat(129) + "@example.com")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
