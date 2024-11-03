package com.sellsphere.client.article;

import com.sellsphere.common.entity.FooterSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FooterSectionRepository extends JpaRepository<FooterSection, Integer> {

    List<FooterSection> findAllByOrderBySectionNumberAsc();

}
