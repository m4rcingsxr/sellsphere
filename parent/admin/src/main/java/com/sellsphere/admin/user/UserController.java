package com.sellsphere.admin.user;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/users/page/0?sortField=firstName&sortDir=asc";

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
        Page<User> userPage = userService.listPage(pageNum, sortField, sortDir);

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

}

