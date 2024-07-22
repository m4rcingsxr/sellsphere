package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.FooterSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FooterSectionRepository extends JpaRepository<FooterSection, Integer> {
    Optional<FooterSection> findBySectionNumber(Integer sectionNumber);

}
