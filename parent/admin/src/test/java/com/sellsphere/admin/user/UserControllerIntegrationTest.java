package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private static final String EXPECTED_REDIRECT_URL = "/users/page/0?sortField=firstName" +
            "&sortDir=asc";

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFirstPage_ShouldRedirectToDefaultUrl() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().is3xxRedirection()).andExpect(
                redirectedUrl(EXPECTED_REDIRECT_URL));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPageWithKeyword_ShouldReturnCorrectPage() throws Exception {
        int testPageNum = 0;

        mockMvc.perform(
                get("/users/page/{pageNum}", testPageNum).param("sortField", "firstName").param(
                        "sortDir", "asc").param("keyword", "example")).andExpect(
                status().isOk()).andExpect(
                model().attributeExists("userList", "totalPages", "totalItems", "sortDir",
                                        "sortField", "keyword"
                )).andExpect(model().attribute("userList", hasSize(10))).andExpect(
                model().attribute("totalItems", is(10L))).andExpect(
                model().attribute("totalPages", is(1))).andExpect(view().name("user/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showUserForm_WhenNewUser_ShouldDisplayFormWithDefaultUser() throws Exception {
        String expectedUserFormTemplate = "user/user_form";

        mockMvc.perform(get("/users/new")).andExpect(status().isOk()).andExpect(
                view().name(expectedUserFormTemplate)).andExpect(
                model().attributeExists("user", "roleList", "pageTitle")).andExpect(
                model().attribute("pageTitle", "Create New User")).andExpect(
                model().attribute("user", hasProperty("enabled", is(false))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showUserForm_WhenExistingUser_ShouldDisplayFormWithUserData() throws Exception {
        int existingUserId = 1;
        String expectedUserFormTemplate = "user/user_form";

        mockMvc.perform(get("/users/edit/{id}", existingUserId)).andExpect(
                status().isOk()).andExpect(view().name(expectedUserFormTemplate)).andExpect(
                model().attributeExists("user", "roleList", "pageTitle")).andExpect(
                model().attribute("pageTitle", is("Edit User [ID: " + existingUserId + "]")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldRedirectWithSuccessMessage() throws Exception {
        Integer userId = 1;

        mockMvc.perform(get("/users/delete/{id}", userId)).andExpect(
                status().is3xxRedirection()).andExpect(
                redirectedUrl(EXPECTED_REDIRECT_URL)).andExpect(
                flash().attribute(Constants.SUCCESS_MESSAGE,
                                  "The user [ID: " + userId + "] has been deleted " + "successfully"
                ));

        User user = entityManager.find(User.class, userId);
        assertNull(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserEnabledStatus_ShouldUpdateStatusAndRedirectWithSuccessMessage()
            throws Exception {
        Integer userId = 1;
        boolean newStatus = false;

        mockMvc.perform(get("/users/{id}/enabled/{status}", userId, newStatus)).andExpect(
                status().is3xxRedirection()).andExpect(
                redirectedUrl(EXPECTED_REDIRECT_URL)).andExpect(
                flash().attribute(Constants.SUCCESS_MESSAGE,
                                  "The user [ID: " + userId + "] has been disabled"
                ));

        // Additional verification: Check if the user's status has been updated.
        User userAfterUpdate = entityManager.find(User.class, userId);
        assertNotNull(userAfterUpdate, "User should exist after update.");
        assertEquals(newStatus, userAfterUpdate.isEnabled(),
                     "User's enabled status should be updated."
        );
    }

}
