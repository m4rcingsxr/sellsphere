package com.sellsphere.admin.brand;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.admin.export.ExportUtil;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.admin.product.ProductService;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.BrandNotFoundException;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Constants;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
 * Controller class responsible for managing CRUD operations for brands.
 * Includes functionality for creating, updating, listing, deleting, and exporting brands.
 */
@RequiredArgsConstructor
@Controller
public class BrandController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/brands/page/0?sortField=name&sortDir=asc";
    public static final String BRAND_FORM_VIEW = "brand/brand_form";

    private final BrandService brandService;
    private final CategoryService categoryService;
    private final ProductService productService;

    /**
     * Redirects to the first page of the brand list.
     *
     * @return the redirect URL for the first page of brand list
     */
    @GetMapping("/brands")
    public String redirectToFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }


    /**
     * Lists brands by page with optional sorting.
     *
     * @param helper  the PagingAndSortingHelper for managing pagination and sorting
     * @param pageNum the page number to display
     * @return the view name for the paginated brand list
     */
    @GetMapping("/brands/page/{pageNum}")
    public String listBrandsByPage(
            @PagingAndSortingParam(listName = "brandList", moduleURL = "/brand") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        brandService.listBrandsByPage(pageNum, helper);
        return "brand/brands";
    }

    /**
     * Displays the form for creating a new brand or editing an existing one.
     *
     * @param id    the brand ID (optional, null if creating a new brand)
     * @param model the model to hold form data
     * @return the view name for the brand form
     * @throws BrandNotFoundException if the brand is not found when editing
     */
    @GetMapping({"/brands/new", "/brands/edit/{id}"})
    public String showBrandForm(@PathVariable(required = false) Integer id, Model model)
            throws BrandNotFoundException {
        Brand brand = (id != null) ? brandService.getBrandById(id) : new Brand();
        model.addAttribute("brand", brand);
        populateModelWithCategoriesAndPageTitle(id, model);
        return BRAND_FORM_VIEW;
    }

    /**
     * Handles the submission of the brand form to create or update a brand.
     * Performs validation and saves the brand along with an optional logo image.
     *
     * @param brand              the brand object from the form
     * @param bindingResult      the result of form validation
     * @param redirectAttributes attributes for storing messages to be displayed after redirection
     * @param model              the model for holding form data
     * @param file               the optional logo file for the brand
     * @return the redirect URL to the brand list after saving
     * @throws IOException if there is an error handling the file upload
     */
    @PostMapping("/brands/save")
    public String saveBrand(@Valid @ModelAttribute("brand") Brand brand,
                            BindingResult bindingResult, RedirectAttributes redirectAttributes,
                            Model model, @RequestParam(value = "newImage") MultipartFile file)
            throws IOException {

        // Validate the brand logo and process form validation errors
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.brand");
        validationHelper.validateMultipartFile(file, brand.getId(), "logo", "Brand logo is required");

        if (!validationHelper.validate()) {
            populateModelWithCategoriesAndPageTitle(brand.getId(), model);
            return BRAND_FORM_VIEW; // Return to the form if validation fails
        }

        // Save the brand and add a success message
        brandService.saveBrand(brand, file);
        String action = (brand.getId() != null) ? "updated" : "saved";
        String successMessage = String.format("Successfully %s the brand '%s'", action, brand.getName());
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL; // Redirect to the brand list
    }

    /**
     * Populates the model with category data and the appropriate page title for the brand form.
     *
     * @param id    the brand ID (optional, null if creating a new brand)
     * @param model the model to populate
     */
    private void populateModelWithCategoriesAndPageTitle(Integer id, Model model) {
        String pageTitle = (id != null) ? "Edit Brand [ID: " + id + "]" : "Create New Brand";
        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("pageTitle", pageTitle);
    }

    /**
     * Deletes a brand by its ID.
     *
     * @param id                 the brand ID to delete
     * @param redirectAttributes attributes for storing messages to be displayed after redirection
     * @return the redirect URL to the brand list after deletion
     * @throws BrandNotFoundException if the brand is not found
     */
    @GetMapping("/brands/delete/{id}")
    public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes)
            throws BrandNotFoundException {
        long count = productService.countByBrandId(id);

        if (count > 0) {
            redirectAttributes.addFlashAttribute(Constants.ERROR_MESSAGE, "Cannot delete brand that's assigned to products");
            return DEFAULT_REDIRECT_URL;
        }

        brandService.deleteBrandById(id);
        String successMessage = String.format("The Brand [ID: %d] has been deleted successfully.", id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports brand data in the specified format (e.g., CSV, Excel).
     *
     * @param format   the export format (csv, excel, etc.)
     * @param response the HTTP response to write the exported data to
     * @throws IOException if there is an error writing the response
     */
    @GetMapping("/brands/export/{format}")
    public void exportBrandData(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {"Id", "Name", "Categories"};
        Function<Brand, String[]> dataExtractor = brand -> new String[]{
                String.valueOf(brand.getId()),
                brand.getName(),
                brand.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.joining(",", "\"", "\""))
        };

        ExportUtil.export(format, this::listAllBrands, headers, dataExtractor, response);
    }

    /**
     * Returns a list of all brands, sorted by name in ascending order.
     *
     * @return the list of all brands
     */
    private List<Brand> listAllBrands() {
        return brandService.listAllBrands("name", Sort.Direction.ASC);
    }
}
