package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;
import util.PagingTestHelper;


@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Sql(scripts = {"classpath:sql/roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:sql/users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserServiceIntegrationTest {

    private final UserService userService;

    @Autowired
    public UserServiceIntegrationTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    void whenListPage_thenReturnPageOfUsers() {

        // Given
        int expectedTotalElements = 10;
        int expectedPages = 2;
        int expectedContentSize = 5;

        // When
        Page<User> users = userService.listPage(0, "firstName", Constants.SORT_ASCENDING);

        // Then
        PagingTestHelper.assertPagingResults(users, expectedContentSize, expectedPages,
                                             expectedTotalElements, "firstName", true
        );
    }

}