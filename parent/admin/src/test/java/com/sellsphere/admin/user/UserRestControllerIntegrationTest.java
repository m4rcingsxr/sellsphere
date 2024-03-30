package com.sellsphere.admin.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserRestControllerIntegrationTest {

    @MockBean
    private UserService service;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @WithMockUser(roles = "ADMIN")
    void testIsEmailUniqueTrue() throws Exception {
        Integer userId = 1;
        String email = "test@example.com";

        when(service.isEmailUnique(userId, email)).thenReturn(true);

        mockMvc.perform(post("/users/check_uniqueness").with(csrf().asHeader())
                                .param("id", userId.toString())
                                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIsEmailUniqueFalse() throws Exception {
        Integer userId = 1;
        String email = "test@example.com";

        when(service.isEmailUnique(userId, email)).thenReturn(false);

        mockMvc.perform(post("/users/check_uniqueness").with(csrf().asHeader())
                                .param("id", userId.toString())
                                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIsEmailUniqueWithException() throws Exception {
        Integer userId = 1;
        String email = "test@example.com";

        when(service.isEmailUnique(userId, email)).thenThrow(new RuntimeException());

        mockMvc.perform(post("/users/check_uniqueness")
                                .param("id", userId.toString()).with(csrf().asHeader())
                                .param("email", email))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while checking email uniqueness"));
    }
}