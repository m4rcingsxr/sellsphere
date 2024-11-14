package com.sellsphere.admin.mainpage;

import com.sellsphere.admin.article.PromotionService;
import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselType;
import com.sellsphere.common.entity.PromotionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/sql/roles.sql", "/sql/users.sql", "/sql/articles.sql", "/sql/carousels.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CarouselServiceIntegrationTest {

    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CarouselRepository carouselRepository;
    @Autowired
    private PromotionService promotionService;

    @Test
    void givenExistingCarousels_whenListAll_thenReturnAllCarouselsOrderedByCarouselOrder() {
        // When
        List<Carousel> carousels = carouselService.listAll();

        // Then
        assertNotNull(carousels);
        assertFalse(carousels.isEmpty(), "List of carousels should not be empty.");
        assertEquals(3, carousels.size(), "There should be 3 carousels.");
        assertEquals(0, carousels.get(0).getCarouselOrder(), "First carousel should have order 0.");
        assertEquals(1, carousels.get(1).getCarouselOrder(), "Second carousel should have order 1.");
        assertEquals(2, carousels.get(2).getCarouselOrder(), "Third carousel should have order 2.");
    }

    @Test
    void givenExistingCarouselId_whenGet_thenReturnCarousel() throws CarouselNotFoundException {
        // Given
        Integer carouselId = 1;

        // When
        Carousel carousel = carouselService.get(carouselId);

        // Then
        assertNotNull(carousel);
        assertEquals("main image carousel", carousel.getHeader(), "Carousel header should match.");
        assertEquals(CarouselType.IMAGE, carousel.getType(), "Carousel type should be IMAGE.");
    }

    @Test
    void givenNonExistingCarouselId_whenGet_thenThrowCarouselNotFoundException() {
        // Given
        Integer nonExistingId = 999;

        // When/Then
        assertThrows(CarouselNotFoundException.class, () -> carouselService.get(nonExistingId));
    }

    @Test
    void givenTwoCarousels_whenChangeCarouselOrder_thenOrdersAreSwapped() throws CarouselNotFoundException {
        // Given
        Integer fromId = 1;
        Integer toId = 2;
        Carousel fromCarousel = carouselService.get(fromId);
        Carousel toCarousel = carouselService.get(toId);
        int initialFromOrder = fromCarousel.getCarouselOrder();
        int initialToOrder = toCarousel.getCarouselOrder();

        // When
        carouselService.changeCarouselOrder(fromId, toId);

        // Then
        Carousel updatedFromCarousel = carouselService.get(fromId);
        Carousel updatedToCarousel = carouselService.get(toId);
        assertEquals(initialToOrder, updatedFromCarousel.getCarouselOrder(), "fromCarousel order should be swapped.");
        assertEquals(initialFromOrder, updatedToCarousel.getCarouselOrder(), "toCarousel order should be swapped.");
    }

    @Test
    void givenNewCarousel_whenSave_thenCarouselIsPersisted() throws PromotionNotFoundException {
        // Given
        Carousel carousel = new Carousel();
        carousel.setType(CarouselType.PROMOTION);
        carousel.setHeader("New Promotion Carousel");
        carousel.setCarouselOrder(3);

        // When
        carouselService.save(carousel, 1);

        // Then
        assertNotNull(carousel.getId(), "Carousel should have a non-null ID after saving.");
        assertEquals(3, carousel.getCarouselOrder(), "Carousel order should be 3.");
    }

    @Test
    void givenCarouselId_whenDelete_thenCarouselIsDeleted() throws CarouselNotFoundException {
        // Given
        Integer carouselId = 2;
        Carousel carousel = carouselService.get(carouselId);
        assertNotNull(carousel);

        // When
        carouselService.delete(carouselId);

        // Then
        Optional<Carousel> deletedCarousel = carouselRepository.findById(carouselId);
        assertFalse(deletedCarousel.isPresent(), "Carousel should be deleted.");
    }

    @Test
    void givenPromotionId_whenSavePromotionCarousel_thenPromotionIsAssigned() throws PromotionNotFoundException {
        // Given
        Carousel carousel = new Carousel();
        carousel.setType(CarouselType.PROMOTION);
        carousel.setHeader("Promotion Carousel");
        Integer promotionId = 1;

        // When
        carouselService.save(carousel, promotionId);

        // Then
        assertNotNull(carousel.getPromotion(), "Promotion should be assigned to the carousel.");
        assertEquals(promotionId, carousel.getPromotion().getId(), "Promotion ID should match.");
    }

    @Test
    void givenNoPromotionId_whenSavePromotionCarousel_thenThrowException() {
        // Given
        Carousel carousel = new Carousel();
        carousel.setType(CarouselType.PROMOTION);
        carousel.setHeader("Promotion Carousel");

        // When/Then
        assertThrows(IllegalStateException.class, () -> carouselService.save(carousel, null),
                     "Saving a promotion carousel without a promotion ID should throw an exception.");
    }

    @Test
    void givenExistingMainImageCarousel_whenReplacing_thenOldOneIsDeletedAndOrderRefreshed()
            throws PromotionNotFoundException {
        // Given
        Carousel newCarousel = new Carousel();
        newCarousel.setType(CarouselType.IMAGE);
        newCarousel.setHeader("New Main Image Carousel");

        // When
        carouselService.save(newCarousel, null);

        // Then
        List<Carousel> carousels = carouselService.listAll();
        assertEquals(3, carousels.size(), "There should still be 3 carousels after replacing the main one.");
        assertEquals(0, carousels.get(0).getCarouselOrder(), "The new main image carousel should have order 0.");
        assertEquals("New Main Image Carousel", carousels.get(0).getHeader(), "Header should match the new carousel.");
    }
}
