package com.sellsphere.provider.customer;

import com.sellsphere.provider.customer.external.Customer;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageUtil;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Adapter class for adapting the {@link Customer} entity to Keycloak's user model.
 * This class extends {@link AbstractUserAdapterFederatedStorage} to provide
 * a bridge between Keycloak and the custom {@link Customer} entity.
 */
public class CustomerAdapter extends AbstractUserAdapter {

    private final Customer customer;

    public CustomerAdapter(KeycloakSession session, RealmModel realm, ComponentModel model,
                           Customer customer) {
        super(session, realm, model);
        this.storageId = new StorageId(storageProviderModel.getId(), customer.getEmail());
        this.customer = customer;
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public String getFirstName() {
        return customer.getFirstName();
    }

    @Override
    public String getLastName() {
        return customer.getLastName();
    }

    @Override
    public String getEmail() {
        return customer.getEmail();
    }

    @Override
    public boolean isEmailVerified() {
        return customer.isEmailVerified();
    }

    public String getPassword() {
        return customer.getPassword();
    }

    public void setPassword(String password) {
        customer.setPassword(password);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }

    @Override
    public String getFirstAttribute(String name) {
        List<String> list = getAttributes().getOrDefault(name, List.of());
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        return attributes;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        Map<String, List<String>> attributes = getAttributes();
        return (attributes.containsKey(name)) ? attributes.get(name).stream() : Stream.empty();
    }

    @Override
    protected Set<GroupModel> getGroupsInternal() {
        return Set.of();
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
        return Set.of();
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return getFederatedStorage().getRequiredActionsStream(realm, this.getId());
    }

    @Override
    public void addRequiredAction(String action) {
        getFederatedStorage().addRequiredAction(realm, this.getId(), action);
    }

    @Override
    public void removeRequiredAction(String action) {
        getFederatedStorage().removeRequiredAction(realm, this.getId(), action);
    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        getFederatedStorage().addRequiredAction(realm, this.getId(), action.name());
    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        getFederatedStorage().removeRequiredAction(realm, this.getId(), action.name());
    }

    UserFederatedStorageProvider getFederatedStorage() {
        return UserStorageUtil.userFederatedStorage(session);
    }

}