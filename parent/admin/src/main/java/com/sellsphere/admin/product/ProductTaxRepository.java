package com.sellsphere.admin.product;

import com.sellsphere.common.entity.ProductTax;
import com.sellsphere.common.entity.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTaxRepository extends JpaRepository<ProductTax, String> {

    List<ProductTax> findByType(TaxType taxType);

}
