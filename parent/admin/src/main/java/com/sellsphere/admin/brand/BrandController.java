package com.sellsphere.admin.brand;

import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
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
public class BrandController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/brands/page/0?sortField=name" +
            "&sortDir=asc";
    public static final String BRAND_FORM = "brand/brand_form";

    private final BrandService brandService;
    private final CategoryService categoryService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/brands")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/brands/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "brandList", moduleURL =
                    "/brand") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum
    ) {
        brandService.listPage(pageNum, helper);

        return "brand/brands";
    }

    @GetMapping({"/brands/new", "/brands/edit/{id}"})
    public String showBrandForm(@PathVariable(required = false) Integer id,
                                Model model) throws BrandNotFoundException {
        Brand brand;
        String pageTitle;

        if (id != null) {
            brand = brandService.get(id);
            pageTitle = "Edit Brand [ID: " + id + "]";
        } else {
            brand = new Brand();
            pageTitle = "Create New Brand";
        }

        List<Category> categoryList =
                categoryService.listAllRootCategoriesSorted("name", Constants.SORT_ASCENDING);

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("brand", brand);
        model.addAttribute("pageTitle", pageTitle);

        return BRAND_FORM;
    }

}
