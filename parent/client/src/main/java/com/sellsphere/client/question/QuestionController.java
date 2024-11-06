package com.sellsphere.client.question;

import com.sellsphere.client.RecaptchaVerificationService;
import com.sellsphere.client.category.CategoryService;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.customer.RecaptchaVerificationFailed;
import com.sellsphere.client.product.ProductService;
import com.sellsphere.common.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final CategoryService categoryService;
    private final QuestionVoteService questionVoteService;

    @GetMapping("/questions/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model, Principal principal)
            throws QuestionNotFoundException, CustomerNotFoundException {

        Question question = questionService.findById(id);
        model.addAttribute("question", question);

        questionVoteService.markQuestionVotedForProductByCustomer(List.of(question), question.getProduct(), getAuthenticatedCustomer(principal));

        return "question/question_detail_sidebar";
    }

    @GetMapping("/questions")
    public String showCustomerAskedQuestions(Principal principal, Model model) throws CustomerNotFoundException {
        Customer authenticatedCustomer = getAuthenticatedCustomer(principal);
        List<Question> questionList = questionService.findQuestionsByCustomer(authenticatedCustomer);

        model.addAttribute("questionList", questionList);
        model.addAttribute("customer", authenticatedCustomer);

        return "question/questions";
    }

    @PostMapping("/questions/create")
    public String createQuestion(Question question, RedirectAttributes ra, @RequestParam("g-recaptcha-response") String token)
            throws IOException, RecaptchaVerificationFailed {
        RecaptchaVerificationService recaptchaService = new RecaptchaVerificationService();
        RecaptchaVerificationService.VerificationResult result = recaptchaService.verifyToken(token);
        recaptchaService.validate(result);

        questionService.save(question);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Question has been asked, waiting for approve.");
        return "redirect:/questions";
    }

    @GetMapping("/questions/p/{product_alias}/page/{pageNum}")
    public String pageQuestions(@PathVariable("product_alias") String productAlias,
                                @PathVariable Integer pageNum,
                                @RequestParam(value = "sortField", required = false) String sortField,
                                Model model, Principal principal)
            throws ProductNotFoundException, CustomerNotFoundException {
        Product product = productService.findByAlias(productAlias);
        Customer customer = getAuthenticatedCustomer(principal);

        Page<Question> questionPage = questionService.pageQuestions(product, pageNum, sortField);
        List<Category> listCategoryParents = categoryService.getCategoryParents(product.getCategory());

        questionVoteService.markQuestionVotedForProductByCustomer(questionPage.getContent(), product, customer);

        model.addAttribute("product", product);
        model.addAttribute("questionList", questionPage.getContent());
        model.addAttribute("listCategoryParents", listCategoryParents);
        model.addAttribute("customer", customer);
        model.addAttribute("question", new Question());

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("sortField", sortField);

        return "question/product_questions";
    }

    private Customer getAuthenticatedCustomer(Principal principal)
            throws CustomerNotFoundException {
        if(principal != null) {
            return customerService.getByEmail(principal.getName());
        }

        return null;
    }

}
