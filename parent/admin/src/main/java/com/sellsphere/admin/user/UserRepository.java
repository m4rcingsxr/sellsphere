package com.sellsphere.admin.user;

import com.sellsphere.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.id, ' ',u.email, ' ', u.firstName, ' ', " +
            "u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);

    Optional<User> findByEmail(String username);

}
