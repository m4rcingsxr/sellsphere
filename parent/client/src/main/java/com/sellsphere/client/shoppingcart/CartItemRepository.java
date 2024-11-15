package com.sellsphere.client.shoppingcart;

import com.sellsphere.common.entity.CartItem;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByCustomer(Customer customer);

    Optional<CartItem> findByCustomerAndProduct(Customer customer, Product product);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.customer.id = ?1 AND c.product.id = ?2")
    void deleteByCustomerAndProduct(Integer customerId, Integer productId);

    void deleteAllByCustomer(Customer customer);

}