package com.sellsphere.client.wishlist;

import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.Wishlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(executionPhase =
        Sql.ExecutionPhase.BEFORE_TEST_CLASS,
        scripts = {"classpath:sql/categories.sql",
                   "classpath:sql/brands.sql",
                   "classpath:sql/brands_categories.sql",
                   "/sql/customers.sql", "/sql/products.sql",
                   "/sql/wishlist.sql"})
class WishlistRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Test
    void givenCustomerAndProducts_whenSaveWishlist_thenWishlistPersistedWithProducts() {
        Customer customer = entityManager.find(Customer.class, 2);
        Product product1 = entityManager.find(Product.class, 1);
        Product product2 = entityManager.find(Product.class, 2);

        Wishlist wishlist = new Wishlist();
        wishlist.setCustomer(customer);
        wishlist.addProduct(product1);
        wishlist.addProduct(product2);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        assertNotNull(savedWishlist.getId());
        assertNotNull(savedWishlist.getCustomer());
        assertEquals(2, savedWishlist.getProducts().size());
    }

    @Test
    void givenExistingWishlist_whenRemoveWishlist_thenWishlistRemoved() {
        wishlistRepository.deleteById(1);
        Wishlist wishlist = entityManager.find(Wishlist.class, 1);
        assertNull(wishlist);
    }

}