package com.sellsphere.client.question;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/questions/create")
    public String createQuestion(Question question, RedirectAttributes ra) {
        questionService.save(question);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Question has been asked, waiting for approve.");
        return "redirect:/p/" + question.getProduct().getAlias();
    }

}
