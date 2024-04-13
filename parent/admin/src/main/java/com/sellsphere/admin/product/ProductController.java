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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
@Controller
public class ProductController {

    public static final String PRODUCT_FORM = "product/product_form";
    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/products/page/0?sortField=name" + "&sortDir=asc";

    private final ProductService productService;
    private final BrandService brandService;

    @GetMapping("/products")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/products/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "productList", moduleURL = "/products") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum) {
        productService.listPage(pageNum, helper);

        return "product/products";
    }

    @GetMapping({"/products/new", "/products/edit/{id}"})
    public String showProductForm(@PathVariable(required = false) Integer id, Model model)
            throws ProductNotFoundException {
        Product product = (id != null) ? productService.get(id) : new Product();

        prepareModelForProductForm(product, model);
        return PRODUCT_FORM;
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult bindingResult, Model model,
                              RedirectAttributes redirectAttributes,
                              @RequestParam("newImage") MultipartFile newPrimaryImage,
                              @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
                              @RequestParam(value = "values", required = false) String[] detailValues,
                              @RequestParam(value = "names", required = false) String[] detailNames)
            throws IOException {
        ProductHelper.addProductDetails(product, detailValues, detailNames);

        ValidationHelper validationHelper = new ValidationHelper(bindingResult, "error.product");
        validationHelper.validateMultipartFile(newPrimaryImage, product.getId(), "mainImage",
                                               "Main image is required"
        );

        if (!validationHelper.validate()) {
            prepareModelForProductForm(product, model);
            return PRODUCT_FORM;
        }

        productService.save(product, newPrimaryImage, extraImages);

        String successMessage = (product.getId() != null) ? "The product has been updated " +
                "successfully." : "A new product has been created " + "successfully.";
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, successMessage);

        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/products/details/{id}")
    public String viewProductDetails(@PathVariable("id") Integer id, Model model)
            throws ProductNotFoundException {
        Product product = productService.get(id);
        prepareModelForProductForm(product, model);

        return "product/product_detail_modal";
    }

    private void prepareModelForProductForm(Product product, Model model) {
        String pageTitle = (product != null && product.getId() != null) ?
                "Edit Product [ID: " + product.getId() + "]" : "Create New Product";

        List<Brand> brandList = brandService.listAll("name", Constants.SORT_ASCENDING);

        model.addAttribute("brandList", brandList);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("product", product);
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id,
                                RedirectAttributes redirectAttributes)
            throws ProductNotFoundException {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                                             "The product has been deleted successfully."
        );

        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/products/{id}/enabled/{status}")
    public String updateProductEnabledStatus(@PathVariable("id") Integer id,
                                             @PathVariable("status") boolean enabled,
                                             RedirectAttributes redirectAttributes)
            throws ProductNotFoundException {
        productService.updateProductEnabledStatus(id, enabled);
        String message = "The product ID " + id + " has been " + (enabled ? "enabled" : "disabled"
        ) + ".";
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, message);

        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/products/export/{format}")
    public void exportEntities(@PathVariable String format, HttpServletResponse response)
            throws IOException {
        String[] headers = {"Id", "Name", "Alias", "Created time", "Enabled", "In stock", "Cost",
                            "Price", "Discount percent", "Length", "Width", "Height", "Weight"};

        Function<Product, String[]> extractor = product -> new String[]{
                String.valueOf(product.getId()), product.getName(), product.getAlias(),
                product.getCreatedTime().toString(), String.valueOf(product.isEnabled()),
                String.valueOf(product.isInStock()), String.valueOf(product.getCost()),
                String.valueOf(product.getPrice()), String.valueOf(product.getDiscountPercent()),
                String.valueOf(product.getLength()), String.valueOf(product.getWidth()),
                String.valueOf(product.getHeight()), String.valueOf(product.getWeight()),};


        ExportUtil.export(format, () -> productService.listAll("id", Constants.SORT_ASCENDING),
                          headers, extractor, response
        );
    }

}
