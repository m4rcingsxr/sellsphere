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

    public Promotion get(String name) throws PromotionNotFoundException {
        return promotionRepository.findByName(name).orElseThrow(PromotionNotFoundException::new);
    }

    public Promotion save(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion getByArticle(Article article) throws PromotionNotFoundException {
        return promotionRepository.findByArticle(article).orElseThrow(PromotionNotFoundException::new);
    }

    public List<Promotion> listAll() {
        return promotionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public void delete(Promotion existingPromotion) {
        promotionRepository.delete(existingPromotion);
    }
}
