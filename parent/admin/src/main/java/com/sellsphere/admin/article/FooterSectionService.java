package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.ArticleType;
import com.sellsphere.common.entity.FooterItem;
import com.sellsphere.common.entity.FooterSection;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FooterSectionService {

    private final FooterSectionRepository footerSectionRepository;
    private final FooterItemRepository footerItemRepository;
    private final ArticleRepository articleRepository;

    public List<FooterSection> findAll() {
        return footerSectionRepository.findAll(Sort.by(Sort.Direction.ASC, "sectionNumber"));
    }

    @Transactional
    public FooterSection save(Article savedArticle, Integer itemNumber, Integer sectionNumber) {
        Optional<FooterSection> footerSectionOpt = footerSectionRepository.findBySectionNumber(
                sectionNumber);
        if (footerSectionOpt.isPresent()) {
            FooterSection footerSection = footerSectionOpt.get();

            // section number are the same

            // 1) footer item with itemNumber exist + article
            Optional<FooterItem> footerItemOpt = footerSection.getFooterItems()
                    .stream()
                    .filter(item -> item.getItemNumber().equals(itemNumber)).findFirst();
            if (footerItemOpt.isPresent()) {

                // 1.1) article is the same
                FooterItem footerItem = footerItemOpt.get();
                if(footerItem.getArticle().getId().equals(savedArticle.getId())) {
                    return footerSection;
                } else {

                    // 1.2) article is different - set old article to type free
                    footerItem.getArticle().setArticleType(ArticleType.FREE);
                    footerItem.setArticle(savedArticle);

                    return footerSectionRepository.save(footerSection);
                }
            } else {
                // 2) footer item with itemNumber not exist + article
                // 2.1) create new item number with article

                Optional<FooterItem> footerItemOptional = footerItemRepository.findByArticle(savedArticle);
                if(footerItemOptional.isPresent()) {
                    FooterItem footerItem = footerItemOptional.get();
                    FooterSection section = footerItem.getFooterSection();
                    section.removeFooterItem(footerItem);
                    footerSectionRepository.saveAndFlush(section);
                }

                FooterItem footerItem = new FooterItem();
                footerItem.setArticle(savedArticle);
                footerItem.setItemNumber(itemNumber);
                footerSection.addFooterItem(footerItem);
                return footerSectionRepository.save(footerSection);
            }


        } else {
            Optional<FooterItem> footerItemOpt = footerItemRepository.findByArticle(savedArticle);
            if(footerItemOpt.isPresent()) {
                FooterItem footerItem = footerItemOpt.get();
                FooterSection footerSection = footerItem.getFooterSection();
                footerSection.removeFooterItem(footerItem);
                footerSectionRepository.saveAndFlush(footerSection);
            }

            FooterSection footerSection = new FooterSection();
            footerSection.setSectionNumber(sectionNumber);

            FooterItem footerItem = new FooterItem();
            footerItem.setItemNumber(itemNumber);
            footerItem.setArticle(savedArticle);

            footerSection.addFooterItem(footerItem);

            return footerSectionRepository.save(footerSection);
        }
    }


}
