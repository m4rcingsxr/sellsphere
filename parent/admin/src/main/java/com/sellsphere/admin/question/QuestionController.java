package com.sellsphere.admin.question;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.admin.user.UserService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

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

    @GetMapping("/questions/edit/{id}")
    public String showQuestionForm(@PathVariable Integer id, Model model) throws
            QuestionNotFoundException {
        prepareModalForQuestionForm(id, model);
        return "question/question_form";
    }

    @PostMapping("/questions/save")
    public String saveQuestion(@ModelAttribute("question") Question question, Principal principal, RedirectAttributes ra) throws
            UserNotFoundException {
        String email = principal.getName();
        User answerer = userService.get(email);

        questionService.save(question, answerer);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Successfully updated question id [" + question.getId() + "]");

        return DEFAULT_REDIRECT_URL;
    }

    private void prepareModalForQuestionForm(Integer id, Model model) throws QuestionNotFoundException {
        Question question = questionService.get(id);
        model.addAttribute("question", question);
    }

}
