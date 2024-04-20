package com.sellsphere.admin.user;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.ForbiddenException;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

/**
 * Controller dedicated to handling actions related to user accounts, such as viewing and
 * updating account information.
 */
@RequiredArgsConstructor
@Controller
public class AccountController {

    private static final String ACCOUNT_FORM_PATH = "user/account_form";

    private final UserService userService;

    /**
     * Presents the account form pre-populated with user information, enabling users to view and
     * update their account details.
     *
     * @param email User's email to fetch and display the corresponding account details.
     * @param model Spring's model interface to add attributes used for rendering views.
     * @return Path to the account form view.
     * @throws UserNotFoundException Thrown if no user is found with the specified email.
     */
    @GetMapping("/account")
    public String showAccountForm(@RequestParam("email") String email, Principal principal,
                                  Model model) throws UserNotFoundException {
        validateUserAuthenticated(principal, email);

        model.addAttribute("user", userService.get(email));
        model.addAttribute("roleList", userService.listAllRoles());

        return ACCOUNT_FORM_PATH;
    }

    /**
     * Processes the form submission for account updates. Supports updates to various user
     * details including handling file uploads for profile adjustments.
     *
     * @param user Updated user details from the form submission.
     * @param ra   Used to add flash attributes, providing feedback messages post-redirect.
     * @param file Optional multipart file for profile adjustments.
     * @return Redirects to the account form, indicating the completion of the update process.
     * @throws IOException Thrown if there is an error processing the file upload.
     */
    @PostMapping("/account/update")
    public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
                             RedirectAttributes ra,
                             @RequestParam(value = "newImage", required = false) MultipartFile file)
            throws IOException, UserNotFoundException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.user");
        validationHelper.validateMultipartFile(file, user.getId(), "mainImage", "An image file is required.");
        validationHelper.validatePassword(user.getPassword(), user.getId());

        if (!validationHelper.validate()) {
            return ACCOUNT_FORM_PATH;
        }

        userService.save(user, file);

        String successMessage = "The user " + user.getEmail() + " has been updated successfully.";
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return "redirect:/account?email=" + user.getEmail();
    }

    private void validateUserAuthenticated(Principal principal, String username) {
        if (principal == null || !principal.getName().equals(username))
            throw new ForbiddenException();
    }

}
