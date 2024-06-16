package com.sellsphere.provider.customer;

import com.sellsphere.provider.customer.external.Customer;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.keycloak.models.UserModel;

public class UserAddedEventListener {

    @PersistenceContext(unitName = "user-store")
    private EntityManager entityManager;

    // Workaround for not available in CustomerProvider#addUser method
    // Method return UserModel that contains first name and last name
    @Transactional
    public void onUserAdded(@Observes(during = TransactionPhase.BEFORE_COMPLETION) UserAddedEvent event) {
        Customer customer = new Customer();
        UserModel userModel = event.getUser();

        customer.setEmail(userModel.getEmail());
        customer.setFirstName(userModel.getFirstName());
        customer.setLastName(userModel.getLastName());
        customer.setPassword(((CustomerAdapter) userModel).getPassword());
        customer.setEnabled(true);  // Or other default values

        entityManager.persist(customer);
        entityManager.flush();
    }
}