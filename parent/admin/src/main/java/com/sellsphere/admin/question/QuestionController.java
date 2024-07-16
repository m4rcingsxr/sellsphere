package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.admin.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/questions/page/0?sortField=askTime&sortDir=asc";

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class,
                new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/questions")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/questions/page/{pageNum}")
    public String listPage(@PagingAndSortingParam(listName = "questionList", moduleURL = "/questions")PagingAndSortingHelper helper,
                           @PathVariable(name = "pageNum") int pageNum) {
        questionService.listPage(pageNum, helper);

        return "question/questions";
    }

}
