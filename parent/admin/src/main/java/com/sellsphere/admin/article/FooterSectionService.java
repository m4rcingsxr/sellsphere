package com.sellsphere.admin.article;

import com.sellsphere.common.entity.*;
import com.sellsphere.common.entity.payload.ArticleMetadata;
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

    /**
     * Retrieves all footer sections sorted by section number in ascending order.
     *
     * @return List of FooterSection objects.
     */
    public List<FooterSection> findAll() {
        return footerSectionRepository.findAll(Sort.by(Sort.Direction.ASC, "sectionNumber"));
    }

    /**
     * Saves or updates a footer section with the given article and footer item details.
     *
     * @param metadata      The article metadata with information related to article placement or article type, products
     * @return The saved FooterSection.
     */
    public FooterSection save(ArticleMetadata metadata) {
        return footerSectionRepository.findBySectionNumber(metadata.getSectionNumber())
                .map(footerSection -> updateFooterSection(footerSection, metadata.getArticle(), metadata.getItemNumber(), metadata.getSectionHeader()[metadata.getSectionNumber() - 1]))
                .orElseGet(() -> createNewFooterSection(metadata.getArticle(), metadata.getItemNumber(), metadata.getSectionNumber(), metadata.getSectionHeader()));
    }

    /**
     * Retrieves a FooterSection by its section number.
     *
     * @param sectionNumber The number of the section.
     * @return Optional containing the FooterSection if found, otherwise empty.
     */
    public Optional<FooterSection> get(Integer sectionNumber) {
        return footerSectionRepository.findBySectionNumber(sectionNumber);
    }

    /**
     * Saves a FooterSection.
     *
     * @param footerSection The FooterSection to save.
     * @return The saved FooterSection.
     */
    public FooterSection save(FooterSection footerSection) {
        return footerSectionRepository.save(footerSection);
    }

    // Helper Methods

    /**
     * Updates an existing footer section with the given article and item details.
     * If the footer item already exists and is linked to the same article, it returns the section.
     * Otherwise, it updates the item or creates a new one as needed.
     *
     * @param footerSection The existing FooterSection to update.
     * @param article       The article to associate with the footer item.
     * @param itemNumber    The number of the footer item.
     * @param sectionHeader The header to set for the footer section.
     * @return The updated FooterSection.
     */
    private FooterSection updateFooterSection(FooterSection footerSection, Article article, Integer itemNumber, String sectionHeader) {
        footerSection.setSectionHeader(sectionHeader);

        // Check if footer item already exists in the section
        footerSection.getFooterItems()
                .stream()
                .filter(item -> item.getItemNumber().equals(itemNumber))
                .findFirst()
                .ifPresentOrElse(
                        footerItem -> updateExistingFooterItem(footerItem, article),
                        () -> addNewFooterItem(footerSection, article, itemNumber)
                );

        return footerSectionRepository.save(footerSection);
    }

    /**
     * Creates a new footer section and associates it with the given article and item details.
     *
     * @param article       The article to associate with the footer item.
     * @param itemNumber    The number of the footer item.
     * @param sectionNumber The number of the new footer section.
     * @param sectionHeader Array of section headers, corresponding to each section number.
     * @return The newly created FooterSection.
     */
    private FooterSection createNewFooterSection(Article article, Integer itemNumber, Integer sectionNumber, String[] sectionHeader) {
        // Remove article from any previous footer section, if it exists
        removeFooterItemIfExists(article);

        FooterSection newFooterSection = new FooterSection();
        newFooterSection.setSectionNumber(sectionNumber);
        newFooterSection.setSectionHeader(sectionHeader[sectionNumber - 1]);

        addNewFooterItem(newFooterSection, article, itemNumber);

        return footerSectionRepository.save(newFooterSection);
    }

    /**
     * Updates an existing footer item, setting its article to the given one. If the article is different,
     * it sets the old article type to FREE before updating.
     *
     * @param footerItem The existing footer item to update.
     * @param article    The new article to associate with the footer item.
     */
    private void updateExistingFooterItem(FooterItem footerItem, Article article) {
        if (!footerItem.getArticle().getId().equals(article.getId())) {
            footerItem.getArticle().setArticleType(ArticleType.FREE);
            footerItem.setArticle(article);
            footerItemRepository.save(footerItem);
        }
    }

    /**
     * Adds a new footer item to the given footer section.
     *
     * @param footerSection The footer section to add the item to.
     * @param article       The article to associate with the footer item.
     * @param itemNumber    The number of the footer item.
     */
    private void addNewFooterItem(FooterSection footerSection, Article article, Integer itemNumber) {
        removeFooterItemIfExists(article);

        FooterItem footerItem = new FooterItem();
        footerItem.setArticle(article);
        footerItem.setItemNumber(itemNumber);
        footerSection.addFooterItem(footerItem);
    }

    /**
     * Removes a footer item associated with the given article if it exists.
     *
     * @param article The article to remove from any existing footer section.
     */
    private void removeFooterItemIfExists(Article article) {
        footerItemRepository.findByArticle(article).ifPresent(footerItem -> {
            FooterSection section = footerItem.getFooterSection();
            section.removeFooterItem(footerItem);
            footerSectionRepository.saveAndFlush(section);
        });
    }

    public FooterItem getFooterItemByArticle(Article article) throws ArticleNotFoundException {
        return footerItemRepository.findByArticle(article).orElseThrow(ArticleNotFoundException::new);
    }

}
