package com.sellsphere.admin.article;

import com.sellsphere.common.entity.PromotionNotFoundException;
import com.sellsphere.common.entity.payload.PromotionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PromotionRestController {

    private final PromotionService promotionService;

    @GetMapping("/promotions/{name}")
    public ResponseEntity<PromotionDTO> getPromotion(@PathVariable("name") String name)
            throws PromotionNotFoundException {
        return ResponseEntity.ok(new PromotionDTO(promotionService.getByName(name)));
    }

}
