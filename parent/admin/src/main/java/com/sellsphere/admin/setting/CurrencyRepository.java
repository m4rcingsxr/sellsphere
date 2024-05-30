package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Country;
import com.sellsphere.common.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Integer> {

    @Query("SELECT c FROM Currency c WHERE c.id IN (SELECT co.currency.id FROM Country co WHERE co.id IN :countries)")
    List<Currency> findAllByCountryIds(@Param("countries") List<Integer> countries);

}
