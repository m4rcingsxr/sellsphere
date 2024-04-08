package com.sellsphere.admin.brand;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.admin.export.ExportUtil;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Constants;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller class for managing Brand-related operations.
 */
@RequiredArgsConstructor
@Controller
public class BrandController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/brands/page/0?sortField=name" +
            "&sortDir=asc";
    public static final String BRAND_FORM = "brand/brand_form";

    private final BrandService brandService;
    private final CategoryService categoryService;

    /**
     * Redirects to the first page of the brand list.
     *
     * @return the redirect URL for the first page
     */
    @GetMapping("/brands")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }


    /**
     * Lists brands by page.
     *
     * @param helper  the PagingAndSortingHelper
     * @param pageNum the page number
     * @return the view name for the brand list
     */
    @GetMapping("/brands/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "brandList", moduleURL = "/brand") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        brandService.listPage(pageNum, helper);

        return "brand/brands";
    }

    /**
     * Shows the form for creating or editing a brand.
     *
     * @param id    the brand ID (optional)
     * @param model the model
     * @return the view name for the brand form
     * @throws BrandNotFoundException if the brand is not found
     */
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

    /**
     * Saves a brand.
     *
     * @param brand              the brand
     * @param bindingResult      the binding result
     * @param redirectAttributes the redirect attributes
     * @param model              the model
     * @param file               the brand logo file
     * @return the redirect URL after saving
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/brands/save")
    public String saveBrand(@Valid @ModelAttribute("brand") Brand brand,
                            BindingResult bindingResult, RedirectAttributes redirectAttributes,
                            Model model, @RequestParam(value = "newImage") MultipartFile file)
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


    /**
     * Prepares model attributes for the brand form.
     *
     * @param id    the brand ID
     * @param model the model
     */
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

    /**
     * Deletes a brand.
     *
     * @param id                 the brand ID
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL after deletion
     * @throws BrandNotFoundException if the brand is not found
     */
    @GetMapping("/brands/delete/{id}")
    public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes)
            throws BrandNotFoundException {
        brandService.delete(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                                             "The Brand [ID: " + id + "] has been deleted " +
                                                     "successfully"
        );

        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports brand entities.
     *
     * @param format   the export format
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/brands/export/{format}")
    public void exportEntities(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {"Id", "Name", "Categories"};

        Function<Brand, String[]> extractor = brand -> new String[]{String.valueOf(brand.getId()),
                                                                    brand.getName(),
                                                                    brand.getCategories().stream().map(
                                                                            Category::getName).collect(
                                                                            Collectors.joining(",",
                                                                                               "\"",
                                                                                               "\""
                                                                            ))};
        ExportUtil.export(format, this::listAll, headers, extractor, response);
    }

    private List<Brand> listAll() {
        return brandService.listAll("name", Constants.SORT_ASCENDING);
    }

}
