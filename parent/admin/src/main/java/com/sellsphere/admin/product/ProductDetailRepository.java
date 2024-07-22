package com.sellsphere.admin.product;

import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
}
