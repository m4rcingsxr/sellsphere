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
 * Controller to manage user account operations, including viewing, updating details,
 * and handling profile image uploads.
 */
@RequiredArgsConstructor
@Controller
public class AccountController {

    private static final String ACCOUNT_FORM_VIEW = "user/account_form";
    private final UserService userService;

    /**
     * Displays the user account form pre-populated with the user's information.
     * Only the authenticated user is allowed to view and update their account.
     *
     * @param email the email of the user whose account is being accessed.
     * @param principal the currently logged-in user's details (for validation).
     * @param model the model to populate the view with user details.
     * @return the view path for the account details form.
     * @throws UserNotFoundException if the user with the provided email is not found.
     */
    @GetMapping("/account")
    public String displayAccountForm(@RequestParam("email") String email,
                                     Principal principal,
                                     Model model) throws UserNotFoundException {
        // Ensure only the logged-in user can access their own account
        validateUserOwnership(principal, email);

        // Add user details and roles to the model for the form
        model.addAttribute("user", userService.get(email));
        model.addAttribute("roleList", userService.listAllRoles());

        return ACCOUNT_FORM_VIEW;
    }

    /**
     * Processes the account update form submission, including profile image uploads.
     * Validates user inputs and restricts access to ensure users can only update their own data.
     *
     * @param user the updated user details submitted via the form.
     * @param bindingResult validation results from form input.
     * @param file an optional multipart file for profile image upload.
     * @param redirectAttributes used to add feedback messages post-redirect.
     * @return a redirection to the account form or the same form if validation fails.
     * @throws IOException if an error occurs during file upload.
     * @throws UserNotFoundException if the user is not found.
     */
    @PostMapping("/account/update")
    public String processAccountUpdate(@Valid @ModelAttribute("user") User user,
                                       BindingResult bindingResult,
                                       @RequestParam(value = "newImage", required = false) MultipartFile file,
                                       RedirectAttributes redirectAttributes)
            throws IOException, UserNotFoundException {
        // Initialize validation helper for processing input validations
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.user");

        // Validate profile image and password inputs
        validationHelper.validateMultipartFile(file, user.getId(), "mainImage",
                                               "An image file is required.");
        validationHelper.validatePassword(user.getPassword(), user.getId());

        // Return to the form if validation fails
        if (!validationHelper.validate()) {
            return ACCOUNT_FORM_VIEW;
        }

        // Save the updated user details and optional profile image
        userService.save(user, file);

        // Add success message to redirect attributes and redirect to the account form
        String successMessage = "The account for " + user.getEmail() + " has been updated successfully.";
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return "redirect:/account?email=" + user.getEmail();
    }

    /**
     * Validates that the logged-in user is attempting to access or update their own account.
     * Throws an exception if the user tries to access another user's account.
     *
     * @param principal the current authenticated user's details.
     * @param email the email of the user account being accessed.
     * @throws ForbiddenException if the user is trying to access an account that is not theirs.
     */
    private void validateUserOwnership(Principal principal, String email) {
        if (principal == null || !principal.getName().equals(email)) {
            throw new ForbiddenException();
        }
    }
}
