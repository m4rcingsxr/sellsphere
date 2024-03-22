package com.sellsphere.admin.user;

import com.sellsphere.admin.PagingHelper;
import com.sellsphere.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public static final int USERS_PER_PAGE = 10;

    public Page<User> listPage(Integer pageNum, String sortField, String sortDirection) {
        PageRequest pageRequest = PagingHelper.getPageRequest(pageNum, USERS_PER_PAGE, sortField, sortDirection);

        return userRepository.findAll(pageRequest);
    }

}
