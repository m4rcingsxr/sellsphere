package com.sellsphere.admin.article;

import com.sellsphere.common.entity.FooterSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FooterSectionRepository extends JpaRepository<FooterSection, Integer> {

    Optional<FooterSection> findBySectionNumber(Integer sectionNumber);

}
