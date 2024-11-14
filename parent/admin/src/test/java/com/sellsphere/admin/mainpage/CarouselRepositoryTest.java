package com.sellsphere.admin.mainpage;

import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselItem;
import com.sellsphere.common.entity.CarouselType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/sql/roles.sql", "/sql/users.sql", "/sql/articles.sql",
                "/sql/carousels.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CarouselRepositoryTest {

    @Autowired
    private CarouselRepository carouselRepository;
    @Autowired
    private CarouselItemRepository carouselItemRepository;

    @Test
    void givenCarouselWithItems_whenSaving_thenItemsArePersisted() {
        // Given
        Carousel carousel = new Carousel();
        carousel.setType(CarouselType.IMAGE);
        carousel.setHeader("New Image Carousel");
        carousel.setCarouselOrder(3);

        CarouselItem item1 = new CarouselItem();
        item1.setEntityId(5);
        item1.setOrder(0);
        carousel.addCarouselItem(item1);

        CarouselItem item2 = new CarouselItem();
        item2.setEntityId(6);
        item2.setOrder(1);
        carousel.addCarouselItem(item2);

        // When
        Carousel savedCarousel = carouselRepository.save(carousel);

        // Then
        assertNotNull(savedCarousel.getId(), "Saved carousel should have a non-null ID.");
        assertEquals(2, savedCarousel.getCarouselItems().size(), "Saved carousel should have 2 associated items.");
        assertEquals(5, savedCarousel.getCarouselItems().get(0).getEntityId(), "First item should have entity ID 5.");
        assertEquals(6, savedCarousel.getCarouselItems().get(1).getEntityId(), "Second item should have entity ID 6.");
    }

    @Test
    void givenExistingCarousel_whenDeleting_thenItemsAreDeleted() {
        // Given
        Integer carouselId = 2;
        Optional<Carousel> carouselOptional = carouselRepository.findById(carouselId);
        assertTrue(carouselOptional.isPresent(), "Carousel with ID " + carouselId + " should exist.");
        Carousel carousel = carouselOptional.get();
        List<CarouselItem> items = carousel.getCarouselItems();
        assertFalse(items.isEmpty(), "Carousel should have associated items.");

        // When
        carouselRepository.delete(carousel);

        // Then
        Optional<Carousel> deletedCarousel = carouselRepository.findById(carouselId);
        assertFalse(deletedCarousel.isPresent(), "Carousel should be deleted.");

        items.forEach(item -> {
            assertTrue(carouselItemRepository.findById(item.getId()).isEmpty(),
                       "Associated CarouselItem with ID " + item.getId() + " should also be deleted."
            );
        });
    }

    @Test
    void givenCarouselType_whenFindingFirstByType_thenReturnMatchingCarousel() {
        // When
        Optional<Carousel> carouselOptional = carouselRepository.findFirstByType(CarouselType.ARTICLE);

        // Then
        assertTrue(carouselOptional.isPresent(), "A carousel with type ARTICLE should exist.");
        assertEquals("main image carousel", carouselOptional.get().getHeader(),
                     "The header should match 'main image carousel'."
        );
    }

    @Test
    void givenNewCarousel_whenAddingItem_thenItemIsAddedCorrectly() {
        // Given
        Optional<Carousel> carouselOptional = carouselRepository.findById(2);
        assertTrue(carouselOptional.isPresent(), "Carousel with ID 2 should exist.");
        Carousel carousel = carouselOptional.get();

        CarouselItem newItem = new CarouselItem();
        newItem.setEntityId(7);
        newItem.setOrder(4);
        carousel.addCarouselItem(newItem);

        // When
        Carousel savedCarousel = carouselRepository.save(carousel);

        // Then
        assertEquals(5, savedCarousel.getCarouselItems().size(), "Carousel should now have 5 items.");
        assertEquals(7, savedCarousel.getCarouselItems().get(4).getEntityId(), "The new item should have entity ID 7.");
    }

    @Test
    void givenExistingCarousel_whenRemovingItem_thenItemIsRemovedCorrectly() {
        // Given
        Optional<Carousel> carouselOptional = carouselRepository.findById(3);
        assertTrue(carouselOptional.isPresent(), "Carousel with ID 3 should exist.");
        Carousel carousel = carouselOptional.get();
        assertFalse(carousel.getCarouselItems().isEmpty(), "Carousel should have associated items.");
        CarouselItem itemToRemove = carousel.getCarouselItems().get(0);

        // When
        carousel.removeCarouselItem(itemToRemove);
        Carousel savedCarousel = carouselRepository.save(carousel);

        // Then
        assertEquals(3, savedCarousel.getCarouselItems().size(), "Carousel should now have 3 items.");
        assertFalse(savedCarousel.getCarouselItems().contains(itemToRemove), "The removed item should not be present.");
    }

}
