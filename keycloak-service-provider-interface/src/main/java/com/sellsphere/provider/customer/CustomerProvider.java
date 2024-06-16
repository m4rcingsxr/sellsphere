package com.sellsphere.provider.customer;

import com.sellsphere.provider.customer.external.Customer;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.*;
import org.keycloak.models.*;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Custom provider for managing customer-related operations in Keycloak.
 */
@Stateless
@NoArgsConstructor
public class CustomerProvider
        implements UserStorageProvider, UserLookupProvider, UserRegistrationProvider,
        UserQueryProvider, CredentialInputUpdater, CredentialInputValidator, OnUserCache,
        CredentialProvider<CredentialModel> {

    public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";

    private PasswordEncoder encoder;
    private KeycloakSession session;
    private ComponentModel model;
    private EntityManager entityManager;
    private Event<UserAddedEvent> userAddedEvent;

    @Inject
    public CustomerProvider(KeycloakSession session, ComponentModel model,
                            JpaConnectionProvider jpaConnectionProvider,
                            Event<UserAddedEvent> userAddedEvent) {
        this.session = session;
        this.model = model;
        this.entityManager = jpaConnectionProvider.getEntityManager();
        this.encoder = new BCryptPasswordEncoder();
        this.userAddedEvent = userAddedEvent;
    }

    @Override
    public String getType() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user,
                                            CredentialModel credentialModel) {
        hashPassword(credentialModel);

        TypedQuery<Customer> customerTypedQuery = entityManager.createQuery(
                "UPDATE Customer c SET c.password = :newPassword WHERE c.id = :customerId",
                Customer.class
        );
        customerTypedQuery.setParameter("customerId", Integer.valueOf(user.getId()));
        customerTypedQuery.setParameter("newPassword", credentialModel.getSecretData());
        customerTypedQuery.executeUpdate();

        return credentialModel;
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        TypedQuery<Customer> customerTypedQuery = entityManager.createQuery(
                "UPDATE Customer c SET c.password = null WHERE c.id = :customerId", Customer.class);
        customerTypedQuery.setParameter("customerId", Integer.valueOf(user.getId()));
        customerTypedQuery.executeUpdate();

        return true;
    }

    @Override
    public CredentialModel getCredentialFromModel(CredentialModel model) {
        return model;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(
            CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.BASIC_AUTHENTICATION)
                .displayName("Simple Password")
                .removeable(true)
                .build(session);
    }

    private void hashPassword(CredentialModel model) {
        model.setSecretData(encoder.encode(model.getSecretData()));
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        String persistenceId = StorageId.externalId(id);
        return getUserByEmail(realm, persistenceId);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        return getUserByEmail(realm, username);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        TypedQuery<Customer> query = entityManager.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :email", Customer.class);
        query.setParameter("email", email);
        List<Customer> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        return new CustomerAdapter(session, realm, model, result.get(0));
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        // Do not persist here, just create the entity

        Customer entity = new Customer();
        entity.setEmail(username);
        entityManager.persist(entity);

        return new CustomerAdapter(session, realm, model, entity);
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        String persistenceId = StorageId.externalId(user.getId());
        Customer entity = entityManager.find(Customer.class, persistenceId);
        if (entity == null) {
            return false;
        }
        entityManager.remove(entity);
        return true;
    }

    @Override
    public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
        String password = ((CustomerAdapter) delegate).getPassword();
        if (password != null) {
            user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
        }
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel cred = (UserCredentialModel) input;
        CustomerAdapter adapter = getUserAdapter(user);
        adapter.setPassword(encoder.encode(cred.getValue()));
        return true;
    }

    private CustomerAdapter getUserAdapter(UserModel user) {
        if (user instanceof CachedUserModel) {
            return (CustomerAdapter) ((CachedUserModel) user).getDelegateForUpdate();
        } else {
            return (CustomerAdapter) user;
        }
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) {
            return;
        }
        getUserAdapter(user).setPassword(null);
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        if (getUserAdapter(user).getPassword() != null) {
            Set<String> set = new HashSet<>();
            set.add(PasswordCredentialModel.TYPE);
            return set.stream();
        } else {
            return Stream.empty();
        }
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && getPassword(user) != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel cred = (UserCredentialModel) input;
        String password = getPassword(user);
        if (password == null) {
            return false;
        }
        return encoder.matches(cred.getValue(), password);
    }

    private String getPassword(UserModel user) {
        String password = null;
        if (user instanceof CachedUserModel) {
            password = (String) ((CachedUserModel) user).getCachedWith().get(PASSWORD_CACHE_KEY);
        } else if (user instanceof CustomerAdapter) {
            password = ((CustomerAdapter) user).getPassword();
        }
        return password;
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        Object count = entityManager.createQuery(
                "SELECT COUNT(c) FROM Customer c").getSingleResult();
        return ((Number) count).intValue();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params,
                                                 Integer firstResult, Integer maxResults) {
        String search = params.get(UserModel.SEARCH);
        TypedQuery<Customer> query;

        if (search == null || search.isEmpty() || search.equals("*")) {
            query = entityManager.createQuery("SELECT c FROM Customer c", Customer.class);
        } else {
            query = entityManager.createQuery(
                    "SELECT c FROM Customer c WHERE lower(c.email) LIKE :search ORDER BY c.email",
                    Customer.class
            );
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }

        List<Customer> resultList = query.getResultList();

        return resultList.stream().map(
                entity -> new CustomerAdapter(session, realm, model, entity));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group,
                                                   Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName,
                                                                String attrValue) {
        return Stream.empty();
    }
}
