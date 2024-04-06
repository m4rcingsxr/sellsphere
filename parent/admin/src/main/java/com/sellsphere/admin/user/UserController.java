package com.sellsphere.admin.user;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.export.ExportUtil;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Role;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller class for managing User-related operations.
 */
@RequiredArgsConstructor
@Controller
public class UserController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/users/page/0?sortField=firstName&sortDir=asc";
    private static final String USER_FORM = "user/user_form";

    private final UserService userService;

    /**
     * Redirects to the first page of the user list.
     *
     * @return the redirect URL for the first page
     */
    @GetMapping("/users")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Lists users by page.
     *
     * @param helper the PagingAndSortingHelper
     * @param pageNum the page number
     * @return the view name for the user list
     */
    @GetMapping("/users/page/{pageNum}")
    public String listPage(@PagingAndSortingParam(listName = "userList", moduleURL = "/users") PagingAndSortingHelper helper,
                           @PathVariable("pageNum") Integer pageNum) {
        userService.listPage(pageNum, helper);
        return "user/users";
    }

    /**
     * Shows the form for creating or editing a user.
     *
     * @param id the user ID (optional)
     * @param model the model
     * @return the view name for the user form
     * @throws UserNotFoundException if the user is not found
     */
    @GetMapping({"/users/new", "/users/edit/{id}"})
    public String showUserForm(@PathVariable(required = false, name = "id") Integer id, Model model)
            throws UserNotFoundException {
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

    /**
     * Saves a user.
     *
     * @param user the user
     * @param bindingResult the binding result
     * @param ra the redirect attributes
     * @param file the user image file
     * @return the redirect URL after saving
     * @throws IOException if an I/O error occurs
     * @throws UserNotFoundException if the user is not found
     */
    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
                           RedirectAttributes ra,
                           @RequestParam(value = "newImage", required = false) MultipartFile file)
            throws IOException, UserNotFoundException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.user");
        validationHelper.validateMultipartFile(file, user.getId(), "mainImage", "An image file is required.");
        validationHelper.validatePassword(user.getPassword(), user.getId());

        if (!validationHelper.validate()) {
            return USER_FORM;
        }

        String successMessage = "The user has been " + (user.getId() != null ? "updated" : "saved") + " successfully.";

        userService.save(user, file);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return getUserPostModificationURL(user);
    }

    /**
     * Constructs a URL for redirection after a user is modified.
     *
     * @param user the user
     * @return the redirect URL
     */
    private String getUserPostModificationURL(User user) {
        String initialEmailPart = user.getEmail().split("@")[0];
        return DEFAULT_REDIRECT_URL + "&keyword=" + initialEmailPart;
    }

    /**
     * Deletes a user.
     *
     * @param id the user ID
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL after deletion
     * @throws UserNotFoundException if the user is not found
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) throws UserNotFoundException {
        userService.delete(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, "The user [ID: " + id + "] has been deleted successfully");
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Updates the enabled status of a user.
     *
     * @param id the user ID
     * @param status the new enabled status
     * @param ra the redirect attributes
     * @return the redirect URL after updating the status
     * @throws UserNotFoundException if the user is not found
     */
    @GetMapping("/users/{id}/enabled/{status}")
    public String updateUserEnabledStatus(@PathVariable Integer id, @PathVariable boolean status, RedirectAttributes ra) throws UserNotFoundException {
        userService.updateUserEnabledStatus(id, status);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "The user [ID: " + id + "] has been " + (status ? "enabled" : "disabled"));
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports user entities.
     *
     * @param format the export format
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/users/export/{format}")
    public void exportEntities(@PathVariable String format, HttpServletResponse response) throws IOException {
        String[] headers = {"Id", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};

        Function<User, String[]> extractor = user -> new String[]{
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(Role::getSimpleName).collect(Collectors.joining(",", "\"", "\"")),
                String.valueOf(user.isEnabled())
        };

        ExportUtil.export(format, this::listAll, headers, extractor, response);
    }

    /**
     * Lists all users sorted by the specified field and direction.
     *
     * @return the list of users
     */
    private List<User> listAll() {
        return userService.listAll("id", Constants.SORT_ASCENDING);
    }

}
