package com.sellsphere.client.review;

import com.sellsphere.client.order.OrderRepository;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
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

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public Review getReview(Integer id) throws ReviewNotFoundException {
        return reviewRepository.findById(id).orElseThrow(ReviewNotFoundException::new);
    }

    public Review save(Review review) {
        review.setReviewTime(LocalDate.now());

        return reviewRepository.save(review);
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
}
