package com.sellsphere.admin.product;

import com.sellsphere.admin.ValidationHelper;
import com.sellsphere.admin.brand.BrandService;
import com.sellsphere.admin.export.ExportUtil;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductNotFoundException;
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

/**
 * Controller for managing products, including creating, updating, listing, and deleting products.
 * Provides web interfaces for product management and exporting product data.
 */
@RequiredArgsConstructor
@Controller
public class ProductController {

    public static final String PRODUCT_FORM_VIEW = "product/product_form";
    public static final String DEFAULT_REDIRECT_URL = "redirect:/products/page/0?sortField=name&sortDir=asc";

    private final ProductService productService;
    private final BrandService brandService;

    /**
     * Redirects to the first page of the product list.
     *
     * @return the redirect URL for the first page of products.
     */
    @GetMapping("/products")
    public String redirectToFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Lists products by page with optional sorting.
     *
     * @param helper  the helper class for pagination and sorting.
     * @param pageNum the current page number.
     * @return the view name for listing products.
     */
    @GetMapping("/products/page/{pageNum}")
    public String listProductsByPage(
            @PagingAndSortingParam(listName = "productList", moduleURL = "/products") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum) {
        productService.paginateProducts(pageNum, helper);
        return "product/products";
    }

    /**
     * Displays the form for creating or editing a product.
     *
     * @param id    the ID of the product to edit, or null to create a new product.
     * @param model the model to add attributes to.
     * @return the view name for the product form.
     * @throws ProductNotFoundException if the product with the specified ID is not found.
     */
    @GetMapping({"/products/new", "/products/edit/{id}"})
    public String showProductForm(@PathVariable(required = false) Integer id, Model model)
            throws ProductNotFoundException {
        Product product = (id != null) ? productService.getProductById(id) : new Product();
        populateModelWithBrandsAndPageTitle(product, model);
        return PRODUCT_FORM_VIEW;
    }

    /**
     * Saves a product after validating the input.
     *
     * @param product            the product to save.
     * @param bindingResult      the binding result for validation.
     * @param model              the model to add attributes to.
     * @param redirectAttributes the redirect attributes to add flash messages.
     * @param newPrimaryImage    the new primary image for the product.
     * @param extraImages        additional images for the product.
     * @param detailValues       the values of the product details.
     * @param detailNames        the names of the product details.
     * @return the redirect URL after saving the product.
     * @throws IOException if an I/O error occurs during image processing.
     */
    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult bindingResult, Model model,
                              RedirectAttributes redirectAttributes,
                              @RequestParam("newImage") MultipartFile newPrimaryImage,
                              @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
                              @RequestParam(value = "values", required = false) String[] detailValues,
                              @RequestParam(value = "names", required = false) String[] detailNames)
            throws IOException {
        ProductHelper.addProductDetails(product, detailNames, detailValues);

        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.product");
        validationHelper.validateMultipartFile(newPrimaryImage, product.getId(), "mainImage", "Main image is required");

        if (!validationHelper.validate()) {
            populateModelWithBrandsAndPageTitle(product, model);
            return PRODUCT_FORM_VIEW;
        }

        productService.saveProduct(product, newPrimaryImage, extraImages);
        String successMessage = (product.getId() != null)
                ? "The product has been updated successfully."
                : "A new product has been created successfully.";
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Views the details of a product.
     *
     * @param id    the ID of the product to view.
     * @param model the model to add attributes to.
     * @return the view name for the product details modal.
     * @throws ProductNotFoundException if the product with the specified ID is not found.
     */
    @GetMapping("/products/details/{id}")
    public String viewProductDetails(@PathVariable("id") Integer id, Model model)
            throws ProductNotFoundException {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/product_detail_modal";
    }

    /**
     * Updates the enabled status of a product.
     *
     * @param id        the ID of the product to update.
     * @param enabled   the new enabled status.
     * @param redirectAttributes the redirect attributes to add flash messages.
     * @return the redirect URL after updating the product's enabled status.
     * @throws ProductNotFoundException if the product with the specified ID is not found.
     */
    @GetMapping("/products/{id}/enabled/{status}")
    public String updateProductStatus(@PathVariable("id") Integer id,
                                      @PathVariable("status") boolean enabled,
                                      RedirectAttributes redirectAttributes)
            throws ProductNotFoundException {
        productService.updateProductEnabledStatus(id, enabled);
        String message = String.format("The product ID %d has been %s.", id, (enabled ? "enabled" : "disabled"));
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, message);

        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id                the ID of the product to delete.
     * @param redirectAttributes the redirect attributes to add flash messages.
     * @return the redirect URL after deleting the product.
     * @throws ProductNotFoundException if the product with the specified ID is not found.
     */
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id,
                                RedirectAttributes redirectAttributes)
            throws ProductNotFoundException {
        productService.deleteProductById(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                                             "The product has been deleted successfully.");

        return DEFAULT_REDIRECT_URL;
    }

    /**
     * Exports the product data in the specified format.
     *
     * @param format   the format to export the data (e.g., CSV, Excel).
     * @param response the HTTP response to write the exported data to.
     * @throws IOException if an I/O error occurs during data export.
     */
    @GetMapping("/products/export/{format}")
    public void exportProducts(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {"Id", "Name", "Alias", "Created time", "Enabled", "In stock", "Cost",
                            "Price", "Discount percent", "Length", "Width", "Height", "Weight"};

        Function<Product, String[]> extractor = product -> new String[]{
                String.valueOf(product.getId()), product.getName(), product.getAlias(),
                product.getCreatedTime().toString(), String.valueOf(product.isEnabled()),
                String.valueOf(product.isInStock()), String.valueOf(product.getCost()),
                String.valueOf(product.getPrice()), String.valueOf(product.getDiscountPercent()),
                String.valueOf(product.getLength()), String.valueOf(product.getWidth()),
                String.valueOf(product.getHeight()), String.valueOf(product.getWeight())};

        ExportUtil.export(format, () -> productService.listAllProducts("id", Sort.Direction.ASC),
                          headers, extractor, response);
    }

    /**
     * Populates the model with brands and the page title for the product form.
     *
     * @param product the product being edited or created.
     * @param model   the model to populate with attributes.
     */
    private void populateModelWithBrandsAndPageTitle(Product product, Model model) {
        String pageTitle = (product.getId() != null)
                ? "Edit Product [ID: " + product.getId() + "]"
                : "Create New Product";

        List<Brand> brandList = brandService.listAllBrands("name", Sort.Direction.ASC);

        model.addAttribute("brandList", brandList);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("product", product);
    }
}
