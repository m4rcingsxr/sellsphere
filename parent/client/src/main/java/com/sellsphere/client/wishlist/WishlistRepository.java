package com.sellsphere.client.wishlist;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    Optional<Wishlist> findByCustomer(Customer customer);

}
