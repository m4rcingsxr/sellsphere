package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.User;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static util.PagingTestHelper.assertPagingResults;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void when_listPage_then_returnPageOfUsers() {
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
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(pageNum, UserService.USERS_PER_PAGE), users.size());
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        // When
        Page<User> result = userService.listPage(pageNum, sortField, sortDirection);

        // Then
        assertPagingResults(result, users.size(), 1, users.size(), sortField, true);
    }



}
