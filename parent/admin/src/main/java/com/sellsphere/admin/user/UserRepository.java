package com.sellsphere.admin.user;

import com.sellsphere.admin.page.SearchRepository;
import com.sellsphere.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends SearchRepository<User,Integer> {

    @Override
    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.id, ' ',u.email, ' ', u.firstName, ' ', " +
            "u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);

    Optional<User> findByEmail(String username);

    long countAllByEnabledIsTrue();
}
