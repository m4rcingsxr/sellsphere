package com.sellsphere.client.mainpage;

import com.sellsphere.client.article.ArticleRepository;
import com.sellsphere.client.product.ProductRepository;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CarouselService {

    private final CarouselRepository carouselRepository;
    private final CarouselImageRepository carouselImageRepository;
    private final ProductRepository productRepository;
    private final ArticleRepository articleRepository;

    public Carousel getMainCarousel() {
        List<Carousel> imageCarousel = findAllByType(CarouselType.IMAGE);
        if (!imageCarousel.isEmpty()) {
            return imageCarousel.get(0);
        }

        return null;
    }

    public List<Carousel> findAllByType(CarouselType type) {
        return carouselRepository.findAllByTypeOrderByCarouselOrder(type);
    }

    public List<CarouselImage> getCarouselImages(Carousel carousel) {
        if (carousel == null) {
            return Collections.emptyList();
        }

        List<CarouselItem> carouselItems = carousel.getCarouselItems();
        List<Integer> carouselImageIds = carouselItems.stream()
                .map(CarouselItem::getEntityId)
                .toList();

        List<CarouselImage> carouselImages =
                carouselImageRepository.findAllById(
                        carouselImageIds);

        // Ensure the order of the returned images matches the order of the IDs
        Map<Integer, CarouselImage> imageMap = carouselImages.stream()
                .collect(Collectors.toMap(CarouselImage::getId,
                                          Function.identity()
                ));

        return carouselImageIds.stream()
                .map(imageMap::get)
                .toList();
    }

    public Map<Carousel, List<Product>> getPromotionCarousels() {
        // Step 1: Retrieve all carousels of type PRODUCT
        List<Carousel> carousels = carouselRepository.findAllByTypeOrderByCarouselOrder(CarouselType.PROMOTION);

        // Step 2: Create a map of carousel IDs to their respective carousel items
        Map<Integer, List<CarouselItem>> carouselMap = carousels
                .stream()
                .collect(Collectors.toMap(Carousel::getId, Carousel::getCarouselItems));

        // Step 3: Create a map of Carousel to List of Products
        Map<Carousel, List<Product>> carouselProductMap = new HashMap<>();

        // Step 4: Iterate through the carousel items, fetch products, and ensure they are in the correct order
        for (Carousel carousel : carousels) {
            List<CarouselItem> carouselItems = carouselMap.get(carousel.getId());
            if (carouselItems != null && !carouselItems.isEmpty()) {
                List<Integer> carouselItemsIDs = carouselItems.stream()
                        .map(CarouselItem::getEntityId)
                        .toList();

                // Fetch products by IDs
                List<Product> products = productRepository.findAllById(carouselItemsIDs);

                // Order products based on carousel item IDs
                Map<Integer, Product> productMap = products.stream()
                        .collect(Collectors.toMap(Product::getId, product -> product));

                List<Product> orderedProducts = carouselItemsIDs.stream()
                        .map(productMap::get)
                        .toList();

                // Map carousel to the ordered list of products
                carouselProductMap.put(carousel, orderedProducts);
            } else {
                // If no items, map to an empty list
                carouselProductMap.put(carousel, Collections.emptyList());
            }
        }

        return carouselProductMap;
    }

    public Map<Carousel, List<Object>> getArticleCarousels() {
        // Step 1: Retrieve all carousels of type ARTICLE
        List<Carousel> carousels = carouselRepository.findAllByTypeOrderByCarouselOrder(CarouselType.ARTICLE);

        // Step 2: Create a map of carousel IDs to their respective carousel items
        Map<Integer, List<CarouselItem>> carouselMap = carousels
                .stream()
                .collect(Collectors.toMap(Carousel::getId, Carousel::getCarouselItems));

        // Step 3: Create a map of Carousel to List of Articles
        Map<Carousel, List<Object>> carouselArticleMap = new HashMap<>();

        // Step 4: Iterate through the carousel items, fetch articles, and ensure they are in the correct order
        for (Carousel carousel : carousels) {
            List<CarouselItem> carouselItems = carouselMap.get(carousel.getId());
            if (carouselItems != null && !carouselItems.isEmpty()) {
                List<Integer> carouselItemsIDs = carouselItems.stream()
                        .map(CarouselItem::getEntityId)
                        .toList();

                // Fetch articles by IDs
                List<Article> articles = articleRepository.findAllById(carouselItemsIDs);

                // Order articles based on carousel item IDs
                Map<Integer, Object> articleMap = articles.stream()
                        .collect(Collectors.toMap(Article::getId, article -> article));

                List<Object> orderedArticles = carouselItemsIDs.stream()
                        .map(articleMap::get)
                        .toList();

                // Map carousel to the ordered list of articles
                carouselArticleMap.put(carousel, orderedArticles);
            } else {
                // If no items, map to an empty list
                carouselArticleMap.put(carousel, Collections.emptyList());
            }
        }

        return carouselArticleMap;
    }

    public Map<Carousel, List<Object>> getAllCarousels() {
        // Step 1: Retrieve all carousels (both PRODUCTS and ARTICLES) in the correct order
        List<Carousel> carousels = carouselRepository.findAllByTypeInOrderByCarouselOrder(List.of(CarouselType.ARTICLE, CarouselType.PROMOTION));

        // Step 2: Create a map of carousel IDs to their respective carousel items
        Map<Integer, List<CarouselItem>> carouselMap = carousels.stream()
                .collect(Collectors.toMap(Carousel::getId, Carousel::getCarouselItems));

        // Step 3: Use a LinkedHashMap to maintain the insertion order of carousels
        Map<Carousel, List<Object>> carouselItemMap = new LinkedHashMap<>();

        // Step 4: Iterate through the carousel items, fetch products or articles, and ensure they are in the correct order
        for (Carousel carousel : carousels) {
            List<CarouselItem> carouselItems = carouselMap.get(carousel.getId());
            if (carouselItems != null && !carouselItems.isEmpty()) {
                List<Integer> carouselItemsIDs = carouselItems.stream()
                        .map(CarouselItem::getEntityId)
                        .toList();

                List<Object> orderedItems;

                if (carousel.getType() == CarouselType.PROMOTION) {
                    // Fetch products by IDs for PRODUCT carousels
                    List<Product> products = productRepository.findAllById(carouselItemsIDs);

                    // Map product IDs to the corresponding product
                    Map<Integer, Product> productMap = products.stream()
                            .collect(Collectors.toMap(Product::getId, product -> product));

                    // Order products according to the carousel item order
                    orderedItems = carouselItemsIDs.stream()
                            .map(productMap::get)
                            .collect(Collectors.toList());
                } else if (carousel.getType() == CarouselType.ARTICLE) {
                    // Fetch articles by IDs for ARTICLE carousels
                    List<Article> articles = articleRepository.findAllById(carouselItemsIDs);

                    // Map article IDs to the corresponding article
                    Map<Integer, Article> articleMap = articles.stream()
                            .collect(Collectors.toMap(Article::getId, article -> article));

                    // Order articles according to the carousel item order
                    orderedItems = carouselItemsIDs.stream()
                            .map(articleMap::get)
                            .collect(Collectors.toList());
                } else {
                    orderedItems = Collections.emptyList();
                }

                // Map the carousel to its ordered list of items (either products or articles)
                carouselItemMap.put(carousel, orderedItems);
            } else {
                // If no items, map to an empty list
                carouselItemMap.put(carousel, Collections.emptyList());
            }
        }

        return carouselItemMap;
    }

}
