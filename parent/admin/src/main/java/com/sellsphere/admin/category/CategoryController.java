package com.sellsphere.admin.category;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.export.ExportUtil;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.CategoryIllegalStateException;
import com.sellsphere.common.entity.CategoryNotFoundException;
import com.sellsphere.common.entity.Constants;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Controller class for managing CRUD operations for categories.
 * Includes functionality for creating, updating, listing, deleting, and exporting categories.
 */
@RequiredArgsConstructor
@Controller
public class CategoryController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/categories/page/0?sortField=name&sortDir=asc";
    public static final String CATEGORY_FORM_VIEW = "category/category_form";

    private final CategoryService categoryService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /**
     * Redirects to the first page of the category list.
     *
     * @return the redirect URL for the first page of category list
     */
    @GetMapping("/categories")
    public String redirectToFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Lists categories by page with optional sorting.
     *
     * @param helper  the PagingAndSortingHelper for managing pagination and sorting
     * @param pageNum the page number to display
     * @return the view name for the paginated category list
     */
    @GetMapping("/categories/page/{pageNum}")
    public String listCategoriesByPage(
            @PagingAndSortingParam(listName = "categoryList", moduleURL = "/category") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum) {
        categoryService.listCategoriesByPage(pageNum, helper);
        return "category/categories";
    }

    /**
     * Displays the form for creating a new category or editing an existing one.
     *
     * @param id    the category ID (optional, null if creating a new category)
     * @param model the model to hold form data
     * @return the view name for the category form
     * @throws CategoryNotFoundException if the category is not found when editing
     */
    @GetMapping({"/categories/new", "/categories/edit/{id}"})
    public String showCategoryForm(@PathVariable(required = false) Integer id, Model model)
            throws CategoryNotFoundException {
        Category category = (id != null) ? categoryService.getCategoryById(id) : new Category();
        model.addAttribute("category", category);
        populateModelWithCategoriesAndPageTitle(id, model);
        return CATEGORY_FORM_VIEW;
    }


    /**
     * Handles the submission of the category form to create or update a category.
     * Performs validation and saves the category along with an optional image file.
     *
     * @param category           the category object from the form
     * @param bindingResult      the result of form validation
     * @param redirectAttributes attributes for storing messages to be displayed after redirection
     * @param model              the model for holding form data
     * @param file               the optional image file for the category
     * @return the redirect URL to the category list after saving
     * @throws IOException                  if there is an error handling the file upload
     * @throws CategoryIllegalStateException if the category state is illegal
     */
    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes,
                               Model model, @RequestParam(value = "newImage") MultipartFile file)
            throws IOException, CategoryIllegalStateException {

        // Validate the category image and process form validation errors
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.category");
        validationHelper.validateMultipartFile(file, category.getId(), "image", "Category image is required");
        validationHelper.validateWithBooleanSupplier(
                () -> category.getParent() != null || category.getCategoryIcon() != null && category.getCategoryIcon().getIconPath() != null,
                "categoryIcon.iconPath", "When category is root, then an icon must be defined."
        );

        if (!validationHelper.validate()) {
            populateModelWithCategoriesAndPageTitle(category.getId(), model);
            return CATEGORY_FORM_VIEW; // Return to the form if validation fails
        }

        // Save the category and add a success message
        categoryService.saveCategory(category, file);
        String action = (category.getId() != null) ? "updated" : "created";
        String successMessage = String.format("Successfully %s the category '%s'", action, category.getName());
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL; // Redirect to the category list
    }

    /**
     * Populates the model with category data and the appropriate page title for the category form.
     *
     * @param id    the category ID (optional, null if creating a new category)
     * @param model the model to populate
     */
    private void populateModelWithCategoriesAndPageTitle(Integer id, Model model) {
        String pageTitle = (id != null) ? "Edit Category [ID: " + id + "]" : "Create New Category";
        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("pageTitle", pageTitle);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id                 the category ID to delete
     * @param redirectAttributes attributes for storing messages to be displayed after redirection
     * @return the redirect URL to the category list after deletion
     * @throws CategoryNotFoundException if the category is not found
     */
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes)
            throws CategoryNotFoundException, CategoryIllegalStateException {
        categoryService.deleteCategoryById(id);
        String successMessage = String.format("The Category [ID: %d] has been deleted successfully.", id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports category data in the specified format (e.g., CSV, Excel).
     *
     * @param format   the export format (csv, excel, etc.)
     * @param response the HTTP response to write the exported data to
     * @throws IOException if there is an error writing the response
     */
    @GetMapping("/categories/export/{format}")
    public void exportCategoryData(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {"Id", "Name", "Alias", "Parent id", "Enabled"};
        Function<Category, String[]> dataExtractor = category -> new String[]{
                String.valueOf(category.getId()), category.getName(), category.getAlias(),
                category.getAllParentIDs() == null ? "-" : category.getAllParentIDs(),
                String.valueOf(category.isEnabled())
        };

        ExportUtil.export(format, this::listAllCategories, headers, dataExtractor, response);
    }

    /**
     * Returns a list of all categories, sorted by name in ascending order.
     *
     * @return the list of all categories
     */
    private List<Category> listAllCategories() {
        return categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC);
    }

    /**
     * Updates the enabled status of a category.
     *
     * @param id     the category ID
     * @param status the new enabled status
     * @param ra     the redirect attributes
     * @return the redirect URL after updating the status
     * @throws CategoryNotFoundException if the category is not found
     */
    @GetMapping("/categories/{id}/enabled/{status}")
    public String updateCategoryEnabledStatus(@PathVariable("id") int id, @PathVariable("status") boolean status,
                                              RedirectAttributes ra) throws CategoryNotFoundException {
        categoryService.toggleCategoryEnabledStatus(id, status);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "The category ID " + id + " has been " + (status ? "enabled" : "disabled"));
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/categories/delete_branch/{id}")
    public String deleteCategoryBranch(@PathVariable("id") int id, RedirectAttributes ra)
            throws CategoryNotFoundException {
        categoryService.deleteCategoryBranch(id);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Branch of category " + id + " successfully removed.");
        return DEFAULT_REDIRECT_URL;
    }

}
