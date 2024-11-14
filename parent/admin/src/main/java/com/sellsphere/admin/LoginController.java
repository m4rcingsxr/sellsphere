package com.sellsphere.admin;

import com.sellsphere.admin.user.UserService;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.User;
import com.sellsphere.common.entity.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling user login functionality, including role-based authentication
 * using admin credentials.
 */
@Controller
public class LoginController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final Map<String, Map.Entry<String, String>> roleEmailMap = new HashMap<>();

    /**
     * Constructs the LoginController with required services and initializes role-based
     * user credentials mapping.
     *
     * @param passwordEncoder the password encoder for verifying user passwords
     * @param userService     the user service for retrieving user details
     */
    @Autowired
    public LoginController(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        initializeRoleEmailMap();
    }

    /**
     * Initializes the role-to-email mapping for user credentials used in role-based login.
     */
    private void initializeRoleEmailMap() {
        roleEmailMap.put("EDITOR", Map.entry("editor@example.com", System.getenv("EDITOR_PASSWORD")));
        roleEmailMap.put("SALESPERSON", Map.entry("salesperson@example.com", System.getenv("SALESPERSON_PASSWORD")));
        roleEmailMap.put("ASSISTANT", Map.entry("assistant@example.com", System.getenv("ASSISTANT_PASSWORD")));
        roleEmailMap.put("SHIPPER", Map.entry("shipper@example.com", System.getenv("SHIPPER_PASSWORD")));
    }

    /**
     * Handles login requests for role-based access using admin credentials.
     *
     * @param adminUsername the admin's username
     * @param adminPassword the admin's password
     * @param role          the role to login as
     * @param ra            redirect attributes for passing messages
     * @param request       the HTTP servlet request for session management
     * @return a redirection URL after successful or failed login
     * @throws UserNotFoundException if the admin user or target role user is not found
     */
    @PostMapping("/login-as")
    public String login(
            @RequestParam String adminUsername,
            @RequestParam String adminPassword,
            @RequestParam String role,
            RedirectAttributes ra,
            HttpServletRequest request
    ) {
        User targetUser;
        try {
            User adminUser = authenticateAdminUser(adminUsername, adminPassword);
            targetUser = getTargetUserForRole(role);
        } catch(UserNotFoundException e) {
            return "redirect:/login?error";
        }
        authenticateTargetUser(targetUser, request);
        return "redirect:/?continue";
    }

    /**
     * Authenticates the admin user with the provided credentials.
     *
     * @param username the admin's username
     * @param password the admin's password
     * @return the authenticated admin user
     * @throws UserNotFoundException if the username is not found or password is invalid
     */
    private User authenticateAdminUser(String username, String password) throws UserNotFoundException {
        User adminUser = userService.get(username);
        if (adminUser == null || !passwordEncoder.matches(password, adminUser.getPassword())) {
            throw new UserNotFoundException("Invalid username or password");
        }
        return adminUser;
    }

    /**
     * Retrieves the target user associated with the specified role.
     *
     * @param role the role to login as
     * @return the target user for the specified role
     * @throws UserNotFoundException if the role is invalid or user is not found
     */
    private User getTargetUserForRole(String role) throws UserNotFoundException {
        Map.Entry<String, String> credentials = roleEmailMap.get(role);
        if (credentials == null) {
            throw new IllegalArgumentException("Invalid role provided");
        }
        String targetEmail = credentials.getKey();
        User targetUser = userService.get(targetEmail);
        if (targetUser == null) {
            throw new UserNotFoundException("Target role user not found");
        }
        return targetUser;
    }

    /**
     * Authenticates the target user by setting the security context and session.
     *
     * @param targetUser the target user to authenticate
     * @param request    the HTTP servlet request for session handling
     */
    private void authenticateTargetUser(User targetUser, HttpServletRequest request) {
        CustomUserDetails customUserDetails = new CustomUserDetails(targetUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                             SecurityContextHolder.getContext()
        );
    }
}