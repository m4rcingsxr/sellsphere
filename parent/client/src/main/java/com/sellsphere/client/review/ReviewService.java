package com.sellsphere.client.review;

import com.sellsphere.client.order.OrderRepository;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int REVIEWS_PER_PAGE = 10;

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public Review getReview(Integer id) throws ReviewNotFoundException {
        return reviewRepository.findById(id).orElseThrow(ReviewNotFoundException::new);
    }

    public Review save(Review review) {
        review.setReviewTime(LocalDate.now());

        return reviewRepository.save(review);
    }

    public Review getReview(Customer customer, Product product)  {
        List<Review> reviews = reviewRepository.findAllByCustomerAndProduct(customer, product);
        if(reviews.isEmpty()) {
            return null;
        } else {
            return reviews.get(0);
        }
    }

    public Map<Product, Review> mapProductsByReviewStatus(Customer customer, List<Product> boughtProducts) {
        List<Review> reviewsByCustomer = reviewRepository.findAllByCustomer(customer);

        Map<Product, Review> productReviewMap = reviewsByCustomer.stream()
                .collect(Collectors.toMap(Review::getProduct, review -> review, (existing, replacement) -> existing));

        Map<Product, Review> result = new HashMap<>();
        for (Product product : boughtProducts) {
            result.put(product, productReviewMap.getOrDefault(product, null));
        }

        return result;
    }

    public List<Review> getFirst5ApprovedReviews(Product product) {
        Sort sort = Sort.by("reviewTime").descending();
        Pageable pageable = PageRequest.of(0, 5, sort);

        return reviewRepository.findAllByProductAndApprovedIsTrue(product, pageable).getContent();
    }

    public boolean hasCustomerReviewPermission(Customer customer, Product product) {
        if(customer == null) {
            return false;
        }

        Review review = getReview(customer, product);
        List<Order> orders = orderRepository.findAllByTransactionCustomer(customer);
        Stream<Order> orderStream = orders.stream().filter(order -> order.getOrderDetails().stream().anyMatch(detail -> detail.getProduct().equals(product)));

        return review == null && orderStream.findAny().isPresent();
    }

    public Map<Integer, Float> calculateRatingPercentages(Product product) {
        List<Review> reviews = reviewRepository.findAllByProductAndApprovedIsTrue(product);

        return reviews
                .stream()
                .collect(Collectors.groupingBy(Review::getRate, Collectors.counting()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (float) e.getValue() * 100 / reviews.size()));
    }

    public boolean hasCustomerPostedReview(Customer customer, Product product) {
        return customer != null && getReview(customer, product) != null;
    }

    public Page<Review> pageReviews(Product product, Integer pageNum, String sortField) {
        Sort sort;

        if(sortField != null) {
            switch (sortField) {
                case "leastPopular" -> sort = Sort.by("votes").ascending();
                case "mostPopular" -> sort = Sort.by("votes").descending();
                default -> sort = Sort.by("reviewTime");
            }
        } else {
            sort = Sort.by("reviewTime");
        }

        Pageable pageable = PageRequest.of(pageNum, REVIEWS_PER_PAGE, sort);
        return reviewRepository.findAllByProductAndApprovedIsTrue(product, pageable);
    }
}
