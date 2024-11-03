package com.sellsphere.admin.mainpage;

import com.sellsphere.admin.article.PromotionService;
import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselType;
import com.sellsphere.common.entity.Promotion;
import com.sellsphere.common.entity.PromotionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class CarouselService {

    private final CarouselRepository carouselRepository;
    private final PromotionService promotionService;
    private static final int TEMP_ORDER = -1;

    /**
     * Retrieves all carousels sorted by their order.
     *
     * @return a list of all carousels, ordered by 'carouselOrder'
     */
    public List<Carousel> listAll() {
        return carouselRepository.findAll(Sort.by("carouselOrder").ascending());
    }

    /**
     * Retrieves a carousel by its ID.
     *
     * @param id the ID of the carousel
     * @return the found carousel
     * @throws CarouselNotFoundException if no carousel is found with the given ID
     */
    public Carousel get(Integer id) throws CarouselNotFoundException {
        return carouselRepository.findById(id)
                .orElseThrow(CarouselNotFoundException::new);
    }

    /**
     * Changes the order of two carousels, swapping their positions.
     *
     * @param fromId the ID of the first carousel
     * @param toId   the ID of the second carousel
     * @throws CarouselNotFoundException if either carousel is not found
     */
    public void changeCarouselOrder(Integer fromId, Integer toId) throws CarouselNotFoundException {
        Carousel fromCarousel = carouselRepository.findById(fromId).orElseThrow(CarouselNotFoundException::new);
        Carousel toCarousel = carouselRepository.findById(toId).orElseThrow(CarouselNotFoundException::new);

        int fromOrder = fromCarousel.getCarouselOrder();
        int toOrder = toCarousel.getCarouselOrder();

        // Swap orders with a temporary order
        fromCarousel.setCarouselOrder(TEMP_ORDER);
        carouselRepository.saveAndFlush(fromCarousel);

        toCarousel.setCarouselOrder(fromOrder);
        carouselRepository.saveAndFlush(toCarousel);

        fromCarousel.setCarouselOrder(toOrder);
        carouselRepository.saveAndFlush(fromCarousel);
    }

    /**
     * Saves a new carousel with a specific promotion, adjusting the order if required.
     *
     * @param carousel    the carousel to save
     * @param promotionId the ID of the promotion (if any)
     * @throws PromotionNotFoundException if the promotion ID is invalid
     */
    public void save(Carousel carousel, Integer promotionId) throws PromotionNotFoundException {
        int orderCount = (int) carouselRepository.count();
        carousel.setCarouselOrder(orderCount);
        carousel.setBidirectionalRelationship();

        if (carousel.getType() == CarouselType.PROMOTION) {
            setPromotion(carousel, promotionId);
        } else if (carousel.getType() == CarouselType.IMAGE) {
            replaceMainCarousel();
            carousel.setCarouselOrder(0);
        }

        carouselRepository.save(carousel);
    }

    private boolean isMainCarouselAbsent() {
        return carouselRepository.findFirstByType(CarouselType.IMAGE).isEmpty();
    }

    /**
     * Assigns a promotion to a carousel if it's of type PROMOTION.
     */
    private void setPromotion(Carousel carousel, Integer promotionId) throws PromotionNotFoundException {
        if (promotionId == null) {
            throw new IllegalStateException("Promotion carousel requires a valid promotion ID");
        }
        Promotion promotion = promotionService.getById(promotionId);
        carousel.setPromotion(promotion);
    }

    /**
     * Replaces the main image carousel if one already exists.
     */
    private void replaceMainCarousel() {
        carouselRepository.findFirstByType(CarouselType.IMAGE).ifPresent(carouselRepository::delete);
        carouselRepository.flush();
    }
}
