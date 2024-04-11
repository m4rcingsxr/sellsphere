package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.PagingHelper;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.common.entity.Constants;
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
 * Service class for managing User-related operations.
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
     * Gets a user by its ID.
     *
     * @param id the user ID
     * @return the user
     * @throws UserNotFoundException if the user is not found
     */
    public User get(Integer id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Lists all roles.
     *
     * @return the list of roles
     */
    public List<Role> listAllRoles() {
        return roleRepository.findAll(PagingHelper.getSort("name", Constants.SORT_ASCENDING));
    }

    /**
     * Lists all users sorted by the specified field and direction.
     *
     * @param sortField the sort field
     * @param sortDirection the sort direction
     * @return the list of users
     */
    public List<User> listAll(String sortField, String sortDirection) {
        Sort sort = PagingHelper.getSort(sortField, sortDirection);
        return userRepository.findAll(sort);
    }

    /**
     * Lists users by page.
     *
     * @param pageNum the page number
     * @param helper the PagingAndSortingHelper
     */
    public void listPage(Integer pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, USERS_PER_PAGE, userRepository);
    }

    /**
     * Saves a user and its image file.
     *
     * @param user the user
     * @param file the image file
     * @return the saved user
     * @throws IOException if an I/O error occurs
     * @throws UserNotFoundException if the user is not found
     */
    public User save(User user, MultipartFile file) throws IOException, UserNotFoundException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            user.setMainImage(fileName);
            User savedUser = save(user);

            String folderName = S3_FOLDER_NAME + savedUser.getId();

            FileService.saveSingleFile(file, folderName, fileName);

            return savedUser;
        } else {
            return save(user);
        }
    }

    /**
     * Saves a user.
     *
     * @param user the user
     * @return the saved user
     * @throws UserNotFoundException if the user is not found
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
     * Encodes the user's password.
     *
     * @param user the user
     */
    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    /**
     * Checks if a user email is unique.
     *
     * @param userId the user ID (optional)
     * @param email the user email
     * @return true if the email is unique, false otherwise
     */
    public boolean isEmailUnique(Integer userId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(true);
    }

    /**
     * Deletes a user by its ID.
     *
     * @param id the user ID
     * @throws UserNotFoundException if the user is not found
     */
    public void delete(Integer id) throws UserNotFoundException {
        User user = get(id);
        userRepository.delete(user);
    }

    /**
     * Updates the enabled status of a user.
     *
     * @param id the user ID
     * @param enabled the new enabled status
     * @throws UserNotFoundException if the user is not found
     */
    public void updateUserEnabledStatus(Integer id, boolean enabled) throws UserNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
