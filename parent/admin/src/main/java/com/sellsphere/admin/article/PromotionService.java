package com.sellsphere.admin.article;

import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.Promotion;
import com.sellsphere.common.entity.PromotionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    /**
     * Retrieves a promotion by its name.
     *
     * @param name the name of the promotion.
     * @return the found Promotion.
     * @throws PromotionNotFoundException if no promotion with the given name is found.
     */
    public Promotion getByName(String name) throws PromotionNotFoundException {
        return promotionRepository.findByName(name)
                .orElseThrow(() -> new PromotionNotFoundException("Promotion not found with name: " + name));
    }

    /**
     * Saves a promotion entity to the repository.
     *
     * @param promotion the promotion to save.
     * @return the saved Promotion.
     */
    public Promotion save(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    /**
     * Retrieves a promotion by its associated article.
     *
     * @param article the associated article.
     * @return the found Promotion.
     * @throws PromotionNotFoundException if no promotion is found for the given article.
     */
    public Promotion getByArticle(Article article) throws PromotionNotFoundException {
        PromotionNotFoundException promotionNotFoundException = new PromotionNotFoundException(
                "Promotion not found with article: " + article);
        if(article.getPromotion() == null) {
            throw promotionNotFoundException;
        }
        return promotionRepository.findByName(article.getPromotion().getName())
                .orElseThrow(() -> promotionNotFoundException);
    }

    /**
     * Retrieves all promotions, sorted by name in ascending order.
     *
     * @return a list of all promotions sorted by name.
     */
    public List<Promotion> listAll() {
        return promotionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * Deletes a given promotion.
     *
     * @param promotion the promotion to delete.
     */
    public void delete(Promotion promotion) {
        promotionRepository.delete(promotion);
    }


    /**
     * Retrieves a promotion by its ID.
     *
     * @param id the ID of the promotion.
     * @return the found Promotion.
     * @throws PromotionNotFoundException if no promotion is found with the given ID.
     */
    public Promotion getById(Integer id) throws PromotionNotFoundException {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException("Promotion not found with ID: " + id));
    }
}
