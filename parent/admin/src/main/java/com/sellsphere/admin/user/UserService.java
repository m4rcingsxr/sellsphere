package com.sellsphere.admin.user;

import com.sellsphere.admin.FileService;
import com.sellsphere.admin.PagingHelper;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    public static final int USERS_PER_PAGE = 10;

    public Page<User> listPage(Integer pageNum, String sortField, String sortDirection) {
        PageRequest pageRequest = PagingHelper.getPageRequest(pageNum, USERS_PER_PAGE, sortField,
                                                              sortDirection
        );

        return userRepository.findAll(pageRequest);
    }

    public User save(User user, MultipartFile file) throws IOException, UserNotFoundException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            user.setMainImage(fileName);
            User savedUser = save(user);

            String folderName = "user-photos/" + savedUser.getId();

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


}
