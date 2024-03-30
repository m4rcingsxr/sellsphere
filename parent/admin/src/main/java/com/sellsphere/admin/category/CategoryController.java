package com.sellsphere.admin.category;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import com.sellsphere.common.entity.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/categories/page/0?sortField=name&sortDir=asc";

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class,
                                        new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/categories")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }


    @GetMapping("/categories/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "categoryList", moduleURL =
                    "/category") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        categoryService.listPage(pageNum, helper);

        return "category/categories";
    }

    @GetMapping({"/categories/new", "/categories/edit/{id}"})
    public String showCategoryForm(@PathVariable(required = false) Integer id,
                                   Model model)
            throws CategoryNotFoundException {

        Category category;
        String pageTitle;

        if (id != null) {
            category = categoryService.get(id);
            pageTitle = "Edit Category [ID: " + id + "]";
        } else {
            category = new Category();
            pageTitle = "Create new category";
        }

        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", Constants.SORT_ASCENDING);

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", pageTitle);

        return "category/category_form";
    }

}
