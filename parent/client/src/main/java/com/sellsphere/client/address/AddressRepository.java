package com.sellsphere.client.address;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByCustomer(Customer customer);

    Address findByPrimaryIsTrueAndCustomer(Customer customer);

}
