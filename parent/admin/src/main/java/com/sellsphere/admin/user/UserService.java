package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingHelper;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service class for managing user-related operations, including CRUD,
 * file management, and user role handling.
 */
@RequiredArgsConstructor
@Service
public class UserService {

    private static final String S3_FOLDER_NAME = "user-photos/";
    private static final int USERS_PER_PAGE = 10;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID.
     * @return the found user.
     * @throws UserNotFoundException if no user is found with the given ID.
     */
    public User get(Integer id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email of the user.
     * @return the found user.
     * @throws UserNotFoundException if no user is found with the given email.
     */
    public User get(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }


    /**
     * Lists all roles.
     *
     * @return a list of all roles sorted by name.
     */
    public List<Role> listAllRoles() {
        return roleRepository.findAll(PagingHelper.getSort("name", Sort.Direction.ASC));
    }


    /**
     * Lists all users sorted by the specified field and direction.
     *
     * @param sortField the field to sort by.
     * @param sortDirection the direction to sort (ASC/DESC).
     * @return a list of sorted users.
     */
    public List<User> listAll(String sortField, Sort.Direction sortDirection) {
        Sort sort = PagingHelper.getSort(sortField, sortDirection);
        return userRepository.findAll(sort);
    }

    /**
     * Lists users by page.
     *
     * @param pageNum the page number.
     * @param helper the helper for managing pagination and sorting.
     */
    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, USERS_PER_PAGE, userRepository);
    }


    /**
     * Saves a user and their profile image, if provided.
     *
     * @param user the user entity to be saved.
     * @param file the profile image to be uploaded.
     * @return the saved user entity.
     * @throws IOException if an error occurs during file upload.
     * @throws UserNotFoundException if the user is not found.
     */
    public User save(User user, MultipartFile file) throws IOException, UserNotFoundException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            user.setMainImage(fileName);

            User savedUser = save(user);

            String folderName = S3_FOLDER_NAME + savedUser.getId();
            FileService.saveSingleFile(file, folderName, fileName);

            return savedUser;
        }
        return save(user);
    }

    /**
     * Saves a user to the database.
     * If the user is being updated, their password is preserved unless changed.
     *
     * @param user the user entity to be saved.
     * @return the saved user entity.
     * @throws UserNotFoundException if the user is not found during update.
     */
    public User save(User user) throws UserNotFoundException {
        if (user.getId() != null && user.getPassword() == null) {
            User existingUser = userRepository
                    .findById(user.getId())
                    .orElseThrow(UserNotFoundException::new);
            user.setPassword(existingUser.getPassword());
        } else {
            encodePassword(user);
        }
        return userRepository.save(user);
    }


    /**
     * Encodes the user's password before saving.
     *
     * @param user the user whose password needs to be encoded.
     */
    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    /**
     * Checks if a user email is unique.
     *
     * @param userId the ID of the user (optional).
     * @param email the email to check.
     * @return true if the email is unique, false otherwise.
     */
    public boolean isEmailUnique(Integer userId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(true);
    }


    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to be deleted.
     * @throws UserNotFoundException if the user is not found.
     */
    public void delete(Integer id) throws UserNotFoundException {
        User user = get(id);
        userRepository.delete(user);
    }

    /**
     * Updates the enabled status of a user.
     *
     * @param id the user ID.
     * @param enabled the new enabled status.
     * @throws UserNotFoundException if the user is not found.
     */
    public void updateUserEnabledStatus(Integer id, boolean enabled) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
