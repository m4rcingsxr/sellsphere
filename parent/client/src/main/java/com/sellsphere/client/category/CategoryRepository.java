package com.sellsphere.client.category;

import com.sellsphere.common.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByAliasAndEnabledIsTrue(String alias);

    List<Category> findAllByParentIsNullAndEnabledIsTrue();

    Category findByName(String name);
}
