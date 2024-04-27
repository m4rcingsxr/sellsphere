package com.sellsphere.client.setting;

import com.sellsphere.common.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {

}
