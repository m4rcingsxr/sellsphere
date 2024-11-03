package com.sellsphere.admin.user;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String DEFAULT_REDIRECT_URL = "/users/page/0?sortField=firstName&sortDir=asc";

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenRootUrl_whenAccessed_thenRedirectToUserList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));
    }

    @Test
    void givenNewUserUrl_whenAccessed_thenDisplayCreateUserForm() throws Exception {
        mockMvc.perform(get("/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user_form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("pageTitle", "Create New User"));
    }

    @Test
    void givenExistingUserId_whenEditUserUrlAccessed_thenDisplayEditUserForm() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        Role admin = new Role();
        admin.setId(1);
        admin.setName("ADMIN");
        List<Role> roles = List.of(admin);

        given(userService.get(1)).willReturn(user);
        given(userService.listAllRoles()).willReturn(roles);

        mockMvc.perform(get("/users/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user_form"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("pageTitle", "Edit User [ID: 1]"))
                .andExpect(model().attributeExists("roleList"));
    }

    @Test
    void givenValidUserAndFile_whenSaveUser_thenRedirectToUserList() throws Exception {
        // Mock the file upload
        MockMultipartFile file = new MockMultipartFile("newImage", "test.png",
                                                       MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(Set.of(new Role(1, "Admin")));

        willAnswer(invocation -> null).given(userService).save(any(User.class), any());

        mockMvc.perform(multipart("/users/save")
                                .file(file)
                                .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL + "&keyword=test"));

        then(userService).should().save(any(User.class), any());
    }

    @Test
    void givenExistingUserId_whenDeleteUser_thenRedirectToUserList() throws Exception {
        willDoNothing().given(userService).delete(1);

        mockMvc.perform(get("/users/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(userService).should().delete(1);
    }

    @Test
    void givenExistingUserIdAndStatus_whenUpdateEnabledStatus_thenRedirectToUserList() throws Exception {
        willDoNothing().given(userService).updateUserEnabledStatus(1, true);

        mockMvc.perform(get("/users/{id}/enabled/{status}", 1, true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(DEFAULT_REDIRECT_URL));

        then(userService).should().updateUserEnabledStatus(1, true);
    }

    @Test
    void givenValidExportFormat_whenExportUsers_thenReturnFile() throws Exception {
        mockMvc.perform(get("/users/export/csv"))
                .andExpect(status().isOk());

        then(userService).should().listAll("id", Sort.Direction.ASC);
    }

    @Test
    void givenValidPageNumber_whenListUsersByPage_thenReturnUsersForSpecificPage() throws Exception {
        int pageNum = 1;
        String sortField = "firstName";
        String sortDir = "asc";
        String keyword = "john";

        willDoNothing().given(userService).listPage(eq(pageNum), any(PagingAndSortingHelper.class));

        mockMvc.perform(get("/users/page/{pageNum}", pageNum)
                                .param("sortField", sortField)
                                .param("sortDir", sortDir)
                                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("user/users"));

        then(userService).should().listPage(eq(pageNum), any(PagingAndSortingHelper.class));
    }
}
