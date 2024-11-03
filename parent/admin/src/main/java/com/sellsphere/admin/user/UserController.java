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
import org.springframework.data.domain.Sort;
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
 * Controller class for managing User-related operations such as creating, editing,
 * deleting, enabling/disabling, listing, and exporting users.
 */
@RequiredArgsConstructor
@Controller
public class UserController {

    private static final String USER_FORM_VIEW = "user/user_form";
    public static final String DEFAULT_REDIRECT_URL = "redirect:/users/page/0?sortField=firstName" +
            "&sortDir=asc";

    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class,
                                    new StringTrimmerEditor(true)
        );
    }

    /**
     * Redirects to the first page of the user list.
     *
     * @return redirect URL for the first page.
     */
    @GetMapping("/users")
    public String redirectToFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Lists users by page, with sorting and filtering options.
     *
     * @param helper  the paging and sorting helper.
     * @param pageNum the page number to display.
     * @return the view name for the paginated user list.
     */
    @GetMapping("/users/page/{pageNum}")
    public String listUsersByPage(
            @PagingAndSortingParam(listName = "userList", moduleURL = "/users") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        userService.listPage(pageNum, helper);
        return "user/users";
    }

    /**
     * Displays the form for creating a new user or editing an existing user.
     *
     * @param id    the user ID (for editing), null for creating a new user.
     * @param model the model to hold form data.
     * @return the view name for the user form.
     * @throws UserNotFoundException if the user is not found (in case of editing).
     */
    @GetMapping({"/users/new", "/users/edit/{id}"})
    public String showUserForm(@PathVariable(required = false, name = "id") Integer id, Model model)
            throws UserNotFoundException {
        User user;
        String pageTitle;

        if (id != null) {
            // Editing an existing user
            user = userService.get(id);
            pageTitle = "Edit User [ID: " + id + "]";
        } else {
            // Creating a new user
            user = new User();
            pageTitle = "Create New User";
        }

        List<Role> roleList = userService.listAllRoles();
        model.addAttribute("roleList", roleList);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", pageTitle);

        return USER_FORM_VIEW;
    }

    /**
     * Saves or updates a user after form submission.
     *
     * @param user          the user data from the form.
     * @param bindingResult the result of form validation.
     * @param ra            the redirect attributes to add feedback messages.
     * @param file          the user image file (optional).
     * @return the redirect URL after saving the user.
     * @throws IOException if an I/O error occurs while handling the file.
     * @throws UserNotFoundException if the user is not found (in case of update).
     */
    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           RedirectAttributes ra,
                           @RequestParam(value = "newImage", required = false) MultipartFile file)
            throws IOException, UserNotFoundException {

        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.user");
        validationHelper.validateMultipartFile(file, user.getId(), "mainImage",
                                               "An image file is required.");
        validationHelper.validatePassword(user.getPassword(), user.getId());

        if (!validationHelper.validate()) {
            return USER_FORM_VIEW;
        }

        userService.save(user, file);

        String successMessage = "The user has been " + (user.getId() != null ? "updated" : "saved") + " successfully.";
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return getUserPostModificationURL(user);
    }

    /**
     * Constructs the redirect URL after saving or updating a user.
     * Redirects to the user list page with a keyword based on the user's email.
     *
     * @param user the user that was saved or updated.
     * @return the redirect URL after modification.
     */
    private String getUserPostModificationURL(User user) {
        String initialEmailPart = user.getEmail().split("@")[0];
        return DEFAULT_REDIRECT_URL + "&keyword=" + initialEmailPart;
    }

    /**
     * Deletes a user by ID.
     *
     * @param id                 the user ID.
     * @param redirectAttributes the redirect attributes to add feedback messages.
     * @return the redirect URL after deletion.
     * @throws UserNotFoundException if the user is not found.
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Integer id,
                             RedirectAttributes redirectAttributes) throws UserNotFoundException {
        userService.delete(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                                             "The user [ID: " + id + "] has been deleted successfully.");
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Updates the enabled/disabled status of a user.
     *
     * @param id    the user ID.
     * @param status the new enabled status.
     * @param ra    the redirect attributes to add feedback messages.
     * @return the redirect URL after updating the status.
     * @throws UserNotFoundException if the user is not found.
     */
    @GetMapping("/users/{id}/enabled/{status}")
    public String updateUserEnabledStatus(@PathVariable Integer id, @PathVariable boolean status,
                                          RedirectAttributes ra) throws UserNotFoundException {
        userService.updateUserEnabledStatus(id, status);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                             "The user [ID: " + id + "] has been " + (status ? "enabled" : "disabled") + ".");
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports users in the specified format (e.g., CSV, Excel).
     *
     * @param format   the export format (e.g., csv, excel).
     * @param response the HTTP response to write the export data to.
     * @throws IOException if an I/O error occurs while exporting.
     */
    @GetMapping("/users/export/{format}")
    public void exportUsers(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {"Id", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};

        Function<User, String[]> extractor = user -> new String[]{
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(Role::getSimpleName).collect(Collectors.joining(",", "\"", "\"")),
                String.valueOf(user.isEnabled())
        };

        ExportUtil.export(format, this::listAllUsers, headers, extractor, response);
    }

    /**
     * Lists all users, sorted by ID in ascending order.
     *
     * @return a list of all users.
     */
    private List<User> listAllUsers() {
        return userService.listAll("id", Sort.Direction.ASC);
    }
}
