package com.sellsphere.provider.listener;

import com.sellsphere.payment.CustomerService;
import com.sellsphere.provider.customer.CustomerAdapter;
import com.sellsphere.provider.UserModelTransaction;
import com.sellsphere.provider.UserModelTransactionManager;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.UserModel;

@Slf4j
public class CustomerEventListenerProvider implements EventListenerProvider {

    private final CustomerService customerService;

    private final EntityManager entityManager;

    UserModelTransaction tx;

    public CustomerEventListenerProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.customerService = new CustomerService();
        this.tx = UserModelTransactionManager.getInstance(this::updateUser);
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.LOGIN) {
            String email = event.getDetails().get("username");
            if(email == null || email.isEmpty()) {
                throw new IllegalStateException("Email cannot be null or empty");
            }

            UserModel user = tx.findUser(email);

            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setName(user.getFirstName() + " " + user.getLastName());
            customer.setId(customer.getId());

            try {
                customerService.createCustomer(customer);
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // Handle admin events
    }

    @Override
    public void close() {
    }

    private void updateUser(UserModel user) {
        CustomerAdapter userAdapter = (CustomerAdapter) user;
        if (userAdapter.isDirty()) {
            com.sellsphere.provider.customer.external.Customer entity = new com.sellsphere.provider.customer.external.Customer();
            entity.setEmail(user.getUsername());

            entityManager.persist(entity);
        }
    }
}