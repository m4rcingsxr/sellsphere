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
 * Controller class for managing Category-related operations.
 */
@RequiredArgsConstructor
@Controller
public class CategoryController {

    public static final String CATEGORY_FORM = "category/category_form";
    public static final String DEFAULT_REDIRECT_URL = "redirect:/categories/page/0?sortField=name&sortDir=asc";

    private final CategoryService categoryService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /**
     * Lists categories by page.
     *
     * @param helper the PagingAndSortingHelper
     * @param pageNum the page number
     * @return the view name for the category list
     */
    @GetMapping("/categories/page/{pageNum}")
    public String listPage(@PagingAndSortingParam(listName = "categoryList", moduleURL = "/category") PagingAndSortingHelper helper,
                           @PathVariable("pageNum") Integer pageNum) {
        categoryService.listPage(pageNum, helper);
        return "category/categories";
    }

    /**
     * Shows the form for creating or editing a category.
     *
     * @param id the category ID (optional)
     * @param model the model
     * @return the view name for the category form
     * @throws CategoryNotFoundException if the category is not found
     */
    @GetMapping({"/categories/new", "/categories/edit/{id}"})
    public String showCategoryForm(@PathVariable(required = false) Integer id, Model model)
            throws CategoryNotFoundException {
        Category category;

        if (id != null) {
            category = categoryService.get(id);
        } else {
            category = new Category();
        }

        model.addAttribute("category", category);
        prepareModelForCategoryForm(model, id);

        return CATEGORY_FORM;
    }

    /**
     * Saves a category.
     *
     * @param category the category
     * @param bindingResult the binding result
     * @param ra the redirect attributes
     * @param file the category image file
     * @param model the model
     * @return the redirect URL after saving
     * @throws IOException if an I/O error occurs
     * @throws CategoryIllegalStateException if the category state is illegal
     */
    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult bindingResult, RedirectAttributes ra,
                               @RequestParam(value = "newImage", required = false) MultipartFile file,
                               Model model) throws IOException, CategoryIllegalStateException {
        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.category");
        validationHelper.validateMultipartFile(file, category.getId(), "image", "An image file is required.");
        validationHelper.validateWithBooleanSupplier(
                () -> category.getParent() != null || category.getCategoryIcon() != null && category.getCategoryIcon().getIconPath() != null,
                "categoryIcon.iconPath", "When category is root then icon must be defined."
        );

        if (!validationHelper.validate()) {
            prepareModelForCategoryForm(model, category.getId());
            return CATEGORY_FORM;
        }

        String successMessage = "Category " + category.getName() + " successfully " + (category.getId() != null ? "updated" : "created");
        categoryService.save(category, file);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Prepares model attributes for the category form.
     *
     * @param model the model
     * @param id the category ID
     */
    private void prepareModelForCategoryForm(Model model, Integer id) {
        String pageTitle;

        if (id != null) {
            pageTitle = "Edit Category [ID: " + id + "]";
        } else {
            pageTitle = "Create new category";
        }

        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", Constants.SORT_ASCENDING);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("pageTitle", pageTitle);
    }

    /**
     * Deletes a category.
     *
     * @param id the category ID
     * @param redirectAttributes the redirect attributes
     * @return the redirect URL after deletion
     * @throws CategoryIllegalStateException if the category state is illegal
     * @throws CategoryNotFoundException if the category is not found
     */
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes)
            throws CategoryIllegalStateException, CategoryNotFoundException {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, "The category [ID: " + id + "] has been deleted successfully");
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Deletes a branch of categories.
     *
     * @param id the category ID
     * @param ra the redirect attributes
     * @return the redirect URL after deletion
     * @throws CategoryNotFoundException if the category is not found
     */
    @GetMapping("/categories/delete_branch/{id}")
    public String deleteCategoryBranch(@PathVariable("id") Integer id, RedirectAttributes ra)
            throws CategoryNotFoundException {
        categoryService.deleteCategoryBranch(id);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "The branch of category [ID: " + id + "] has been deleted successfully");
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Updates the enabled status of a category.
     *
     * @param id the category ID
     * @param status the new enabled status
     * @param ra the redirect attributes
     * @return the redirect URL after updating the status
     * @throws CategoryNotFoundException if the category is not found
     */
    @GetMapping("/categories/{id}/enabled/{status}")
    public String updateCategoryEnabledStatus(@PathVariable("id") int id, @PathVariable("status") boolean status, RedirectAttributes ra)
            throws CategoryNotFoundException {
        categoryService.toggleCategoryEnabledStatus(id, status);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "The category ID " + id + " has been " + (status ? "enabled" : "disabled"));
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports category entities.
     *
     * @param format the export format
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/categories/export/{format}")
    public void exportEntities(@PathVariable String format, HttpServletResponse response) throws IOException {
        String[] headers = {"Id", "Name", "Alias", "Parent id", "Enabled"};

        Function<Category, String[]> extractor = category -> new String[]{
                String.valueOf(category.getId()), category.getName(), category.getAlias(),
                category.getAllParentIDs() == null ? "-" : category.getAllParentIDs(),
                String.valueOf(category.isEnabled())
        };

        ExportUtil.export(format, this::listAll, headers, extractor, response);
    }

    /**
     * Lists all root categories sorted by the specified field and direction.
     *
     * @return the list of categories
     */
    private List<Category> listAll() {
        return categoryService.listAllRootCategoriesSorted("name", Constants.SORT_ASCENDING);
    }

}
