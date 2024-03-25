package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.PagingHelper;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private static final String S3_FOLDER_NAME = "user-photos/";
    private static final int USERS_PER_PAGE = 10;

    public User get(Integer id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public List<Role> listAllRoles() {
        return roleRepository.findAll(PagingHelper.getSort("name", Constants.SORT_ASCENDING));
    }

    public Page<User> listPage(Integer pageNum, String sortField, String sortDirection,
                               String keyword) {
        PageRequest pageRequest = PagingHelper.getPageRequest(pageNum, USERS_PER_PAGE, sortField,
                                                              sortDirection
        );


        if (keyword != null) {
            return userRepository.findAll(keyword, pageRequest);
        } else {
            return userRepository.findAll(pageRequest);

        }

    }

    public User save(User user, MultipartFile file) throws IOException, UserNotFoundException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            user.setMainImage(fileName);
            User savedUser = save(user);

            String folderName = S3_FOLDER_NAME + savedUser.getId();

            fileService.saveSingleFile(file, folderName, fileName);

            return savedUser;
        } else {
            return save(user);
        }

    }

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

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }


    public boolean isEmailUnique(Integer userId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(true);
    }

    public void delete(Integer id) throws UserNotFoundException {
        User user = get(id);
        userRepository.delete(user);
    }

    public void updateUserEnabledStatus(Integer id, boolean enabled)
            throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(
                UserNotFoundException::new);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
