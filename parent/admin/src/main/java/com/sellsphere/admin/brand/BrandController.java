package com.sellsphere.admin.brand;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
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
            @PagingAndSortingParam(listName = "brandList", moduleURL = "/brand") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        brandService.listPage(pageNum, helper);

        return "brand/brands";
    }

    @GetMapping({"/brands/new", "/brands/edit/{id}"})
    public String showBrandForm(@PathVariable(required = false) Integer id, Model model)
            throws BrandNotFoundException {
        Brand brand;

        if (id != null) {
            brand = brandService.get(id);
        } else {
            brand = new Brand();
        }

        model.addAttribute("brand", brand);
        prepareModelFormAttributes(id, model);

        return BRAND_FORM;
    }

    @PostMapping("/brands/save")
    public String saveBrand(@Valid @ModelAttribute("brand") Brand brand,
                            BindingResult bindingResult, RedirectAttributes redirectAttributes,
                            Model model,
                            @RequestParam(value = "newImage") MultipartFile file)
            throws IOException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.brand");
        validationHelper.validateMultipartFile(file, brand.getId(), "logo",
                                               "Brand logo is required"
        );

        if (!validationHelper.validate()) {
            prepareModelFormAttributes(brand.getId(), model);

            return BRAND_FORM;
        }

        String successMessage =
                "Successfully " + (brand.getId() != null ? "updated" : "saved") + " a brand '" + brand.getName() + "'";

        brandService.save(brand, file);

        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL;
    }

    private void prepareModelFormAttributes(Integer id, Model model) {
        String pageTitle;

        if (id != null) {
            pageTitle = "Edit Brand [ID: " + id + "]";
        } else {
            pageTitle = "Create New Brand";
        }

        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name",
                                                                                  Constants.SORT_ASCENDING
        );

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("pageTitle", pageTitle);

    }


}
