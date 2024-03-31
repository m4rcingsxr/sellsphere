package com.sellsphere.admin.category;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import com.sellsphere.common.entity.CategoryNotFoundException;
import com.sellsphere.common.entity.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class CategoryController {

    public static final String CATEGORY_FORM = "category/category_form";
    public static final String DEFAULT_REDIRECT_URL = "redirect:/categories/page/0?sortField=name"
            + "&sortDir=asc";

    private final CategoryService categoryService;


    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/categories")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }


    @GetMapping("/categories/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "categoryList", moduleURL = "/category") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        categoryService.listPage(pageNum, helper);

        return "category/categories";
    }

    @GetMapping({"/categories/new", "/categories/edit/{id}"})
    public String showCategoryForm(@PathVariable(required = false) Integer id, Model model)
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

        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name",
                                                                                  Constants.SORT_ASCENDING
        );

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", pageTitle);

        return CATEGORY_FORM;
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult bindingResult, RedirectAttributes ra,
                               @RequestParam(value = "newImage", required = false) MultipartFile file)
            throws IOException, CategoryIllegalStateException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.category");
        validationHelper.validateMultipartFile(file, category.getId(), "image",
                                               "An image file is required."
        );
        validationHelper.validateWithBooleanSupplier(
                () -> category.getParent() != null || (category.getParent() == null && category.getCategoryIcon() != null && category.getCategoryIcon().getIconPath() != null),
                "categoryIcon.iconPath",
                "When category is root then icon must be defined."
        );

        if (!validationHelper.validate()) {
            return CATEGORY_FORM;
        }

        String successMessage =
                "Category " + category.getName() + " successfully " + (category.getId() != null ?
                        "updated" : "created");

        categoryService.save(category, file);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);
        return DEFAULT_REDIRECT_URL;
    }

}
