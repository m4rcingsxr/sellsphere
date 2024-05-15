package com.sellsphere.client.product;

import com.sellsphere.client.category.CategoryService;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    public static final String PRODUCTS_PATH = "product/products";

    private final CategoryService categoryService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/c/{category_alias}")
    public String viewProductsByCategory(
            @PathVariable(value = "category_alias") String alias,
            Model model) throws CategoryNotFoundException {
        String pageTitle = "Products by category: ";

        Category category = categoryService.getCategoryByAlias(alias);
        List<Category> categoryParentList = categoryService.getCategoryParents(category);

        model.addAttribute("categoryParentList", categoryParentList);
        model.addAttribute("pageTitle", pageTitle.concat(category.getName()));
        model.addAttribute("category", category);

        return PRODUCTS_PATH;
    }

    @GetMapping("/p/search")
    public String viewProductsByKeyword(
            @RequestParam(value = "keyword") String keyword,
            Model model
    ) {
        model.addAttribute("keyword", keyword);
        return PRODUCTS_PATH;
    }


}