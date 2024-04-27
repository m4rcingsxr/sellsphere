package com.sellsphere.client.address;

import com.sellsphere.common.entity.Address;
import com.sellsphere.common.entity.Country;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AddressTestUtil {

    public static Address generateDummyAddress1() {
        Address address = new Address();
        address.setFirstName("John");
        address.setLastName("Doe");
        address.setPhoneNumber("1234567890");
        address.setAddressLine1("123 Main St");
        address.setAddressLine2("Apt 4B");
        address.setCity("Springfield");
        address.setState("IL");
        address.setCountry(generateDummyCountry());
        address.setPostalCode("62704");
        address.setPrimary(true);
        return address;
    }

    public static Address generateDummyAddress2() {
        Address address = new Address();
        address.setFirstName("Jane");
        address.setLastName("Smith");
        address.setPhoneNumber("0987654321");
        address.setAddressLine1("456 Elm St");
        address.setAddressLine2(null);
        address.setCity("Metropolis");
        address.setState("NY");
        address.setCountry(generateDummyCountry());
        address.setPostalCode("10001");
        address.setPrimary(false);
        return address;
    }

    public static Address generateDummyAddress3() {
        Address address = new Address();
        address.setFirstName("Alice");
        address.setLastName("Johnson");
        address.setPhoneNumber("1122334455");
        address.setAddressLine1("789 Oak St");
        address.setAddressLine2("Suite 12");
        address.setCity("Gotham");
        address.setState("NJ");
        address.setCountry(generateDummyCountry());
        address.setPostalCode("07001");
        address.setPrimary(true);
        return address;
    }

    public static Country generateDummyCountry() {
        Country country = new Country();
        country.setId(1);
        country.setName("United States");
        return country;
    }

}
