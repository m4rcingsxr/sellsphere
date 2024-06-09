package com.sellsphere.client.shoppingcart;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

    Optional<ShoppingCart> findByCustomer(Customer customer);

    Optional<ShoppingCart> findByCustomerId(Integer customerId);

    void deleteByCustomer(Customer customer);
}
