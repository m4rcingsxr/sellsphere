package com.sellsphere.admin.category;

import com.sellsphere.common.entity.CategoryIcon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryIconRepository extends JpaRepository<CategoryIcon, Integer> {
}
