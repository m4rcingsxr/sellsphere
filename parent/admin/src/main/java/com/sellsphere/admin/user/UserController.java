package com.sellsphere.admin.user;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

//TODO: 2) rest api call to check email uniqueness, 1) search functionality
@RequiredArgsConstructor
@Controller
public class UserController {

    public static final String USER_FORM = "user/user_form";
    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/users/page/0?sortField=firstName&sortDir=asc";

    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/users")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/users/page/{pageNum}")
    public String listPage(
            @RequestParam("sortField") String sortField,
            @RequestParam("sortDir") String sortDir,
            @PathVariable("pageNum") Integer pageNum,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        Page<User> userPage = userService.listPage(pageNum, sortField, sortDir, keyword);

        model.addAttribute("userList", userPage.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("reversedSortDir", sortDir.equals(
                Constants.SORT_ASCENDING) ? Constants.SORT_DESCENDING : Constants.SORT_ASCENDING);

        return "user/users";
    }

    @GetMapping({"/users/new", "/users/edit/{id}"})
    public String showUserForm(@PathVariable(required = false, name = "id") Integer id,
                               Model model) throws UserNotFoundException {
        User user;
        String pageTitle;

        if (id != null) {

            // Edit form
            user = userService.get(id);
            pageTitle = "Edit User [ID: " + id + "]";
        } else {

            // New form
            user = new User();
            pageTitle = "Create New User";
        }

        List<Role> roleList = userService.listAllRoles();

        model.addAttribute("roleList", roleList);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", pageTitle);

        return USER_FORM;
    }


    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           RedirectAttributes ra,
                           @RequestParam(value = "newImage", required = false) MultipartFile file)
            throws IOException, UserNotFoundException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.user");
        validationHelper.validateMultipartFile(file, "mainImage", "An image file is required.");
        validationHelper.validatePassword(user.getPassword(), user.getId());

        if (!validationHelper.validate()) {
            return USER_FORM;
        }

        String successMessage = "The user has been " + (user.getId() != null
                ? "updated" : "saved") + " successfully.";

        userService.save(user, file);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return getUserPostModificationURL(user);
    }

    private String getUserPostModificationURL(User user) {

        // Constructs a URL for redirection after a user is modified
        String initialEmailPart = user.getEmail().split("@")[0];
        return DEFAULT_REDIRECT_URL + "&keyword=" + initialEmailPart;
    }

}

